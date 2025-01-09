package cn.xyf.framework.core.config;

import java.util.List;
import java.util.Set;

public interface IConfigRepository {
    Object getProperty(String paramString);

    default boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }


    boolean getBooleanProperty(String paramString, boolean paramBoolean);


    default int getIntProperty(String name) {
        return getIntProperty(name, 0);
    }


    int getIntProperty(String paramString, int paramInt);


    default long getLongProperty(String name) {
        return getLongProperty(name, 0L);
    }


    long getLongProperty(String paramString, long paramLong);


    default String getStringProperty(String name) {
        return getStringProperty(name, "");
    }


    String getStringProperty(String paramString1, String paramString2);


    default Set<String> getSetProperty(String name) {
        return getSetProperty(name, null);
    }


    Set<String> getSetProperty(String paramString1, String paramString2);


    default List<String> getListProperty(String name) {
        return getListProperty(name, null);
    }

    List<String> getListProperty(String paramString1, String paramString2);

    void addCallback(String paramString, Runnable paramRunnable);

    void removeCallback(String paramString);
}



