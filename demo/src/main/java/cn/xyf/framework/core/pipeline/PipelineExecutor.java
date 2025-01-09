package cn.xyf.framework.core.pipeline;

import cn.xyf.framework.core.exception.framework.FrameworkException;
import cn.xyf.framework.core.util.TaskWrapLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Pipeline执行入口
 */
@Component
public class PipelineExecutor {
    private Logger logger = LoggerFactory.getLogger(PipelineExecutor.class);

    @Autowired
    private PipelineRepository pipelineRepository;


    public <R, T> Response execute(String pipelineName, Class<T> targetClz, Function<T, R> function) {
        return execute(pipelineName, targetClz, function, null);
    }


    public <R, T> Response execute(String pipelineName, Class<T> targetClz, Function<T, R> function, BiFunction<R, Throwable, Boolean> terminatefunc) {
        // 根据pipeline name获取其下的所有phase NavigableSet是一个可排序的set接口，在TreeMultimap中是使用TreeSet实现的
        NavigableSet<PhaseDO> phases = this.pipelineRepository.getPipelineRepo().get(pipelineName);
        if (phases.isEmpty()) {
            throw new FrameworkException("step is empty for pipeline:" + pipelineName);
        }

        Response response = new Response();
        try {
            boolean terminate = false;
            for (PhaseDO phase : phases) {
                // 如果前一个phase执行失败，且当前phase为非必须，则跳过当前phase
                if (terminate && !phase.isRequired()) {
                    continue;
                }

                if (terminate) {
                    executePhase(pipelineName, phase, function, terminatefunc);
                    continue;
                }
                terminate = executePhase(pipelineName, phase, function, terminatefunc);
            }
            response.setSuccess(!terminate);
        } catch (Exception e) {
            response.setSuccess(false);
            this.logger.warn("pipeline:{}, error:", pipelineName, e);
            try {
                PhaseDO errorPhase = phases.last();
                if ("error".equals(errorPhase.getName())) {
                    executePhase(pipelineName, errorPhase, function, terminatefunc);
                }
            } catch (Exception e2) {
                this.logger.error("pipeline:{}, errorPhase error :", pipelineName, e2);
            }
        }
        return response;
    }


    private <R, T> boolean executePhase(String pipelineName, PhaseDO phase, Function<T, R> function, BiFunction<R, Throwable, Boolean> terminatefunc) {
        boolean terminate = false;
        // 获取当前phase下所有step的包装器对象
        Set<WrappedStep> wrappedSteps = this.pipelineRepository.getPhaseRepo().get(StringUtils.join(pipelineName, ".", phase.getName()));
        // 没有step就跳过此phase，执行下一个phase
        if (wrappedSteps.isEmpty()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("{}.{} have not steps", pipelineName, phase.getName());
            }
            return terminate;
        }

