 package cn.xyf.framework.core.boot;

 import cn.xyf.framework.core.common.ApplicationContextHelper;
 import cn.xyf.framework.core.exception.framework.FrameworkException;
 import cn.xyf.framework.core.extension.*;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ExtensionRegister implements IRegister {
   @Autowired
   private ExtensionRepository extensionRepository;
   
   public void doRegistration(Class<?> targetClz) {
     IExtensionPoint extension = (IExtensionPoint) ApplicationContextHelper.getBean(targetClz);
     Extension extensionAnn = targetClz.<Extension>getDeclaredAnnotation(Extension.class);
     String extPtClassName = calculateExtensionPoint(targetClz);
     BizScenario bizScenario = BizScenario.valueOf(extensionAnn.tenant(), extensionAnn.business(), extensionAnn.partner());
     ExtensionCoordinate extensionCoordinate = new ExtensionCoordinate(extPtClassName, bizScenario.getUniqueIdentity());
     IExtensionPoint preVal = this.extensionRepository.getExtensionRepo().put(extensionCoordinate, extension);
     if (preVal != null) {
       throw new FrameworkException("Duplicate registration is not allowed for :" + extensionCoordinate);
     }
   }
 
 
 
 
   
   private String calculateExtensionPoint(Class<?> targetClz) {
     Class[] interfaces = targetClz.getInterfaces();
     if (ArrayUtils.isEmpty((Object[])interfaces)) {
       throw new FrameworkException("Please assign a extension point interface for " + targetClz);
     }
     for (Class intf : interfaces) {
       String extensionPoint = intf.getSimpleName();
       if (StringUtils.contains(extensionPoint, "ExtPt")) {
         return intf.getName();
       }
     } 
     throw new FrameworkException("Your name of ExtensionPoint for " + targetClz + " is not valid, must be end of " + "ExtPt");
   }
   
   public ExtensionRepository getExtensionRepository() {
     return this.extensionRepository;
   }
 }



