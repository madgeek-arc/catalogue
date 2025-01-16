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

package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class TypeInfo {

    String type;
    List<String> values;
    String vocabulary;
    boolean multiplicity = false;
    DataRequest prefill;

    public TypeInfo() {}

    public String getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type.getKey();
    }

    public void setType(String type) {
        try {
            if (FieldType.exists(type)) {
                this.type = FieldType.fromString(type).getKey();
            } else {
                this.type = type;
            }
        } catch (IllegalArgumentException e) {
            this.type = FieldType.COMPOSITE.getKey();
        }
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

    public boolean isMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(boolean multiplicity) {
        this.multiplicity = multiplicity;
    }

    public DataRequest getPrefill() {
        return prefill;
    }

    public void setPrefill(DataRequest prefill) {
        this.prefill = prefill;
    }
}