        if (phase.isParallel()) {// 并行
            terminate = parallel(pipelineName, phase, wrappedSteps, function, terminatefunc);
        } else {// 串行
            terminate = serial(pipelineName, phase, wrappedSteps, function, terminatefunc);
        }
        return terminate;
    }

    /**
     * 并行逻辑 （并行针对的是phase下的step并行）
     *
     * @param pipelineName  pipeline名称
     * @param phase         阶段对象
     * @param wrappedSteps  setp包装器集合
     * @param function      执行函数，用final修饰，防止并发问题
     * @param terminatefunc 是否终止执行的函数
     * @param <R>           step执行函数的返回值类型
     * @param <T>           执行函数的入参，即step的执行方法
     * @return 是否终止执行
     */
    private <R, T> boolean parallel(String pipelineName, PhaseDO phase, Set<WrappedStep> wrappedSteps, final Function<T, R> function, BiFunction<R, Throwable, Boolean> terminatefunc) {
        boolean terminate = false;
        // 获取当前phase对应的线程池
        ExecutorService executor = this.pipelineRepository.getThreadPool(StringUtils.join(pipelineName, ".", phase.getName()));
        if (executor == null) {
            throw new FrameworkException("Thread is null from pipeline" + pipelineName);
        }
        //
        List<Callable<R>> tasks = new ArrayList<>(wrappedSteps.size());
        List<IStep> stepList = new ArrayList<>(wrappedSteps.size());
        for (WrappedStep wrappedStep : wrappedSteps) {
            final IStep step = wrappedStep.getStep();
            stepList.add(step);
            tasks.add(TaskWrapLoader.getTaskWrapper().wrap(new Callable() {
                public Object call() throws Exception {
                    return executeStep(step, function);
                }
            }));
        }

        List<Future<R>> futures = null;
        Exception executorExc = null;
        try {
            if (phase.getTimeOut() <= 0) {
                futures = executor.invokeAll(tasks);
            } else {
                futures = executor.invokeAll(tasks, phase.getTimeOut(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            this.logger.warn("线程池执行中断", e);
            executorExc = e;
        } catch (RejectedExecutionException e) {
            this.logger.warn("线程池拒绝", e);
            executorExc = e;
        } catch (Exception e) {
            this.logger.warn("线程池异常", e);
            executorExc = e;
        }

        int size = 0;
        if (null != futures && !futures.isEmpty()) {
            size = futures.size();
            for (int i = 0; i < size; i++) {
                Future<R> future = futures.get(i);
                R result = null;
                Exception stepException = null;
                if (future.isDone() && !future.isCancelled()) {
                    try {
                        result = future.get();
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("pipeline:{}, phase:{},executeStep step:{} result:{}", new Object[]{pipelineName, phase
                                    .getName(), ((IStep) stepList.get(i)).getClass().getCanonicalName(), result});
                        }
                    } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                        this.logger.warn("数据获取失败,pipeline:{}, phase:{}", new Object[]{pipelineName, phase.getName(), e});
                        stepException = e;
                    } catch (Exception e) {
                        this.logger.warn("数据获取失败,pipeline:{}, phase:{}", new Object[]{pipelineName, phase.getName(), e});
                        stepException = e;
                    }
                } else {
                    stepException = (executorExc != null) ? executorExc : new CancellationException("task is cancelled");
                    if (this.logger.isWarnEnabled()) {
                        this.logger.warn("数据获取被cancel,pipeline:{}, phase:{}", pipelineName, phase.getName());
                    }
                }
                if (stepException != null) {
                    ((IStep) stepList.get(i)).errorHandle(stepException);
                }
                if (terminatefunc != null) {
                    try {
                        terminate = terminatefunc.apply(result, stepException);
                        if (terminate) {
                            if (this.logger.isDebugEnabled()) {
                                this.logger.debug("中断执行,pipeline:{}, phase:{},step:{},result:{},exception:{}", new Object[]{pipelineName, phase
                                        .getName(), ((IStep) stepList.get(i)).getClass().getCanonicalName(), result, stepException});
                            }
                            break;
                        }
                    } catch (Exception e) {
                        this.logger.error("判断是否中断执行时异常,pipeline:{}, phase:{}", new Object[]{pipelineName, phase.getName(), e});
                    }
                }
            }
        }
        return terminate;
    }

    /**
     * 串行逻辑
     *
     * @param pipelineName  pipeline名称
     * @param phase         阶段
     * @param wrappedSteps  step的包装器集合
     * @param function      step的执行函数
     * @param terminatefunc 是否终止执行的函数
     * @param <R>           step执行函数的返回值类型
     * @param <T>           执行函数的入参，即step的执行方法
     * @return 是否终止执行
     */
    private <R, T> boolean serial(String pipelineName, PhaseDO phase, Set<WrappedStep> wrappedSteps, Function<T, R> function, BiFunction<R, Throwable, Boolean> terminatefunc) {
        boolean terminate = false;
        for (WrappedStep wrappedStep : wrappedSteps) {
            // 定义step返回结果和异常，给BiFunction使用，用来判断是否中断pipeline的执行
            R result = null;
            Exception stepException = null;
            // 执行step
            try {
                result = executeStep(wrappedStep.getStep(), function);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("pipeline:{}, phase:{},executeStep step:{} result:{}", new Object[]{pipelineName, phase
                            .getName(), wrappedStep.getStep().getClass().getCanonicalName(), result});
                }
            } catch (Exception e) {
                wrappedStep.getStep().errorHandle(e);
                this.logger.error("pipeline:{}, phase:{},step执行失败,step:{}", pipelineName, phase
                        .getName(), wrappedStep.getStep().getClass().getCanonicalName(), e);
                stepException = e;
            }
            // 判断是否中断执行
            if (terminatefunc != null) {
                try {
                    // 执行传入的逻辑
                    terminate = terminatefunc.apply(result, stepException);
                    // 中断执行并打印日志
                    if (terminate) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("pipeline:{}, phase:{},中断执行,step:{},result:{},exception:{}", pipelineName, phase
                                    .getName(), wrappedStep.getStep().getClass().getSimpleName(), result, stepException == null ? null : stepException.getMessage());
                        }
                        break;
                    }
                } catch (Exception e) {// 中断逻辑异常
                    this.logger.error("pipeline:{}, phase:{},step:{},判断是否中断执行时异常", pipelineName, phase
                            .getName(), wrappedStep.getStep().getClass().getSimpleName(), e);
                }
            }
        }
        return terminate;
    }


    private <R, T> R executeStep(IStep step, Function<T, R> function) {
        return function.apply((T) step);
    }
}



