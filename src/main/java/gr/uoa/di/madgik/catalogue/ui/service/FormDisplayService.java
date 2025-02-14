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
package gr.uoa.di.madgik.catalogue.ui.service;

import gr.uoa.di.madgik.catalogue.ui.domain.Display;
import gr.uoa.di.madgik.catalogue.ui.domain.Form;
import gr.uoa.di.madgik.catalogue.ui.domain.UiFieldDisplay;
import gr.uoa.di.madgik.catalogue.ui.domain.UiFieldForm;

import java.util.List;
import java.util.Map;

public interface FormDisplayService {

    Form getForm(String modelId, String fieldId);
    UiFieldForm saveForm(String modelId, String fieldId, Form form);
    void deleteForm(String modelId, String fieldId);

    Display getDisplay(String modelId, String fieldId);
    UiFieldDisplay saveDisplay(String modelId, String fieldId, Display display);
    void deleteDisplay(String modelId, String fieldId);


    List<UiFieldForm> getForms();
    List<UiFieldDisplay> getDisplays();

    Map<String, Form> getUiFieldIdFormMap(String modelId);
    Map<String, Display> getUiFieldIdDisplayMap(String modelId);

}
