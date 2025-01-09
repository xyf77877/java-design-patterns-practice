package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.PipelineExecutor;
import cn.xyf.framework.core.pipeline.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AppMain {
    @Resource
    private PipelineExecutor pipelineExecutor;

    @GetMapping("test")
    public Response test() {
        Response execute = pipelineExecutor.execute(TestPipeline.NAME, ITestStep.class, step -> step.execute("执行"), (isSuc, e) -> isSuc == null || !isSuc);
        return execute;
    }
}
