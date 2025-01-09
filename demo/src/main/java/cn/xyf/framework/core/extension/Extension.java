package cn.xyf.framework.core.extension;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Extension {
  String tenant() default "defaultTenant";
  
  String business() default "defaultBusiness";
  
  String partner() default "defaultPartner";
}



