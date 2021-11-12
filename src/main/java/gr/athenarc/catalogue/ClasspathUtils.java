package gr.athenarc.catalogue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(ClasspathUtils.class);

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
            logger.error(e);
        }
        return null;
    }

    private ClasspathUtils() {
    }
}
