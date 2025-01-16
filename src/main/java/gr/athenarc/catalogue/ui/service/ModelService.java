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

package gr.athenarc.catalogue.ui.service;

import gr.uoa.di.madgik.registry.domain.Browsing;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;

import java.util.ArrayList;
import java.util.List;

public interface ModelService {

    String MODEL_RESOURCE_TYPE_NAME = "model";

    Model add(Model model);

    Model update(String id, Model model);

    void delete(String id) throws ResourceNotFoundException;

    Model get(String id);

    Browsing<Model> browse(FacetFilter filter);

    List<UiField> getAllFields(Model model);

    List<UiField> getSectionFields(Section section);

    List<UiField> getFieldsRecursive(List<UiField> fields);
}
