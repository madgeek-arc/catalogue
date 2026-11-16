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

package gr.uoa.di.madgik.catalogue.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The fields of a {@link Model} included in snippets, as a tree of field names, used to reduce payloads of the
 * model's resource type to snippets.
 * <p>
 * A field included in snippets is kept whole. A composite field that is not, but has sub-fields included in
 * snippets, keeps only those sub-fields. Every other field is dropped.
 */
public final class SnippetTemplate {

    /**
     * Child templates by field name; a {@code null} child keeps the field whole.
     */
    private final Map<String, SnippetTemplate> fields;

    private SnippetTemplate(Map<String, SnippetTemplate> fields) {
        this.fields = Collections.unmodifiableMap(fields);
    }

    /**
     * Builds the template of a model. Fields of its sections and sub-sections are fields of the payload root.
     */
    public static SnippetTemplate of(Model model) {
        Map<String, SnippetTemplate> fields = new LinkedHashMap<>();
        if (model.getSections() != null) {
            model.getSections().forEach(section -> addSection(section, fields));
        }
        return new SnippetTemplate(fields);
    }

    /**
     * @return {@code true} if the template keeps no field
     */
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    /**
     * Reduces a payload to the fields of the template. Composite values that are left without fields, and lists left
     * without elements, are dropped.
     *
     * @return a new map; the payload is not modified
     */
    public Map<String, Object> apply(Map<?, ?> payload) {
        Map<String, Object> snippet = new LinkedHashMap<>();
        fields.forEach((name, template) -> {
            if (!payload.containsKey(name)) {
                return;
            }
            Object value = payload.get(name);
            Object kept = template == null ? value : template.applyNested(value);
            if (template == null || kept != null) {
                snippet.put(name, kept);
            }
        });
        return snippet;
    }

    /**
     * @return the reduction of a composite value or of a list of them, or {@code null} if nothing is left
     */
    private Object applyNested(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> reduced = apply(map);
            return reduced.isEmpty() ? null : reduced;
        }
        if (value instanceof List<?> list) {
            List<Object> reduced = list.stream().map(this::applyNested).filter(Objects::nonNull).toList();
            return reduced.isEmpty() ? null : reduced;
        }
        return null;
    }

    private static void addSection(Section section, Map<String, SnippetTemplate> fields) {
        if (section.getFields() != null) {
            addFields(section.getFields(), fields);
        }
        if (section.getSubSections() != null) {
            section.getSubSections().forEach(subSection -> addSection(subSection, fields));
        }
    }

    private static void addFields(List<UiField> uiFields, Map<String, SnippetTemplate> fields) {
        for (UiField field : uiFields) {
            if (field.getName() == null) {
                continue;
            }
            if (field.isIncludedInSnippet()) {
                fields.put(field.getName(), null);
            } else if (field.getSubFields() != null && !fields.containsKey(field.getName())) {
                Map<String, SnippetTemplate> subFields = new LinkedHashMap<>();
                addFields(field.getSubFields(), subFields);
                if (!subFields.isEmpty()) {
                    fields.put(field.getName(), new SnippetTemplate(subFields));
                }
            }
        }
    }
}
