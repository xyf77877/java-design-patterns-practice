package cn.xyf.framework.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class TdFrameworkServiceLoader {
    private static Logger logger = LoggerFactory.getLogger(TdFrameworkServiceLoader.class);


    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final String PLATFORM_DIRECTORY = "META-INF/platform/";

    private static final String TDFRAME_DIRECTORY = "META-INF/tdframework/";

    private static final ConcurrentHashMap<Class<?>, TdFrameworkServiceLoader> LOADERS = new ConcurrentHashMap<>();


    private final ConcurrentMap<String, Holder<Object>> cachedClasses = new ConcurrentHashMap<>();


    private static final String EXTENSION = "extension";


    private static final String INTERNAL = "internal";


    private static final String PLATFORM = "platform";


    private Class<?> type;


    public static TdFrameworkServiceLoader getExtensionLoader(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        TdFrameworkServiceLoader loader = LOADERS.get(type);
        if (loader == null) {
            LOADERS.putIfAbsent(type, new TdFrameworkServiceLoader(type));
            loader = LOADERS.get(type);
        }
        return loader;
    }

    private TdFrameworkServiceLoader(Class<?> type) {
        this.type = type;
    }


    public Class<?> getExtension(String classType) {
        if (StringUtils.isBlank(classType)) {
            return null;
        }
        Map<String, Class<?>> extensionMap = getExtensionClassInnerOutter("extension");
        if (null != extensionMap && extensionMap.containsKey(classType)) {
            return extensionMap.get(classType);
        }
        Map<String, Class<?>> baselineMap = getExtensionClassInnerOutter("platform");
        if (null != baselineMap && baselineMap.containsKey(classType)) {
            return baselineMap.get(classType);
        }
        Map<String, Class<?>> innerMap = getExtensionClassInnerOutter("internal");
        return innerMap.get(classType);
    }


    private Map<String, Class<?>> getExtensionClassInnerOutter(String type) {
        Holder<Object> holder = this.cachedClasses.get(type);
        if (holder == null) {
            this.cachedClasses.putIfAbsent(type, new Holder());
            holder = this.cachedClasses.get(type);
        }
        Map<String, Class<?>> instances = (Map<String, Class<?>>) holder.get();
        if (null == instances) {
            synchronized (holder) {
                instances = (Map<String, Class<?>>) holder.get();
                if (null == instances) {
                    instances = getExtensionClasses(type);
                    holder.set(instances);
                }
            }
        }
        return instances;
    }


    private Map<String, Class<?>> getExtensionClasses(String type) {
        Holder<Object> holder = this.cachedClasses.get(type);
        if (null == holder) {
            this.cachedClasses.putIfAbsent(type, new Holder());
            holder = this.cachedClasses.get(type);
        }
        Map<String, Class<?>> classes = (Map<String, Class<?>>) holder.get();
        if (null == classes) {
            synchronized (holder) {
                classes = (Map<String, Class<?>>) holder.get();
                if (null == classes) {
                    classes = loadClasses(type);
                    holder.set(classes);
                }
            }
        }
        return classes;
    }


    private Map<String, Class<?>> loadClasses(String type) {
        String dir = "META-INF/services/";
        if (StringUtils.equalsIgnoreCase(type, "internal")) {
            dir = "META-INF/tdframework/";
        } else if (StringUtils.equalsIgnoreCase(type, "platform")) {
            dir = "META-INF/platform/";
        }
        Map<String, Class<?>> classList = new HashMap<>();
        loadFile(classList, dir);

        return classList;
    }

    private ClassLoader findClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + this.type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                int ci = line.indexOf('#');
                                if (ci >= 0) {
                                    line = line.substring(0, ci);
                                }
                                line = line.trim();
                                if (line.length() > 0) {
                                    try {
                                        if (line.length() > 0) {

                                            int equalSplitIndex = line.indexOf('=');
                                            if (equalSplitIndex <= 0) {
                                                throw new IllegalStateException("Error when load extension class(interface: " + this.type + ", class line: " + line + "),no type exist!");
                                            }

                                            String classType = line.substring(0, equalSplitIndex);
                                            String classString = line.substring(equalSplitIndex + 1);
                                            if (StringUtils.isBlank(classString)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " + this.type + ", class line: " + line + "),no class exist!");
                                            }


                                            Class<?> clazz = Class.forName(classString, true, classLoader);
                                            if (!this.type.isAssignableFrom(clazz)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " + this.type + ", class line: " + clazz.getName() + "), class " + clazz.getName() + "is not subtype of interface.");
                                            }
                                            extensionClasses.put(classType, clazz);
                                        }
                                    } catch (Throwable t) {
                                        logger.error("Failed to load extension class(interface: " + this.type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                    }
                                }
                            }
                        } finally {
                            reader.close();
                        }
                    } catch (Throwable t) {
                        logger.error("Exception when load extension class(interface: " + this.type + ", class file: " + url + ") in " + url, t);
                    }

                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " + this.type + ", description file: " + fileName + ").", t);
        }
    }
}



