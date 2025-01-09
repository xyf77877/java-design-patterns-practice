package cn.xyf.framework.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPoolExecutorProxy implements ExecutorService {
    private static Logger logger = LoggerFactory.getLogger(ThreadPoolExecutorProxy.class);


    private ThreadPoolExecutor threadPoolExecutor;

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();


    public ThreadPoolExecutorProxy(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }


    public ThreadPoolExecutorProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this.threadPoolExecutor = new TdThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }


    public ThreadPoolExecutorProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this.threadPoolExecutor = new TdThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return this.threadPoolExecutor;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }


    public void execute(Runnable command) {
        this.threadPoolExecutor.execute(command);
    }


    public void shutdown() {
        this.threadPoolExecutor.shutdown();
    }


    public List<Runnable> shutdownNow() {
        return this.threadPoolExecutor.shutdownNow();
    }


    public boolean isShutdown() {
        return this.threadPoolExecutor.isShutdown();
    }


    public boolean isTerminated() {
        return this.threadPoolExecutor.isTerminated();
    }


    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.threadPoolExecutor.awaitTermination(timeout, unit);
    }


    public <T> Future<T> submit(Callable<T> task) {
        return this.threadPoolExecutor.submit(task);
    }


    public <T> Future<T> submit(Runnable task, T result) {
        return this.threadPoolExecutor.submit(task, result);
    }


    public Future<?> submit(Runnable task) {
        return this.threadPoolExecutor.submit(task);
    }


    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.threadPoolExecutor.invokeAll(tasks);
    }


    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return this.threadPoolExecutor.invokeAll(tasks, timeout, unit);
    }


    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.threadPoolExecutor.invokeAny(tasks);
    }


    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.threadPoolExecutor.invokeAny(tasks, timeout, unit);
    }
}



