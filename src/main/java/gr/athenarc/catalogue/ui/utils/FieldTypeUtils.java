package gr.athenarc.catalogue.ui.utils;

import java.util.Date;

public class FieldTypeUtils {

    public static String convertTypeToCanonicalClassName(String type) {
        Class<?> clazz;
        switch (type) {
            case "checkbox":
            case "vocabulary":
            case "select":
            case "string":
            case "email":
            case "phone":
            case "url":
                clazz = String.class;
                break;
            case "integer":
                clazz = Integer.class;
                break;
            case "long":
                clazz = Long.class;
                break;
            case "float":
                clazz = Float.class;
                break;
            case "number":
            case "double":
                clazz = Double.class;
                break;
            case "date":
                clazz = Date.class;
                break;
            case "composite":
            case "radioGrid":
            default:
                clazz = Object.class;
        }
        return clazz.getCanonicalName();
    }

    private FieldTypeUtils() {}
}
