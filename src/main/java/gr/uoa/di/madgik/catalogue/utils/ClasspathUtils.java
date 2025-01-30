/**
 * Copyright 2021-2025 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.uoa.di.madgik.catalogue.utils;

import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    public static Set<Class<?>> filterOutInterfaces(Set<Class<?>> classes) {
        Set<Class<?>> nonInterfaces = new HashSet<>();
        for (Class<?> c : classes) {
            if (!c.isInterface()) {
                nonInterfaces.add(c);
            }
        }
        return nonInterfaces;
    }

    public static Set<Class<?>> findAllEnums(String packageName) {
        return new HashSet<>(getSubclassesUsingReflections(packageName, Enum.class));
    }

    public static Set<Class<?>> findAllClasses(String packageName) {
        return new HashSet<>(getSubclassesUsingReflections(packageName, Object.class));
    }

    public static Set<Class<?>> getClassesWithoutInterfaces(String packageName) {
        return getClassesWithoutInterfaces(List.of(packageName));
    }

    public static Set<Class<?>> getClassesWithoutInterfaces(List<String> packageNames) {

        Set<Class<?>> allClasses = new HashSet<>();
        for (String packageName : packageNames) {
            Set<Class<?>> classes = filterOutInterfaces(ClasspathUtils.findAllClasses(packageName));
            logger.info("Classes found in '{}': {}", packageName, classes.size());
            if (logger.isDebugEnabled()) {
                classes.forEach(c -> logger.debug(" - {}", c.getCanonicalName()));
            }
            allClasses.addAll(classes);
        }

        return allClasses;
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

    private static Set<Class<?>> getSubclassesUsingReflections(String packageName, Class superClass) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Set<Class<?>> classes;
        try {
            classes = reflections.getSubTypesOf(superClass);
        } catch (ReflectionsException e) {
            classes = new HashSet<>();
            logger.warn("Package '{}' does not contain classes", packageName);
        }
        return classes;
    }

    private ClasspathUtils() {
    }
}
