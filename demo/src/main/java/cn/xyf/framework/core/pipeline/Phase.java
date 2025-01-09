package cn.xyf.framework.core.pipeline;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Component
public @interface Phase {
    boolean parallel() default false;

    int timeOut() default 0;

    int poolSize() default 10;

    int queueSize() default 0;

    int order() default 1000;

    boolean required() default false;
}



