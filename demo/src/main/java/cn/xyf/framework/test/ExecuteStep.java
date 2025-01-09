package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.Step;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Step(pipeline = TestPipeline.NAME, phase = TestPipeline.EXECUTE)
public class ExecuteStep implements ITestStep {
    public boolean execute(String str) {
        log.info("ExecuteStep execute:{}",str);
        return true;
    }
}
