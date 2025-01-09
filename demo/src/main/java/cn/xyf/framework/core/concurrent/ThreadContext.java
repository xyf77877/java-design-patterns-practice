package cn.xyf.framework.core.concurrent;

import java.util.HashMap;
import java.util.Map;

public class ThreadContext {
    private static final ThreadLocal<ThreadContext> holder = new ThreadLocal<>();

    private Map<String, Object> attrs = new HashMap<>();


    public static final ThreadContext getContext() {
        ThreadContext context = holder.get();
        if (context == null) {
            context = new ThreadContext();
            holder.set(context);
        }
        return context;
    }

    public Object getAttr(String attrName) {
        return this.attrs.get(attrName);
    }

    public void setAttr(String attrName, Object value) {
        this.attrs.put(attrName, value);
    }

    public Map<String, Object> getAttrs() {
        return this.attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public static final void setContext(ThreadContext context) {
        holder.set(context);
    }

    public static final void clear() {
        holder.remove();
    }


    public static ThreadContext getCopyOfContext() {
        ThreadContext oldContext = getContext();
        ThreadContext newContext = new ThreadContext();
        newContext.setAttrs(oldContext.getAttrs());
        return newContext;
    }
}



