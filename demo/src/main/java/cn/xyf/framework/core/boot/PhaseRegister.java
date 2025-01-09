package cn.xyf.framework.core.boot;

import cn.xyf.framework.core.common.ApplicationContextHelper;
import cn.xyf.framework.core.config.IConfigRepository;
import cn.xyf.framework.core.pipeline.IStep;
import cn.xyf.framework.core.pipeline.PipelineRepository;
import cn.xyf.framework.core.pipeline.Step;
import cn.xyf.framework.core.pipeline.WrappedStep;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhaseRegister
        implements IRegister {
    private Logger logger = LoggerFactory.getLogger(PhaseRegister.class);

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired(required = false)
    private IConfigRepository configRepository;


    public void doRegistration(Class<?> targetClz) {
        // 获取bean对象
        Object bean = ApplicationContextHelper.getBean(targetClz);
        // bean对象必须实现IStep接口
        if (!(bean instanceof IStep)) {
            this.logger.error("Step not implements IStep:" + targetClz.getCanonicalName());
            return;
        }
        IStep step = (IStep) bean;
        Step stepAnn = targetClz.<Step>getDeclaredAnnotation(Step.class);
        String stepName = step.getClass().getSimpleName();
        // 可以动态禁用某个Step
        String propertyName = StringUtils.join((Object[]) new String[]{"pipeline.", stepAnn.pipeline(), ".", stepAnn.phase(), ".", stepName, ".disable"});
        String stepDisable = getProperty(propertyName);
        if (stepDisable != null && "true".equalsIgnoreCase(stepDisable)) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn(propertyName + "=true");
            }
            return;
        }
        WrappedStep wrappedStep = new WrappedStep(step, stepAnn.order());
        this.pipelineRepository.getPhaseRepo().put(stepAnn.pipeline() + "." + stepAnn.phase(), wrappedStep);
        this.pipelineRepository.getStepMap().put(StringUtils.join(stepAnn.pipeline(), ".", stepAnn.phase(), ".", stepName), wrappedStep);
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



