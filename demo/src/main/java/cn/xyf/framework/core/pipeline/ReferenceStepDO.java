package cn.xyf.framework.core.pipeline;

public class ReferenceStepDO {
    String originPipeline;
    String originPhase;
    Class originStep;
    int order;

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

    public Class getOriginStep() {
        return this.originStep;
    }

    public void setOriginStep(Class originStep) {
        this.originStep = originStep;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}



