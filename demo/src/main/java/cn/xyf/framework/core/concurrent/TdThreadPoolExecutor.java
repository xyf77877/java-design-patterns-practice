package cn.xyf.framework.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;


public class TdThreadPoolExecutor extends ThreadPoolExecutor {
    private static Logger logger = LoggerFactory.getLogger(TdThreadPoolExecutor.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();


    private static final String TABLE = "forseti.thread.pool";


    public TdThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }


    public TdThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }


    public TdThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    private void init() {
    }


    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        this.startTime.set(Long.valueOf(System.nanoTime()));
    }


    protected void afterExecute(Runnable r, Throwable t) {
        try {
            super.afterExecute(r, t);
        } catch (Exception e) {

            logger.error("ForsetiThreadPool error", e);
        }
    }


    protected void terminated() {
        try {
            String poolName = "";
            if (getThreadFactory() instanceof CustomizableThreadFactory) {
                poolName = ((CustomizableThreadFactory) getThreadFactory()).getThreadNamePrefix();
            }
            logger.info("ForsetiThreadPool poolName:{} terminated", poolName);
        } finally {
            super.terminated();
        }
    }
}



