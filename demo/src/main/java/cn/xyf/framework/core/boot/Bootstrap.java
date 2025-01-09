package cn.xyf.framework.core.boot;

import cn.xyf.framework.core.exception.framework.FrameworkException;
import cn.xyf.framework.core.extension.ExtensionCoordinate;
import cn.xyf.framework.core.extension.IExtensionPoint;
import cn.xyf.framework.core.pipeline.PhaseDO;
import cn.xyf.framework.core.pipeline.PipelineRepository;
import cn.xyf.framework.core.pipeline.ReferenceStepDO;
import cn.xyf.framework.core.pipeline.WrappedStep;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * 启动类,需要使用者在spring容器中注册该类，并调用init方法，该方法会扫描所有配置的包路径，并注册所有符合规则的类，包括pipeline、step、extensionPoint等。
 */
@Slf4j
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    /**
     * 扫描包路径集合
     */
    private List<String> packages;

    private ClassPathScanHandler handler;

    @Autowired
    private RegisterFactory registerFactory;


    public void init() {
        log.info("开始注册pipeline");
        Set<Class<?>> classSet = scanConfiguredPackages();
        log.info("扫描到的类集合"+classSet);
        registerBeans(classSet);
    }

    private PhaseDO getPhaseByName(String pipeline, String phase, TreeMultimap<String, PhaseDO> pipelineRepo) {
        if (null == pipelineRepo || pipelineRepo.isEmpty() || StringUtils.isBlank(pipeline) || StringUtils.isBlank(phase)) {
            return null;
        }
        Set<PhaseDO> phaseDOSet = pipelineRepo.get(pipeline);
        if (null == phaseDOSet || phaseDOSet.isEmpty()) {
            return null;
        }
        for (PhaseDO phaseDO : phaseDOSet) {
            if (StringUtils.equalsIgnoreCase(phaseDO.getName(), phase)) {
                return phaseDO;
            }
        }
        return null;
    }


    private void registerBeans(Set<Class<?>> classSet) {
        try {
            for (Class<?> targetClz : classSet) {
                // 根据传入类获取注册器
                IRegister register = this.registerFactory.getRegister(targetClz);
                if (null != register) {
                    //执行注册
                    register.doRegistration(targetClz);
                }
            }
            // 获取pipelineRepository
            PipelineRepository pipelineRepository = this.registerFactory.getPipelineRegister().getPipelineRepository();
            // 获取pipelineRepository中的所有pipeline，key为pipeline的name，value为phaseDO，一对多
            TreeMultimap<String, PhaseDO> treeMultimap = this.registerFactory.getPipelineRegister().getPipelineRepository().getPipelineRepo();
            // 获取pipelineRepository中的所有phase，key为pipeline.phase，value为wrappedStep
            TreeMultimap<String, WrappedStep> phaseRepo = this.registerFactory.getPipelineRegister().getPipelineRepository().getPhaseRepo();
            // 获取pipelineRepository中的所有step，key为pipeline.phase.stepName，value为wrappedStep
            Map<String, WrappedStep> stepMap = this.registerFactory.getPipelineRegister().getPipelineRepository().getStepMap();
            for (String pipeline : treeMultimap.keySet()) {
                Set<PhaseDO> phaseDOSet = treeMultimap.get(pipeline);
                for (PhaseDO phaseDO : phaseDOSet) {
                    String originPipeline = phaseDO.getOriginPipeline();
                    String originPhase = phaseDO.getOriginPhase();
                    List<ReferenceStepDO> referenceStepDOList = phaseDO.getReferenceStepDOList();

                    if (StringUtils.isNotBlank(originPipeline) && StringUtils.isNotBlank(originPhase)) {
                        Set<WrappedStep> stepSet = phaseRepo.get(StringUtils.join((Object[]) new String[]{originPipeline, ".", originPhase}));
                        PhaseDO originalPhase = getPhaseByName(originPipeline, originPhase, treeMultimap);
                        if (null != originalPhase) {
                            phaseDO.setParallel(originalPhase.isParallel());
                            phaseDO.setPoolSize(originalPhase.getPoolSize());
                            phaseDO.setQueueSize(originalPhase.getQueueSize());
                            phaseDO.setTimeOut(originalPhase.getTimeOut());
                            if (phaseDO.isParallel()) {
                                ExecutorService executorService = pipelineRepository.getThreadPool(StringUtils.join(originPipeline, ".", originPhase));
                                pipelineRepository.getThreadPoolMap().put(StringUtils.join(pipeline, ".", phaseDO.getName()), executorService);
                            }
                        } for (WrappedStep wrappedStep : stepSet) {
                            phaseRepo.put(pipeline + "." + phaseDO.getName(), wrappedStep);
                            stepMap.put(StringUtils.join((Object[]) new String[]{pipeline, ".", phaseDO.getName(), ".", wrappedStep.getStep().getClass().getSimpleName()}), wrappedStep);
                        } continue;
                    } if (referenceStepDOList != null && referenceStepDOList.size() > 0) {
                        for (ReferenceStepDO referenceStepDO : referenceStepDOList) {
                            Class stepClass = referenceStepDO.getOriginStep();
                            String stepName = stepClass.getSimpleName();
                            WrappedStep wrappedOriginalStep = stepMap.get(StringUtils.join((Object[]) new String[]{referenceStepDO.getOriginPipeline(), ".", referenceStepDO.getOriginPhase(), ".", stepName}));
                            if (wrappedOriginalStep != null) {
                                WrappedStep wrappedReferenceStep = new WrappedStep(wrappedOriginalStep.getStep(), referenceStepDO.getOrder());
                                phaseRepo.put(StringUtils.join((Object[]) new String[]{pipeline, ".", phaseDO.getName()} ), wrappedReferenceStep);
                                stepMap.put(StringUtils.join((Object[]) new String[]{pipeline, ".", phaseDO.getName(), ".", stepName}), wrappedReferenceStep);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("register pipeline error", e);

            throw e;
        }

        TreeMultimap<String, PhaseDO> pipelineRepo = this.registerFactory.getPipelineRegister().getPipelineRepository().getPipelineRepo();

        StringBuilder buffer = new StringBuilder(2000);
        if (pipelineRepo != null) {
            buffer.append("{\n");
            boolean isPipeline = true;
            for (String key : pipelineRepo.keySet()) {
                if (isPipeline) {
                    isPipeline = false;
                    buffer.append("   ");
                } else {
                    buffer.append(",\n\n   ");
                }
                buffer.append("\"").append(key).append("\":[\n");
                Set<PhaseDO> phaseDOSet = pipelineRepo.get(key);
                boolean isFirstPhase = true;
                for (PhaseDO phaseDO : phaseDOSet) {
                    if (isFirstPhase) {
                        isFirstPhase = false;
                        buffer.append("      ");
                    } else {
                        buffer.append(",\n      ");
                    }
                    buffer.append("{");
                    buffer.append("\"phaseName\":\"").append(phaseDO.getName()).append("\",");
                    buffer.append("\"order\":").append(phaseDO.getOrder()).append(",");
                    buffer.append("\"isParallel\":").append(phaseDO.isParallel()).append(",");
                    buffer.append("\"isRequired\":").append(phaseDO.isRequired()).append(",");
                    if (phaseDO.isParallel()) {
                        buffer.append("\"timeOut\":").append(phaseDO.getTimeOut()).append(",");
                        buffer.append("\"poolSize\":").append(phaseDO.getPoolSize()).append(",");
                        buffer.append("\"queueSize\":").append(phaseDO.getQueueSize()).append(",");
                    }
                    buffer.append("\n          \"steps\":[\n");
                    Set<WrappedStep> stepSet = this.registerFactory.getPipelineRegister().getPipelineRepository().getSteps(key + "." + phaseDO.getName());
                    boolean isFirstStep = true;
                    for (WrappedStep wrappedStep : stepSet) {
                        if (isFirstStep) {
                            isFirstStep = false;
                        } else {
                            buffer.append(",\n");
                        }
                        buffer.append("             {");
                        if (!phaseDO.isParallel()) {
                            buffer.append("\"order:\"").append(wrappedStep.getOrder()).append(",");
                        }
                        buffer.append("\"step:\"").append(wrappedStep.getStep().getClass().getCanonicalName()).append("\"");
                        buffer.append("}");
                    }
                    buffer.append("\n");
                    buffer.append("          ]\n");
                    buffer.append("      }");
                }
                buffer.append("\n");
                buffer.append("   ]");
            }
            buffer.append("\n");
            buffer.append("}\n");
        }


        LOGGER.info("registerBeans pipeline:\n" + buffer);
        System.out.println("registerBeans pipeline:\n" + buffer);

        Map<ExtensionCoordinate, IExtensionPoint> extensionRepo = this.registerFactory.getExtensionRegister().getExtensionRepository().getExtensionRepo();
        LOGGER.info("registerBeans extensions:\n" + extensionRepo);
    }


    /**
     * 获取所有需要扫描包路径下的类的class对象
     * @return class对象集合
     */
    private Set<Class<?>> scanConfiguredPackages() {
        if (this.packages == null) {
            throw new FrameworkException("Command packages is not specified");
        }

        String[] pkgs = new String[this.packages.size()];
        this.handler = new ClassPathScanHandler(this.packages.<String>toArray(pkgs));

        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : this.packages) {
            classSet.addAll(this.handler.getPackageAllClasses(pakName, true));
        }
        return classSet;
    }

    public List<String> getPackages() {
        return this.packages;
    }

    public void setPackages(List<String> packages) {
        if (packages == null || packages.isEmpty()) {
            this.packages = packages;
        }

        List<String> newPackages = new ArrayList<>();
        packages.forEach(packageName -> {
            if (StringUtils.isBlank(packageName)) {
                return;
            }

            String[] packageNames = packageName.split(",");
            newPackages.addAll(Lists.newArrayList((String[]) packageNames));
        });
        this.packages = newPackages;
    }
}



