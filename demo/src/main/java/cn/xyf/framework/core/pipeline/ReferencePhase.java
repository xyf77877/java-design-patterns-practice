package cn.xyf.framework.core.pipeline;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Component
// @Deprecated
public @interface ReferencePhase {
    String originPipeline() default "";

    String originPhase() default "";

    int order() default 1000;

    boolean required() default false;
}



