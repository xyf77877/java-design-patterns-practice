package cn.xyf.framework.core.pipeline;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Step {
    String pipeline() default "default";

    String phase();

    int order() default 1000;
}



