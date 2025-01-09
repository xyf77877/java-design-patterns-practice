package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.Step;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Step(pipeline = TestPipeline.NAME,phase = TestPipeline.START)
public class StartStep implements ITestStep {
    public boolean execute(String str) {
        int i = 1 / 0;
        log.info("start step execute:{}",str);
        return true;
    }
}
