package cn.xyf.framework.core.concurrent;

import java.util.concurrent.Callable;

public interface TaskWrapper {
  <T> Callable<T> wrap(Callable<T> paramCallable);
  
  Runnable wrapRun(Runnable paramRunnable);
}



