/*
 * Copyright 2021-2026 OpenAIRE AMKE
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

package gr.uoa.di.madgik.catalogue.service;

import gr.uoa.di.madgik.catalogue.config.CatalogueLibProperties;
import gr.uoa.di.madgik.catalogue.domain.Model;
import gr.uoa.di.madgik.catalogue.domain.SnippetTemplate;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.domain.Resource;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.monitor.ResourceListener;
import gr.uoa.di.madgik.registry.service.ParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cached {@link SnippetTemplate}s of resource types, each built from the model that defines the resource type.
 * <p>
 * When several models define a resource type, the most recently modified one is used, then the one with the
 * smallest id. A resource type that no model defines is cached as such.
 * <p>
 * Model writes reported to registry resource listeners replace the templates of the resource types the model
 * defined before and after the write, built from the written model rather than from a search, which may not reflect
 * the write yet. Templates of other resource types are rebuilt on their next use. Every template is also rebuilt
 * once it is older than {@code catalogue-lib.model.snippet-template-ttl}, which bounds how long it can be stale after
 * model writes that bypass the listeners, such as restores, or that happen on another instance.
 */
public class SnippetTemplateCache implements ResourceListener {

    private static final Logger logger = LoggerFactory.getLogger(SnippetTemplateCache.class);

    private static final Comparator<Model> PREFERRED = Comparator
            .comparing(Model::getModificationDate, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Model::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final ModelService models;
    private final ParserService parser;
    private final CatalogueLibProperties properties;

    private final ConcurrentMap<String, Entry> templates = new ConcurrentHashMap<>();
    /**
     * Incremented on every model write; entries built under an earlier generation are rebuilt on their next use.
     * An entry records the generation read before its model was looked up, so a lookup that overlaps a write cannot
     * leave a template of the previous model in the cache.
     */
    private final AtomicLong generation = new AtomicLong();

    /**
     * @param template {@code null} when no model defines the resource type
     */
    private record Entry(SnippetTemplate template, Instant built, long generation) {
    }

    public SnippetTemplateCache(ModelService models, ParserService parser, CatalogueLibProperties properties) {
        this.models = models;
        this.parser = parser;
        this.properties = properties;
    }

    /**
     * @return the snippet template of the model defining the resource type, or empty if no model defines it
     */
    public Optional<SnippetTemplate> find(String resourceType) {
        Instant oldest = Instant.now().minus(properties.getModel().getSnippetTemplateTtl());
        Entry entry = templates.compute(resourceType, (type, cached) -> {
            if (cached != null && cached.generation() == generation.get() && cached.built().isAfter(oldest)) {
                return cached;
            }
            long current = generation.get();
            return entry(preferred(type, modelsDefining(type)), current);
        });
        return Optional.ofNullable(entry.template());
    }

    @Override
    public void resourceAdded(Resource resource) {
        if (isModel(resource)) {
            long current = generation.incrementAndGet();
            modelWritten(null, parse(resource), current);
        }
    }

    @Override
    public void resourceUpdated(Resource previousResource, Resource newResource) {
        if (isModel(previousResource) || isModel(newResource)) {
            long current = generation.incrementAndGet();
            modelWritten(isModel(previousResource) ? parse(previousResource) : null,
                    isModel(newResource) ? parse(newResource) : null, current);
        }
    }

    @Override
    public void resourceChangedType(Resource previousResource, Resource newResource,
                                    ResourceType previousResourceType, ResourceType resourceType) {
        if (isModel(previousResourceType) || isModel(resourceType)) {
            generation.incrementAndGet();
        }
    }

    @Override
    public void resourceDeleted(Resource resource) {
        if (isModel(resource)) {
            long current = generation.incrementAndGet();
            modelWritten(parse(resource), null, current);
        }
    }

    /**
     * Replaces the templates of the resource types a model defined before and after a write.
     *
     * @param before the model before the write, or {@code null} if it was added
     * @param after  the model after the write, or {@code null} if it was deleted
     * @param current the generation the write started
     */
    private void modelWritten(Model before, Model after, long current) {
        String id = after != null ? after.getId() : before != null ? before.getId() : null;
        Set<String> types = new LinkedHashSet<>();
        if (before != null && before.getResourceType() != null) {
            types.add(before.getResourceType());
        }
        if (after != null && after.getResourceType() != null) {
            types.add(after.getResourceType());
        }
        for (String type : types) {
            templates.compute(type, (t, cached) -> {
                List<Model> candidates = new ArrayList<>(modelsDefining(t).stream()
                        .filter(model -> !Objects.equals(model.getId(), id))
                        .toList());
                if (after != null && t.equals(after.getResourceType())) {
                    candidates.add(after);
                }
                return entry(preferred(t, candidates), current);
            });
        }
    }

    private List<Model> modelsDefining(String resourceType) {
        FacetFilter filter = new FacetFilter();
        filter.addFilter("resourceType", resourceType);
        filter.setQuantity(1_000);
        return models.browse(filter).getResults();
    }

    private static Model preferred(String resourceType, List<Model> candidates) {
        if (candidates.size() > 1) {
            logger.warn("{} models define resource type '{}'; using the most recently modified", candidates.size(),
                    resourceType);
        }
        return candidates.stream().min(PREFERRED).orElse(null);
    }

    private static Entry entry(Model model, long generation) {
        return new Entry(model != null ? SnippetTemplate.of(model) : null, Instant.now(), generation);
    }

    private Model parse(Resource resource) {
        return parser.deserialize(resource, Model.class);
    }

    private static boolean isModel(Resource resource) {
        return resource != null && ModelService.MODEL_RESOURCE_TYPE_NAME.equals(resource.getResourceTypeName());
    }

    private static boolean isModel(ResourceType resourceType) {
        return resourceType != null && ModelService.MODEL_RESOURCE_TYPE_NAME.equals(resourceType.getName());
    }
}
