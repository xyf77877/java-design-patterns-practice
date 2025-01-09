package cn.xyf.framework.core.pipeline;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阶段对象
 */
public class PhaseDO implements Comparable<PhaseDO> {
    private static AtomicInteger counts = new AtomicInteger();
    private int currIndex = counts.incrementAndGet();

    private String name;
    /**
     * 是否并行
     */
    private boolean parallel;
    /**
     * 超时时间
     */
    private int timeOut;
    /**
     * 线程池大小
     */
    private int poolSize;
    /**
     * 队列大小
     */
    private int queueSize;
    /**
     * 执行优先级
     */
    private int order;
    /**
     * 是否必须执行，为true时，不管前面的phase执行是否成功都会执行此phase
     */
    private boolean required;
    /**
     * 来源Pipeline
     */
    private String originPipeline;
    /**
     * 来源Phase
     */
    private String originPhase;
    /**
     * 引用的Step集合
     */
    private List<ReferenceStepDO> referenceStepDOList;


    public int compareTo(PhaseDO o) {
        return Integer.compare((this.order + 1) * 10000 + this.currIndex, (o.getOrder() + 1) * 10000 + o.getCurrIndex());
    }

    public int getCurrIndex() {
        return this.currIndex;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isParallel() {
        return this.parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public int getTimeOut() {
        return this.timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getPoolSize() {
        return this.poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getQueueSize() {
        return this.queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getOriginPipeline() {
        return this.originPipeline;
    }

    public void setOriginPipeline(String originPipeline) {
        this.originPipeline = originPipeline;
    }

    public String getOriginPhase() {
        return this.originPhase;
    }

    public void setOriginPhase(String originPhase) {
        this.originPhase = originPhase;
    }

    public List<ReferenceStepDO> getReferenceStepDOList() {
        return this.referenceStepDOList;
    }

    public void setReferenceStepDOList(List<ReferenceStepDO> referenceStepDOList) {
        this.referenceStepDOList = referenceStepDOList;
    }

    public String toString() {
        return StringUtils.join((Object[]) new Serializable[]{"{\"name\":\"", this.name, "\",\"parallel\":", Boolean.valueOf(this.parallel), ",\"timeOut\":", Integer.valueOf(this.timeOut), ",\"poolSize\":", Integer.valueOf(this.poolSize), ",\"queueSize\":", Integer.valueOf(this.queueSize), ",\"order\":", Integer.valueOf(this.order), ",\"required\":", Boolean.valueOf(this.required), "}"});
    }

    public static void main(String[] args) {
        new PriorityBlockingQueue<>(100, Comparator.comparing(PhaseDO::getName));
    }

}



