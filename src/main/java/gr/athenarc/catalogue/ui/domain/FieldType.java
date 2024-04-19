/*
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

package gr.athenarc.catalogue.ui.domain;

import java.util.Arrays;

public enum FieldType {
    STRING                  ("string"),
    URL                     ("url"),
    INT                     ("int"),
    BOOLEAN                 ("boolean"),
    LIST                    ("array"),
    METADATA                ("Metadata"),
    VOCABULARY              ("vocabulary"),
    COMPOSITE               ("composite"),
    XMLGREGORIANCALENDAR    ("date");

    private final String typeValue;

    FieldType(final String type) {
        this.typeValue = type;
    }

    public String getKey() {
        return typeValue;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static FieldType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(FieldType.values())
                .filter(v -> v.typeValue.equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }

    /**
     * Checks if the given {@link String} exists in the values of the enum.
     * @return boolean
     */
    public static boolean exists(String s) {
        return Arrays.stream(FieldType.values())
                .anyMatch(v -> v.typeValue.equalsIgnoreCase(s));
    }
}
