package gr.athenarc.catalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClasspathUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathUtils.class);

    public static <T> T[] toArray(Collection<T> collection) {
        T[] classes = null;
        if (!collection.isEmpty()) {
            classes = (T[]) Array.newInstance(collection.stream().findFirst().get().getClass(), collection.size());
            int i = 0;
            for (T item : collection) {
                classes[i++] = item;
            }
        }
        return classes;
    }

    public static Class<?>[] classesToArray(Collection<Class<?>> set) {
        Class<?>[] classes = new Class[set.size()];
        int i = 0;
        for (Class<?> item : set) {
            classes[i++] = item;
        }
        return classes;
    }

    public static Set<Class<?>> findAllEnums(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        return new HashSet<>(reflections.getSubTypesOf(Enum.class));
    }

    public static Set<Class<?>> findAllClasses(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    public static Set<Class<?>> getClassesWithoutInterfaces(String packageName) {
        Set<Class<?>> allClasses = ClasspathUtils.findAllClasses(packageName);
        logger.info("Classes found in '" + packageName + "': " + allClasses.size());
        Set<Class<?>> classes = new HashSet<>();
        for (Class<?> c : allClasses) {
            if (!c.isInterface()) {
                classes.add(c);
            }
        }
        return classes;
    }

    public static Set<Class<?>> getAllClasses(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> (Class<?>) getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private static Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private ClasspathUtils() {
    }
}
