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

import gr.uoa.di.madgik.catalogue.domain.UiField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultModelServiceTest {

    @Test
    void reportsFieldsMarkedAsFacetsButNotIndexed() {
        UiField contradictory = field("a", false, true);
        UiField notIndexed = field("b", false, null);
        UiField noFacet = field("c", true, false);
        UiField plain = field("d", true, null);

        assertEquals(List.of("a"), DefaultModelService.facetsNotIndexed(List.of(contradictory, notIndexed, noFacet, plain)));
    }

    private static UiField field(String id, boolean index, Boolean facet) {
        UiField field = new UiField();
        field.setId(id);
        field.setIndex(index);
        field.setFacet(facet);
        return field;
    }
}
