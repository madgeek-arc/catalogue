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

public interface ModelService {

    String MODEL_RESOURCE_TYPE_NAME = "model";

    Model add(Model model);

    Model update(String id, Model model);

    ResourceType createResourceType(Model model);

    void updateResourceType(Model model);

    void delete(String id) throws ResourceNotFoundException;

    Model get(String id);

    Paging<Model> browse(FacetFilter filter);

    List<UiField> getAllFields(Model model);

    List<UiField> getSectionFields(Section section);

    List<UiField> getFieldsRecursive(List<UiField> fields);
}
