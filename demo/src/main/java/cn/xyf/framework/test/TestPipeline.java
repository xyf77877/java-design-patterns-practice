package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.Phase;
import cn.xyf.framework.core.pipeline.Pipeline;
import cn.xyf.framework.core.pipeline.Step;

@Pipeline(name = TestPipeline.NAME)
public class TestPipeline {

    public static final String NAME = "test";

    @Phase
    public static final String START = "start";

    @Phase(required = true,order = 1)
    public static final String CHECK = "check";

    @Phase
    public static final String EXECUTE = "execute";

    @Phase(required = true)
    public static final String TESTREQUIRED = "testrequired";

    @Phase
    public static final String END = "end";
}
