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

import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.domain.Paging;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.exception.ResourceNotFoundException;
import gr.uoa.di.madgik.catalogue.domain.Model;
import gr.uoa.di.madgik.catalogue.domain.Section;
import gr.uoa.di.madgik.catalogue.domain.UiField;

import java.util.List;

/**
 * Service API for managing {@link Model} definitions and deriving Registry
 * {@link ResourceType} metadata from them.
 */
public interface ModelService {

    String MODEL_RESOURCE_TYPE_NAME = "model";

    /**
     * Persists a new model definition.
     *
     * @param model the model to create
     * @return the persisted model
     */
    Model add(Model model);

    /**
     * Updates an existing model definition by id.
     *
     * <p>Implementations may also trigger a refresh of the mapped Registry {@link ResourceType}
     * when the model declares one.</p>
     *
     * @param id the id of the model to update
     * @param model the updated model state
     * @return the updated model
     */
    Model update(String id, Model model);

    /**
     * Builds a Registry {@link ResourceType} definition from the provided model without
     * necessarily persisting it.
     *
     * @param model the source model definition
     * @return the generated resource type mapping
     */
    ResourceType generateResourceType(Model model);

    /**
     * Deletes the model with the given id.
     *
     * @param id the model id
     * @throws ResourceNotFoundException if no model exists with the given id
     */
    void delete(String id) throws ResourceNotFoundException;

    /**
     * Returns the model with the given id.
     *
     * @param id the model id
     * @return the matching model
     */
    Model get(String id);

    /**
     * Browses models using the provided Registry facet filter.
     *
     * @param filter paging, sorting, and filtering options
     * @return a paged result of models
     */
    Paging<Model> browse(FacetFilter filter);

    /**
     * Flattens all fields contained anywhere in the model hierarchy.
     *
     * @param model the source model
     * @return all fields from all sections and subsections
     */
    List<UiField> getAllFields(Model model);

    /**
     * Flattens all fields contained in a section, including nested subsections.
     *
     * @param section the source section
     * @return all fields reachable from the section
     */
    List<UiField> getSectionFields(Section section);

    /**
     * Recursively flattens a field list, including composite sub-fields.
     *
     * @param fields the root field list
     * @return all fields reachable from the provided list
     */
    List<UiField> getFieldsRecursive(List<UiField> fields);
}
