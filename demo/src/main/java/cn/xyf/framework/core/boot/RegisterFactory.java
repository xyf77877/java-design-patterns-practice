package cn.xyf.framework.core.boot;

import cn.xyf.framework.core.extension.Extension;
import cn.xyf.framework.core.pipeline.Pipeline;
import cn.xyf.framework.core.pipeline.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册器工厂类
 */
@Component
public class RegisterFactory {
    @Autowired
    private ExtensionRegister extensionRegister;
    @Autowired
    private PhaseRegister phaseRegister;
    @Autowired
    private PipelineRegister pipelineRegister;

    /**
     * 找到需要注册的类的注册器
     * @param targetClz
     * @return
     */
    public IRegister getRegister(Class<?> targetClz) {
        Extension extensionAnn = targetClz.<Extension>getDeclaredAnnotation(Extension.class);
        if (extensionAnn != null) {
            return this.extensionRegister;
        }
        // 获取传入类上的Step注解，返回Step注册器
        Step stepAnn = targetClz.<Step>getDeclaredAnnotation(Step.class);
        if (stepAnn != null) {
            return this.phaseRegister;
        }
        // 获取传入类上的Pipeline注解，返回Pipeline注册器
        Pipeline pipelineAnn = targetClz.<Pipeline>getDeclaredAnnotation(Pipeline.class);
        if (pipelineAnn != null) {
            return this.pipelineRegister;
        }
        return null;
    }

    public ExtensionRegister getExtensionRegister() {
        return this.extensionRegister;
    }

    public PhaseRegister getStepRegister() {
        return this.phaseRegister;
    }

    public PipelineRegister getPipelineRegister() {
        return this.pipelineRegister;
    }
}



