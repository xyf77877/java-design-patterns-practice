package cn.xyf.framework.core.concurrent;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

public class FrameworkTaskWrapper implements TaskWrapper {
    public <T> Callable<T> wrap(final Callable<T> t) {
        final Map mdcMap = MDC.getCopyOfContextMap();
        final ThreadContext threadContext = ThreadContext.getCopyOfContext();
        return new Callable<T>() {
            public T call() throws Exception {
                if (mdcMap != null) {
                    MDC.setContextMap(mdcMap);
                }
                ThreadContext.setContext(threadContext);
                try {
                    return (T) t.call();
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (null != mdcMap) {
                        MDC.clear();
                    }
                    ThreadContext.clear();
                }
            }
        };
    }


    public Runnable wrapRun(final Runnable t) {
        final Map mdcMap = MDC.getCopyOfContextMap();
        final ThreadContext threadContext = ThreadContext.getCopyOfContext();
        return new Runnable() {
            public void run() {
                if (mdcMap != null) {
                    MDC.setContextMap(mdcMap);
                }
                if (threadContext != null) {
                    ThreadContext.setContext(threadContext);
                }
                try {
                    t.run();
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (null != mdcMap) {
                        MDC.clear();
                    }
                    ThreadContext.clear();
                }
            }
        };
    }
}



