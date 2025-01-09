package cn.xyf.framework.core.boot;

import cn.xyf.framework.core.extension.ExtensionCoordinate;
import cn.xyf.framework.core.extension.IBizScenario;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractComponentExecutor {
    public <R, T> R execute(Class<T> targetClz, IBizScenario bizScenario, Function<T, R> exeFunction) {
        T component = locateComponent(targetClz, bizScenario);
        return exeFunction.apply(component);
    }

    public <R, T> R execute(ExtensionCoordinate extensionCoordinate, Function<T, R> exeFunction) {
        return (R) execute(extensionCoordinate.getExtensionPointClass(), extensionCoordinate.getBizScenario(), exeFunction);
    }


    public <T> void executeVoid(Class<T> targetClz, IBizScenario context, Consumer<T> exeFunction) {
        T component = locateComponent(targetClz, context);
        exeFunction.accept(component);
    }

    public <T> void executeVoid(ExtensionCoordinate extensionCoordinate, Consumer<T> exeFunction) {
        executeVoid(extensionCoordinate.getExtensionPointClass(), extensionCoordinate.getBizScenario(), exeFunction);
    }

    protected abstract <C> C locateComponent(Class<C> paramClass, IBizScenario paramIBizScenario);
}



