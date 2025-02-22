package cn.xyf.framework.core.boot;

import java.util.Comparator;

public class ClassNameComparator implements Comparator<Class<?>> {
    public int compare(Class<?> o1, Class<?> o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}



