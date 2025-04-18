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
package gr.uoa.di.madgik.catalogue.controller;

import gr.uoa.di.madgik.catalogue.config.CatalogueLibProperties;
import gr.uoa.di.madgik.catalogue.ui.domain.Model;
import gr.uoa.di.madgik.catalogue.service.ModelService;
import gr.uoa.di.madgik.catalogue.utils.ClasspathUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UiController {

    private final CatalogueLibProperties properties;


    public UiController(CatalogueLibProperties properties) {
        this.properties = properties;
    }


    @GetMapping(value = "ui/vocabularies/map", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<String>> getVocabularies() {
        return getEnumsMap(properties.getJaxb().getAllPackages());
    }

    private Map<String, List<String>> getEnumsMap(List<String> packageNames) {
        if (packageNames == null || packageNames.isEmpty()) {
            return null;
        }
        Map<String, List<String>> enumsMap = new HashMap<>();
        for (String packageName : packageNames) {
            Set<Class<?>> allClasses = ClasspathUtils.findAllEnums(packageName);
            for (Class<?> c : allClasses) {
                if (c.isEnum()) {
                    enumsMap.put(c.getSimpleName(), new ArrayList<>(Arrays.stream(c.getEnumConstants()).map(Object::toString).collect(Collectors.toList())));
                }
            }
        }
        return enumsMap;
    }
}
