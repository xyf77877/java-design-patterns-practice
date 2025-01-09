package cn.xyf.framework.core.boot;

import cn.xyf.framework.core.common.ApplicationContextHelper;
import cn.xyf.framework.core.common.Constant;
import cn.xyf.framework.core.concurrent.IThreadService;
import cn.xyf.framework.core.config.IConfigRepository;
import cn.xyf.framework.core.exception.framework.FrameworkException;
import cn.xyf.framework.core.pipeline.*;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 流水线注册器
 */
@Component
public class PipelineRegister implements IRegister {
    private final Logger logger = LoggerFactory.getLogger(PipelineRegister.class);


    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private IThreadService threadService;

    /**
     * 由客户端实现此配置（可决定是否禁用Phase）
     */
    @Autowired(required = false)
    private IConfigRepository configRepository;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 30L;


    public void doRegistration(Class<?> targetClz) {
        // 获取bean对象
        Object bean = ApplicationContextHelper.getBean(targetClz);
        // 获取对象上的Pipeline注解
        Pipeline pipelineAnn = targetClz.<Pipeline>getDeclaredAnnotation(Pipeline.class);
        // 获取类的所有属性
        Field[] fields = targetClz.getDeclaredFields();
        if (fields.length == 0) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("PipelineRegister have not phases for " + pipelineAnn.name());
            }
            return;
        }
        for (Field field : fields) {
            // 获取属性上的注解（Phase和ReferencePhase只能存在一个）
            Phase phaseAnn = field.<Phase>getAnnotation(Phase.class);
            ReferencePhase referencePhaseAnn = field.<ReferencePhase>getAnnotation(ReferencePhase.class);
            if (null != phaseAnn || null != referencePhaseAnn) {
                if (phaseAnn != null && referencePhaseAnn != null) {
                    String errorMsg = String.format("PipelineRegister register phase error,@Phase and @ReferencePhase only one can be annotation,pipeline:%s, phase:%s", new Object[]{pipelineAnn.name(), field.getName()});
                    throw new FrameworkException(errorMsg);
                }
                PhaseDO phaseDO = new PhaseDO();
                String phaseName = null;
                try {
                    // 设置属性可访问
                    field.setAccessible(true);
                    // 获取字段值
                    phaseName = String.valueOf(field.get(bean));
                    phaseDO.setName(phaseName);
                } catch (Exception e) {
                    this.logger.warn("PipelineRegister get field value error", e);
                }
                if (phaseAnn != null) {
                    // 设置注解属性
                    phaseDO.setParallel(phaseAnn.parallel());
                    phaseDO.setTimeOut(phaseAnn.timeOut());
                    phaseDO.setPoolSize(phaseAnn.poolSize());
                    phaseDO.setQueueSize(phaseAnn.queueSize());
                    phaseDO.setOrder(phaseAnn.order());
                    phaseDO.setRequired(phaseAnn.required());
                    // 如果是并行，就创建线程池，线程池参数使用用户在注解中配置的参数，线程池是Phase级别的，每个Phase都会创建一个线程池
                    if (phaseAnn.parallel()) {
                        createThreadPool(StringUtils.join(pipelineAnn.name(), ".", phaseName), phaseAnn.poolSize(), phaseAnn.queueSize());
                    }
                    // 获取所有ReferenceStep
                    ReferenceStep[] referenceStepAnns = field.<ReferenceStep>getAnnotationsByType(ReferenceStep.class);
                    List<ReferenceStepDO> referenceStepDOList = new ArrayList<>(referenceStepAnns.length);
                    for (ReferenceStep stepAnn : referenceStepAnns) {
                        ReferenceStepDO referenceStepDO = new ReferenceStepDO();
                        referenceStepDO.setOriginPipeline(stepAnn.originPipeline());
                        referenceStepDO.setOriginPhase(stepAnn.originPhase());
                        referenceStepDO.setOriginStep(stepAnn.originStep());
                        referenceStepDO.setOrder(stepAnn.order());
                        referenceStepDOList.add(referenceStepDO);
                    }
                    phaseDO.setReferenceStepDOList(referenceStepDOList);
                } else {
                    phaseDO.setOriginPipeline(referencePhaseAnn.originPipeline());
                    phaseDO.setOriginPhase(referencePhaseAnn.originPhase());
                    phaseDO.setOrder(referencePhaseAnn.order());
                    phaseDO.setRequired(referencePhaseAnn.required());
                }
                // 可以动态禁用某个Phase
                String propertyName = StringUtils.join((Object[]) new String[]{"pipeline.", pipelineAnn.name(), ".", phaseName, ".disable"});
                String phaseDisable = getProperty(propertyName);
                if ("true".equalsIgnoreCase(phaseDisable)) {
                    if (this.logger.isWarnEnabled()) {
                        this.logger.warn(propertyName + "=true");
                    }
                } else {
                    if ("error".equals(phaseName)) {
                        phaseDO.setOrder(Integer.MAX_VALUE);
                    }
                    // 添加到流水线仓库，key为pipeline.name，value为PhaseDO
                    pipelineRepository.getPipelineRepo().put(pipelineAnn.name(), phaseDO);
                }
            }
        }
        // NavigableSet继承自SortedSet，是一个可排序的set接口，在TreeMultimap中是使用TreeSet实现的
        NavigableSet<PhaseDO> phases = pipelineRepository.getPipelineRepo().get(pipelineAnn.name());
        if (!phases.isEmpty() && "error".equals(((PhaseDO) phases.last()).getName())) {
            return;
        }
        PhaseDO errorPhase = new PhaseDO();
        errorPhase.setName("error");
        errorPhase.setParallel(false);
        errorPhase.setOrder(Integer.MAX_VALUE);
        pipelineRepository.getPipelineRepo().put(pipelineAnn.name(), errorPhase);
    }

    private void createThreadPool(String phasePath, int poolSize, int queueSize) {
        if (poolSize <= 0) {
            poolSize = Constant.THREAD_POOL_SIZE;
        }

        ExecutorService threadPool = this.threadService.createThreadPool(poolSize, poolSize, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.MINUTES, queueSize, phasePath + "-");
        // 在pipelineRepository中缓存线程池
        this.pipelineRepository.getThreadPoolMap().put(phasePath, threadPool);
    }

    private String getProperty(String name) {
        if (this.configRepository == null) {
            return null;
        }
        return this.configRepository.getStringProperty(name);
    }


    public PipelineRepository getPipelineRepository() {
        return this.pipelineRepository;
    }
}



