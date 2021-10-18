package gr.athenarc.catalogue.service;

public interface ResourcePayloadService {

    String getRaw(String resourceTypeName, String id);

    String addRaw(String resourceTypeName, String payload);

    String updateRaw(String resourceTypeName, String id, String payload) throws NoSuchFieldException;
}
