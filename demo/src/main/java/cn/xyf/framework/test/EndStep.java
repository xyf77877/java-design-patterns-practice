package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.Step;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Step(pipeline = TestPipeline.NAME, phase = TestPipeline.END)
public class EndStep implements ITestStep {
    public boolean execute(String str) {
        log.info("EndStep step execute:{}",str);
        return true;
    }
}
