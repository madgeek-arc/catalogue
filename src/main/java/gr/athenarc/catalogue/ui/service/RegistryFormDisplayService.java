/**
 * Copyright 2021-2024 OpenAIRE AMKE
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

import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.ui.domain.Display;
import gr.athenarc.catalogue.ui.domain.Form;
import gr.athenarc.catalogue.ui.domain.UiFieldDisplay;
import gr.athenarc.catalogue.ui.domain.UiFieldForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RegistryFormDisplayService implements FormDisplayService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryFormDisplayService.class);

    private final GenericItemService genericItemService;

    public RegistryFormDisplayService(GenericItemService genericItemService) {
        this.genericItemService = genericItemService;
    }

    @Override
    public Form getForm(String fieldId, String modelId) {
        return genericItemService.get(camelCaseToSnakeCase(Form.class.getName()), fieldId);
    }

    @Override
    public UiFieldForm saveForm(String fieldId, String modelId, Form form) {
        return null;
    }

    @Override
    public void deleteForm(String fieldId, String modelId) {

    }

    @Override
    public Display getDisplay(String fieldId, String modelId) {
        return null;
    }

    @Override
    public UiFieldDisplay saveDisplay(String fieldId, String modelId, Display display) {
        return null;
    }

    @Override
    public void deleteDisplay(String fieldId, String modelId) {

    }

    @Override
    public List<UiFieldForm> getForms() {
        return null;
    }

    @Override
    public List<UiFieldDisplay> getDisplays() {
        return null;
    }

    @Override
    public Map<String, Form> getUiFieldIdFormMap(String modelId) {
        return null;
    }

    @Override
    public Map<String, Display> getUiFieldIdDisplayMap(String modelId) {
        return null;
    }

    public static String camelCaseToSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
