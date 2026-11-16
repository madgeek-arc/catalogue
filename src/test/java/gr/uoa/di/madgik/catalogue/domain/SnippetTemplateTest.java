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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnippetTemplateTest {

    /**
     * A model with a whole field, an unflagged field, and composite fields at several depths, split over a section
     * and its sub-section.
     */
    private static Model model() {
        Section details = new Section();
        details.setFields(List.of(
                field("logo", true),
                field("extras", false,
                        field("image", true),
                        field("git", false),
                        field("payments", false, field("title", true), field("includes", false)),
                        field("contact", false, field("email", false)))));
        Section main = new Section();
        main.setFields(List.of(field("tagline", true), field("description", false)));
        main.setSubSections(List.of(details));
        Model model = new Model();
        model.setSections(List.of(main));
        return model;
    }

    @Test
    void keepsFlaggedFieldsWholeAndOnlyTheFlaggedSubFieldsOfOthers() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tagline", "Plan data");
        payload.put("description", "Long text");
        payload.put("logo", Map.of("url", "https://example.org/logo.png", "alt", "Logo"));
        payload.put("extras", Map.of(
                "image", "https://example.org/image.png",
                "git", "https://example.org/repo.git",
                "contact", Map.of("email", "someone@example.org"),
                "payments", List.of(
                        Map.of("title", "Free", "includes", List.of("support")),
                        Map.of("includes", List.of("hosting")))));

        Map<String, Object> snippet = SnippetTemplate.of(model()).apply(payload);

        assertEquals(Map.of(
                "tagline", "Plan data",
                "logo", Map.of("url", "https://example.org/logo.png", "alt", "Logo"),
                "extras", Map.of(
                        "image", "https://example.org/image.png",
                        "payments", List.of(Map.of("title", "Free")))), snippet);
        assertEquals(4, payload.size());
    }

    @Test
    void dropsCompositeValuesLeftEmpty() {
        Map<String, Object> payload = Map.of("extras", Map.of(
                "git", "https://example.org/repo.git",
                "payments", List.of(Map.of("includes", List.of("hosting")))));

        assertEquals(Map.of(), SnippetTemplate.of(model()).apply(payload));
    }

    @Test
    void keepsFlaggedFieldsThatAreNullAndSkipsMissingOnes() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tagline", null);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("tagline", null);
        assertEquals(expected, SnippetTemplate.of(model()).apply(payload));
    }

    @Test
    void aModelWithoutFlaggedFieldsHasAnEmptyTemplate() {
        Section section = new Section();
        section.setFields(List.of(field("name", false), field("extras", false, field("git", false))));
        Model model = new Model();
        model.setSections(List.of(section));

        assertTrue(SnippetTemplate.of(model).isEmpty());
        assertTrue(SnippetTemplate.of(new Model()).isEmpty());
    }

    private static UiField field(String name, boolean includedInSnippet, UiField... subFields) {
        UiField field = new UiField();
        field.setName(name);
        field.setIncludedInSnippet(includedInSnippet);
        if (subFields.length > 0) {
            field.setSubFields(List.of(subFields));
        }
        return field;
    }
}
