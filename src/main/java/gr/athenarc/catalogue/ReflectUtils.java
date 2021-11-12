package gr.athenarc.catalogue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;

public class ReflectUtils {

    private static final Logger logger = LogManager.getLogger(ReflectUtils.class);

    public static void setId(@NotNull Class<?> clazz, @NotNull Object resource, @NotNull String id) {
        try {
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(resource, id);
        } catch (NoSuchFieldException e) {
            logger.error("Could not find 'id' field in class [" + clazz.getName() + "]");
        } catch (IllegalAccessException e) {
            logger.error(e);
        }
    }

    public static String getId(@NotNull Class<?> clazz, @NotNull Object resource) throws NoSuchFieldException {
        String id = null;
        try {
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            id = (String) idField.get(resource);
        } catch (NoSuchFieldException e) {
            logger.error("Could not find 'id' field in class [" + clazz.getName() + "]");
            throw e;
        } catch (IllegalAccessException e) {
            logger.error(e);
        }
        return id;
    }

    private ReflectUtils() {}
}
