package gr.athenarc.catalogue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

public class ReflectUtils {

    private static final Logger logger = LogManager.getLogger(ReflectUtils.class);

    public static Class<?> getFieldClass(Object obj) {
        Field field = null;
        Object clazz = null;
        try {
            field = obj.getClass().getDeclaredField("declaredType");
            field.setAccessible(true);
            clazz = field.get(obj);
        } catch (NoSuchFieldException e) {
            logger.warn("Cannot find field 'declaredType'. Is the object an instance of a class from xsd2java package?", e);
        } catch (IllegalAccessException e) {
            logger.error(e);
        }
        return (Class<?>) clazz;
    }

    public static Object getFieldValue(String name, Object obj) {
        Field field = null;
        Object value = null;
        try {
            field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            value = field.get(obj);
        } catch (NoSuchFieldException e) {
            logger.warn("Cannot find field 'declaredType'. Is the object an instance of a class from xsd2java package?", e);
        } catch (IllegalAccessException e) {
            logger.error(e);
        }
        return value;
    }

    private ReflectUtils() {
    }
}
