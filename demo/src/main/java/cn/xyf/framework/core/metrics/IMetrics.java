package cn.xyf.framework.core.metrics;

import java.util.Collection;

public interface IMetrics {
  void counter(String paramString, String... paramVarArgs);
  
  void counter(String paramString1, String paramString2, String... paramVarArgs);
  
  void summary(String paramString, long paramLong, String... paramVarArgs);
  
  ITimeContext timer(String paramString, String... paramVarArgs);
  
  ITimeContext metricTimer(String paramString, String... paramVarArgs);
  
  void gaugeCollectionSize(String paramString, Collection paramCollection);
}



