package cn.xyf.framework.core.concurrent;

import cn.xyf.framework.core.config.IConfigRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

public interface IThreadService {
  void setConfigRepository(IConfigRepository paramIConfigRepository);
  
  ExecutorService createThreadPool(int paramInt1, int paramInt2, long paramLong, TimeUnit paramTimeUnit, int paramInt3, String paramString);
  
  ExecutorService createThreadPool(int paramInt1, int paramInt2, long paramLong, TimeUnit paramTimeUnit, int paramInt3, String paramString, RejectedExecutionHandler paramRejectedExecutionHandler);
  
  ExecutorService createThreadPool(int paramInt1, int paramInt2, long paramLong, TimeUnit paramTimeUnit, int paramInt3, String paramString, boolean paramBoolean);
  
  ExecutorService createThreadPool(int paramInt1, int paramInt2, long paramLong, TimeUnit paramTimeUnit, int paramInt3, String paramString, RejectedExecutionHandler paramRejectedExecutionHandler, boolean paramBoolean);
}



