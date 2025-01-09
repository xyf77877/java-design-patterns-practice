package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.Step;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Step(pipeline = TestPipeline.NAME, phase = TestPipeline.CHECK)
public class CheckStep implements ITestStep {
    public boolean execute(String str) {
        log.info("check step execute:{}", str);
        return true;
    }
}
