package cn.xyf.framework.core.concurrent;

import cn.xyf.framework.core.config.IConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.*;

@Service("tdframeworkThreadService")
public class ThreadService implements IThreadService {
    private static Logger logger = LoggerFactory.getLogger(ThreadService.class);

    private static final String TABLE = "forseti.thread.pool";
    private static final String prefix = "thread.pool.";
    private static final String corePoolSizeKey = "core";
    private static final String maximumPoolSizeKey = "max";
    private static final String keepAliveTimeKey = "alive";
    private static final String queueSizeKey = "queue";
    private static final String allowCoreThreadTimeOutKey = "allowCoreThreadTimeOut";
    private IConfigRepository configRepository;
    private Map<String, ThreadPoolExecutorProxy> pools = new ConcurrentHashMap<>();
    private Map<String, Integer> queueSizeMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduledFuture;


    /**
     * 自定义拒绝策略
     */
    private static final RejectedExecutionHandler defaultHandler = new RejectedExecutionHandler() {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
                if (ThreadService.logger.isWarnEnabled()) {
                    if (e.getThreadFactory() instanceof CustomizableThreadFactory) {
                        CustomizableThreadFactory factory = (CustomizableThreadFactory) e.getThreadFactory();
                        ThreadService.logger.warn("ThreadPool:" + factory.getThreadNamePrefix() + " is full,now execute ThreadPoolExecutor.CallerRunsPolicy!");
                    } else {
                        ThreadService.logger.warn("ThreadPool is full!");
                    }
                }
            }
        }
    };

    @PostConstruct
    public void init() {
        startScheduled();
    }


    public void startScheduled() {
    }


    private String getPropertityKey(String threadPoolName, String propertityName) {
        StringBuilder sb = new StringBuilder();
        sb.append("thread.pool.").append(threadPoolName).append(".").append(propertityName);
        return sb.toString();
    }


    public void setConfigRepository(IConfigRepository config) {
        this.configRepository = config;
    }


    public ExecutorService createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueSize, String threadPoolName) {
        return createThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueueSize, threadPoolName, defaultHandler, false);
    }


    public ExecutorService createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueSize, String threadPoolName, RejectedExecutionHandler handler) {
        return createThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueueSize, threadPoolName, handler, false);
    }


    public ExecutorService createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueSize, String threadPoolName, boolean allowCoreThreadTimeOut) {
        return createThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueueSize, threadPoolName, defaultHandler, allowCoreThreadTimeOut);
    }


    /**
     * 这里如果configRepository不为null，则使用configRepository的配置，否则使用传入配置
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueueSize
     * @param threadPoolName
     * @param handler
     * @param allowCoreThreadTimeOut
     * @return
     */
    public ExecutorService createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueSize, String threadPoolName, RejectedExecutionHandler handler, boolean allowCoreThreadTimeOut) {
        if (this.configRepository != null) {
            corePoolSize = this.configRepository.getIntProperty(getPropertityKey(threadPoolName, "core"), corePoolSize);
            maximumPoolSize = this.configRepository.getIntProperty(getPropertityKey(threadPoolName, "max"), maximumPoolSize);
            keepAliveTime = this.configRepository.getLongProperty(getPropertityKey(threadPoolName, "alive"), keepAliveTime);
            workQueueSize = this.configRepository.getIntProperty(getPropertityKey(threadPoolName, "queue"), workQueueSize);
            allowCoreThreadTimeOut = this.configRepository.getBooleanProperty(getPropertityKey(threadPoolName, "allowCoreThreadTimeOut"), allowCoreThreadTimeOut);
        }
        ThreadPoolExecutor pool = createThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueueSize, threadPoolName, handler);
        // 核心线程被回收的时间
        pool.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        // TODO 这里为什么要使用代理
        ThreadPoolExecutorProxy service = new ThreadPoolExecutorProxy(pool);
        this.pools.put(threadPoolName, service);

        if (this.configRepository != null) {
            Runnable task = new ThreadPoolAdjust(this.configRepository, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueueSize, threadPoolName, handler, allowCoreThreadTimeOut);
            this.configRepository.addCallback("thread.pool." + threadPoolName, task);
        }

        return service;
    }

    /**
     * 创建线程池
     *
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   非核心线程在空闲时存活的时间
     * @param unit            时间单位
     * @param workQueueSize   队列大小
     * @param threadPoolName  队列名称
     * @param handler         拒绝策略
     * @return 线程池
     */
    private ThreadPoolExecutor createThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueSize, String threadPoolName, RejectedExecutionHandler handler) {
        if (corePoolSize < 0) {
            corePoolSize = 0;
        }
        if (maximumPoolSize <= 0) {
            maximumPoolSize = 1;
        }
        if (maximumPoolSize < corePoolSize) {
            maximumPoolSize = corePoolSize;
        }
        if (keepAliveTime < 0L) {
            keepAliveTime = 0L;
        }
        if (workQueueSize < 0) {
            workQueueSize = 0;
        }

        String threadNamePrefix = threadPoolName;
        if (!threadNamePrefix.endsWith("-")) {
            threadNamePrefix = threadNamePrefix + "-";
        }

        BlockingQueue<Runnable> queue = null;
        // 工作队列小于等于0则创建同步队列，否则创建大小为workQueueSize的有界队列
        if (workQueueSize <= 0) {
            queue = new SynchronousQueue<>();
        } else {
            queue = new LinkedBlockingQueue<>(workQueueSize);
        }
        // 使用CustomizableThreadFactory自定义线程工厂自定义线程名称
        ThreadPoolExecutor service = new TdThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, queue, new CustomizableThreadFactory(threadNamePrefix), handler);
        // 缓存工作队列大小
        this.queueSizeMap.put(threadPoolName, workQueueSize);
        return service;
    }

    class ThreadPoolAdjust
            implements Runnable {
        private IConfigRepository configRepository;
        private int corePoolSize;
        private int maximumPoolSize;
        private long keepAliveTime;
        private TimeUnit unit;
        private int workQueueSize;
        private String threadPoolName;
        private RejectedExecutionHandler handler;
        private boolean allowCoreThreadTimeOut;

        public ThreadPoolAdjust(IConfigRepository configRepository, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueSize, String threadPoolName, RejectedExecutionHandler handler, boolean allowCoreThreadTimeOut) {
            this.configRepository = configRepository;
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
            this.unit = unit;
            this.workQueueSize = workQueueSize;
            this.threadPoolName = threadPoolName;
            this.handler = handler;
            this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        }


        public void run() {
            try {
                ThreadPoolExecutorProxy poolProxy = (ThreadPoolExecutorProxy) ThreadService.this.pools.get(this.threadPoolName);
                if (poolProxy == null) {
                    return;
                }

                int newCorePoolSize = this.configRepository.getIntProperty(ThreadService.this.getPropertityKey(this.threadPoolName, "core"), this.corePoolSize);
                int newMaximumPoolSize = this.configRepository.getIntProperty(ThreadService.this.getPropertityKey(this.threadPoolName, "max"), this.maximumPoolSize);
                long newKeepAliveTime = this.configRepository.getLongProperty(ThreadService.this.getPropertityKey(this.threadPoolName, "alive"), this.keepAliveTime);
                int newWorkQueueSize = this.configRepository.getIntProperty(ThreadService.this.getPropertityKey(this.threadPoolName, "queue"), this.workQueueSize);
                boolean newAllowCoreThreadTimeOut = this.configRepository.getBooleanProperty(ThreadService.this.getPropertityKey(this.threadPoolName, "allowCoreThreadTimeOut"), this.allowCoreThreadTimeOut);

                Integer oldWorkQueueSize = (Integer) ThreadService.this.queueSizeMap.get(this.threadPoolName);
                if (oldWorkQueueSize == null) {
                    oldWorkQueueSize = Integer.valueOf(-1);
                }


                if (newWorkQueueSize >= 0 && oldWorkQueueSize.intValue() != newWorkQueueSize) {
                    ThreadPoolExecutor oldThreadPoolExecutor = poolProxy.getThreadPoolExecutor();
                    ThreadPoolExecutor newThreadPoolExecutor = ThreadService.this.createThreadPoolExecutor(newCorePoolSize, newMaximumPoolSize, newKeepAliveTime, this.unit, newWorkQueueSize, this.threadPoolName, this.handler);
                    poolProxy.setThreadPoolExecutor(newThreadPoolExecutor);
                    oldThreadPoolExecutor.shutdown();

                    return;
                }
                if (newCorePoolSize >= 0 && poolProxy.getThreadPoolExecutor().getCorePoolSize() != newCorePoolSize) {
                    poolProxy.getThreadPoolExecutor().setCorePoolSize(newCorePoolSize);
                }
                if (newMaximumPoolSize > 0 && poolProxy.getThreadPoolExecutor().getMaximumPoolSize() != newMaximumPoolSize) {
                    poolProxy.getThreadPoolExecutor().setMaximumPoolSize(newMaximumPoolSize);
                }
                if (newKeepAliveTime > 0L && poolProxy.getThreadPoolExecutor().getKeepAliveTime(this.unit) != newKeepAliveTime) {
                    poolProxy.getThreadPoolExecutor().setKeepAliveTime(newKeepAliveTime, this.unit);
                }
                if (poolProxy.getThreadPoolExecutor().allowsCoreThreadTimeOut() != newAllowCoreThreadTimeOut) {
                    poolProxy.getThreadPoolExecutor().allowCoreThreadTimeOut(newAllowCoreThreadTimeOut);

                }
            } catch (Exception e) {
                ThreadService.logger.error("ThreadService ThreadPoolAdjust error", e);
            }
        }
    }


    public void resetMonitorInterval() {
        try {
            if (this.scheduledFuture != null) {
                this.scheduledFuture.cancel(true);
            }
            startScheduled();
        } catch (Exception e) {
            logger.error("ForsetiThreadService resetMonitorInterval error", e);
        }
    }
}



