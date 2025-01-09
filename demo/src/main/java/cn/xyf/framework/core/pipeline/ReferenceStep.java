package cn.xyf.framework.core.pipeline;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Repeatable(ReferenceSteps.class)
public @interface ReferenceStep {
    String originPipeline() default "";

    String originPhase() default "";

    Class originStep();

    int order() default 1000;
}



