package cn.xyf.framework.core.util;


public class Holder<T> {
    private volatile T value;
    private volatile boolean isInit;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    public boolean isInit() {
        return this.isInit;
    }

    public void setInit(boolean init) {
        this.isInit = init;
    }
}



