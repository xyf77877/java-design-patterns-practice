package cn.xyf.framework.core.pipeline;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ReferenceSteps {
    ReferenceStep[] value();
}



