package cn.xyf.framework.test;

import cn.xyf.framework.core.pipeline.IStep;

public interface ITestStep extends IStep {
    boolean execute(String str);
}
