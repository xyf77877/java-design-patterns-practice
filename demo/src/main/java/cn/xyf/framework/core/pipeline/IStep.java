package cn.xyf.framework.core.pipeline;

public interface IStep {
  default void errorHandle(Throwable e) {}
}



