package cn.xyf.framework.core.pipeline;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;


public class WrappedStep implements Comparable<WrappedStep> {
    private IStep step;
    private int order;
    private int currIndex;
    private static AtomicInteger counts = new AtomicInteger();

    public IStep getStep() {
        return this.step;
    }

    public int getOrder() {
        return this.order;
    }

    public int getCurrIndex() {
        return this.currIndex;
    }

    public WrappedStep(IStep step, int order) {
        this.step = step;
        this.order = order;
        this.currIndex = counts.incrementAndGet();
    }


    public int compareTo(WrappedStep o) {
        return Integer.compare((this.order + 1) * 10000 + this.currIndex, (o.getOrder() + 1) * 10000 + o.getCurrIndex());
    }


    public String toString() {
        return StringUtils.join((Object[]) new Serializable[]{"{\"step\":\"", (this.step != null) ? this.step.getClass().getCanonicalName() : null, "\",\"order\":", Integer.valueOf(this.order), "}"});
    }
}



