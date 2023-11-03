package gr.athenarc.catalogue.service;

import java.lang.reflect.InvocationTargetException;

public interface ResourcePayloadService {

    String getRaw(String resourceTypeName, String id);

    String addRaw(String resourceTypeName, String payload) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException;

    String updateRaw(String resourceTypeName, String id, String payload) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException;
}
