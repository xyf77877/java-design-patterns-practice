package cn.xyf.framework.core.pipeline;

import com.google.common.collect.TreeMultimap;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 存储pipeline和step的仓库
 */
@Component
public class PipelineRepository {

    /**
     * TreeMultimap是Guava提供的一个有序的Multimap，可以按照key进行排序，并且每个key可以对应多个值
     */
    private TreeMultimap<String, PhaseDO> pipelineRepo = TreeMultimap.create();
    private TreeMultimap<String, WrappedStep> phaseRepo = TreeMultimap.create();
    private Map<String, ExecutorService> threadPoolMap = new HashMap<>();
    private Map<String, WrappedStep> stepMap = new HashMap<>();

    public TreeMultimap<String, PhaseDO> getPipelineRepo() {
        return this.pipelineRepo;
    }

    public TreeMultimap<String, WrappedStep> getPhaseRepo() {
        return this.phaseRepo;
    }

    public Map<String, ExecutorService> getThreadPoolMap() {
        return this.threadPoolMap;
    }

    public Map<String, WrappedStep> getStepMap() {
        return this.stepMap;
    }

    public Set<PhaseDO> getPhases(String pipeline) {
        return this.pipelineRepo.get(pipeline);
    }

    public Set<WrappedStep> getSteps(String phasePath) {
        return this.phaseRepo.get(phasePath);
    }

    public ExecutorService getThreadPool(String phasePath) {
        return this.threadPoolMap.get(phasePath);
    }
}



