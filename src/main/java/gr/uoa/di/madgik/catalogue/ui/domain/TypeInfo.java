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

package gr.uoa.di.madgik.catalogue.ui.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.madgik.catalogue.dto.IdLabel;
import gr.uoa.di.madgik.catalogue.ui.domain.jackson.TypeInfoDeserializer;
import gr.uoa.di.madgik.catalogue.ui.domain.types.TypeProperties;

import java.util.List;

@JsonDeserialize(using = TypeInfoDeserializer.class)
public class TypeInfo {

    String vocabulary;
    FieldType type;
    String defaultValue;
    List<IdLabel> values;
    TypeProperties properties;
    boolean multiplicity = false;
    DataRequest prefill;

    public TypeInfo() {
    }

    public TypeInfo(String vocabulary, FieldType type, String defaultValue, List<IdLabel> values, TypeProperties properties, boolean multiplicity, DataRequest prefill) {
        this.vocabulary = vocabulary;
        this.type = type;
        this.defaultValue = defaultValue;
        this.values = values;
        this.properties = properties;
        this.multiplicity = multiplicity;
        this.prefill = prefill;
    }

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<IdLabel> getValues() {
        return values;
    }

    public void setValues(List<IdLabel> values) {
        this.values = values;
    }

    public TypeProperties getProperties() {
        return properties;
    }

    public void setProperties(TypeProperties properties) {
        this.properties = properties;
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
