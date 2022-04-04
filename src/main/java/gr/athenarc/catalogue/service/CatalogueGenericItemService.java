package gr.athenarc.catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.domain.ResourceType;
import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.LoggingUtils;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.exception.ResourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service("catalogueGenericItemService")
public class CatalogueGenericItemService extends AbstractGenericItemService implements GenericItemService, ResourcePayloadService {

    private static final Logger logger = LogManager.getLogger(CatalogueGenericItemService.class);

    @Autowired
    public CatalogueGenericItemService(SearchService searchService,
                                       ResourceService resourceService,
                                       ResourceTypeService resourceTypeService,
                                       ParserService parserPool) {
        super(searchService, resourceService, resourceTypeService, parserPool);
    }

    @Override
    public String getRaw(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id, true);
        return res.getPayload();
    }

    @Override
    public String addRaw(String resourceTypeName, String payload) {
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        payload = payload.replaceAll("[\n\t]", "");

        Resource res = new Resource();
        res.setResourceTypeName(resourceTypeName);
        res.setResourceType(resourceType);
        Date now = new Date();
        res.setCreationDate(now);
        res.setModificationDate(now);
        res.setPayload(payload);
        res.setPayloadFormat(resourceType.getPayloadType());

        // create Java class and set ID using reflection
        Object item = parserPool.deserialize(res, clazz);
        String id = UUID.randomUUID().toString();
        ReflectUtils.setId(clazz, item, id);

        // return to Resource class and save
        payload = parserPool.serialize(item, ParserService.ParserServiceTypes.XML);
        res.setPayload(payload);
        logger.info(LoggingUtils.addResource(resourceTypeName, id, res));
        resourceService.addResource(res);
        return payload;
    }

    @Override
    public String updateRaw(String resourceTypeName, String id, String payload) throws NoSuchFieldException {
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        payload = payload.replaceAll("[\n\t]", "");

        // FIXME: following converts String payload to class, just to get if id exists...
        Resource resource = new Resource();
        resource.setPayload(payload);
        resource.setPayloadFormat(resourceTypeService.getResourceType(resourceTypeName).getPayloadType());
        Object item = parserPool.deserialize(resource, clazz);

        String existingId = ReflectUtils.getId(clazz, item);
        if (!id.equals(existingId)) {
            throw new ResourceException("Resource body id different than path id", HttpStatus.CONFLICT);
        }
        Resource res = this.searchResource(resourceTypeName, id, true);
        Date now = new Date();
        res.setModificationDate(now);
        res.setPayload(payload);

        // save resource
        logger.info(LoggingUtils.updateResource(resourceTypeName, id, res));
        res = resourceService.updateResource(res);
        return res.getPayload();
    }
}
