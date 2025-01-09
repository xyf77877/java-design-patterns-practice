package cn.xyf.framework.core.util;

import cn.xyf.framework.core.concurrent.TaskWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TaskWrapLoader {
    private static Logger logger = LoggerFactory.getLogger(TaskWrapLoader.class);

    private static volatile TaskWrapper taskWrapper;

    public static TaskWrapper getTaskWrapper() {
        if (null != taskWrapper) {
            return taskWrapper;
        }
        synchronized (TaskWrapLoader.class) {
            if (null != taskWrapper) {
                return taskWrapper;
            }
            Class<?> clazz = TdFrameworkServiceLoader.getExtensionLoader(TaskWrapper.class).getExtension("taskWrapper");
            if (null == clazz) {
                throw new RuntimeException("TaskWrapLoader getTaskWrapper error!");
            }
            try {
                taskWrapper = (TaskWrapper) clazz.newInstance();
            } catch (Exception e) {
                logger.error("TaskWrapLoader getTaskWrapper error!");
                throw new RuntimeException("TaskWrapLoader getTaskWrapper error!", e);
            }
        }
        return taskWrapper;
    }
}



