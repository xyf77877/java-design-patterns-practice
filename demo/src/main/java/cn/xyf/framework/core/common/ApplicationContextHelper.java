package cn.xyf.framework.core.common;

import cn.xyf.framework.core.exception.SysException;
import cn.xyf.framework.core.exception.framework.BasicErrorCode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 获取spring容器的工具类
 * 此工具将spring容器保存到静态变量中，并提供静态方法获取bean，简化了容器的依赖注入
 */
@Component
public class ApplicationContextHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    private static void setAC(ApplicationContext ac) {
        applicationContext = ac;
    }

    /**
     * spring 容器初始化时，会调用该方法，将当前容器实例注入进来
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setAC(applicationContext);
    }

    public static <T> T getBean(Class<T> targetClz) {
        T beanInstance = null;

        try {
            beanInstance = (T) applicationContext.getBean(targetClz);
        } catch (Exception exception) {
        }


        if (beanInstance == null) {
            String simpleName = targetClz.getSimpleName();

            simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
            beanInstance = (T) applicationContext.getBean(simpleName);
        }
        if (beanInstance == null) {
            throw new SysException(BasicErrorCode.FRAMEWORK_ERROR, "Component " + targetClz + " can not be found in Spring Container");
        }
        return beanInstance;
    }

    public static Object getBean(String claz) {
        return applicationContext.getBean(claz);
    }
}



