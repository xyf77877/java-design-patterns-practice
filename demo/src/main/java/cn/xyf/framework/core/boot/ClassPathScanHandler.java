package cn.xyf.framework.core.boot;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 *
 */
public class ClassPathScanHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathScanHandler.class);


    private static final String CLASS_EXTENSION_NAME = ".class";

    private boolean excludeInner = true;


    public boolean isExcludeInner() {
        return this.excludeInner;
    }

    public void setExcludeInner(boolean excludeInner) {
        this.excludeInner = excludeInner;
    }

    private boolean checkInOrEx = true;

    public boolean isCheckInOrEx() {
        return this.checkInOrEx;
    }

    public void setCheckInOrEx(boolean checkInOrEx) {
        this.checkInOrEx = checkInOrEx;
    }


    private List<String> classFilters = null;

    public List<String> getClassFilters() {
        return this.classFilters;
    }

    public void setClassFilters(List<String> classFilters) {
        this.classFilters = classFilters;
    }


    private Reflections reflections = null;

    public Reflections getReflections() {
        return this.reflections;
    }

    public void setReflections(Reflections reflections) {
        this.reflections = reflections;
    }


    public ClassPathScanHandler(String... packages) {
        this.reflections = new Reflections((new ConfigurationBuilder()).forPackages(packages).addScanners(new Scanner[]{(Scanner) new TypeAnnotationsScanner(), (Scanner) new SubTypesScanner()}));
    }


    public ClassPathScanHandler(Boolean excludeInner, Boolean checkInOrEx, List<String> classFilters) {
        this.excludeInner = excludeInner.booleanValue();
        this.checkInOrEx = checkInOrEx.booleanValue();
        this.classFilters = classFilters;
    }


    public Set<Class<?>> getAllClassesWithAnnotation(Class<? extends Annotation> annotation, boolean honorInherited) {
        return this.reflections.getTypesAnnotatedWith(annotation, honorInherited);
    }


    public <T> Set<Class<? extends T>> getAllSubClassesByParent(Class<T> parent) {
        return this.reflections.getSubTypesOf(parent);
    }


    public Set<Class<?>> getPackageAllClasses(String basePackage, boolean recursive) {
        if (basePackage == null) {
            return new HashSet<>();
        }
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageName = basePackage;
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        }
        String package2Path = packageName.replace('.', '/');

        try {
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(package2Path);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    LOGGER.debug("扫描file类型的class文件....");
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    doScanPackageClassesByFile(classes, packageName, filePath, recursive);
                    continue;
                }
                if ("jar".equals(protocol)) {
                    LOGGER.debug("扫描jar文件中的类....");
                    doScanPackageClassesByJar(packageName, url, recursive, classes);
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException error:");
        }

        TreeSet<Class<?>> sortedClasses = new TreeSet<>(new ClassNameComparator());
        sortedClasses.addAll(classes);
        return sortedClasses;
    }


    private void doScanPackageClassesByJar(String basePackage, URL url, boolean recursive, Set<Class<?>> classes) {
        String package2Path = basePackage.replace('.', '/');

        try {
            JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(package2Path) || entry.isDirectory()) {
                    continue;
                }

                if (!recursive && name.lastIndexOf('/') != package2Path.length()) {
                    continue;
                }

                if (this.excludeInner && name.indexOf('$') != -1) {
                    LOGGER.debug("exclude inner class with name:" + name);
                    continue;
                }
                String classSimpleName = name.substring(name.lastIndexOf('/') + 1);

                if (filterClassName(classSimpleName)) {
                    String className = name.replace('/', '.');
                    className = className.substring(0, className.length() - 6);
                    try {
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Class.forName error:URL is ===>" + url.getPath());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException error:URL is ===>" + url.getPath());
        } catch (Throwable e) {
            LOGGER.error("ScanPackageClassesByJar error:URL is ===>" + url.getPath());
        }
    }


    private void doScanPackageClassesByFile(Set<Class<?>> classes, String packageName, String packagePath, final boolean recursive) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return ClassPathScanHandler.this.filterClassFileByCustomization(pathname, recursive);
            }
        });

        if (null == files || files.length == 0) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes, packageName + "." + file.getName(), file.getAbsolutePath(), recursive);
            } else {
                String className = file.getName().substring(0, file.getName().length() - ".class".length());
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    LOGGER.error("IOException error:");
                }
            }
        }
    }


    private boolean filterClassFileByCustomization(@Nonnull File file, boolean recursive) {
        if (file.isDirectory()) {
            return recursive;
        }
        String filename = file.getName();
        if (this.excludeInner && filename.indexOf('$') != -1) {
            LOGGER.debug("exclude inner class with name:" + filename);
            return false;
        }
        return filterClassName(filename);
    }


    private boolean filterClassName(String className) {
        if (!className.endsWith(".class")) {
            return false;
        }
        if (null == this.classFilters || this.classFilters.isEmpty()) {
            return true;
        }
        String tmpName = className.substring(0, className.length() - 6);
        boolean flag = false;
        for (String str : this.classFilters) {
            flag = matchInnerClassname(tmpName, str);
            if (flag) {
                break;
            }
        }
        return ((this.checkInOrEx && flag) || (!this.checkInOrEx && !flag));
    }


    private boolean matchInnerClassname(String className, String filterString) {
        String reg = "^" + filterString.replace("*", ".*") + "$";
        Pattern p = Pattern.compile(reg);
        return p.matcher(className).find();
    }
}



