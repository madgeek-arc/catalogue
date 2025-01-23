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

package gr.athenarc.catalogue.service;

import gr.uoa.di.madgik.registry.domain.Resource;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.service.*;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import gr.athenarc.catalogue.utils.LoggingUtils;
import gr.athenarc.catalogue.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.UUID;

@Service("catalogueGenericResourceService")
public class CatalogueGenericResourceService extends GenericResourceManager implements GenericResourceService, ResourcePayloadService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueGenericResourceService.class);

    public CatalogueGenericResourceService(SearchService searchService,
                                           ResourceService resourceService,
                                           ResourceTypeService resourceTypeService,
                                           VersionService versionService,
                                           ParserService parserPool) {
        super(searchService, resourceService, resourceTypeService, versionService, parserPool);
    }

    @Override
    public String getRaw(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id, true);
        return res.getPayload();
    }

    @Override
    public String addRaw(String resourceTypeName, String payload) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
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
        String id = ReflectUtils.getId(clazz, item);
        if (id == null) {
            id = UUID.randomUUID().toString();
            ReflectUtils.setId(clazz, item, id);
        }

        // return to Resource class and save
        payload = parserPool.serialize(item, ParserService.ParserServiceTypes.XML);
        res.setPayload(payload);
        logger.info(LoggingUtils.addResource(resourceTypeName, id, res));
        resourceService.addResource(res);
        return payload;
    }

    @Override
    public String updateRaw(String resourceTypeName, String id, String payload) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
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
