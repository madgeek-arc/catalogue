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
package gr.uoa.di.madgik.catalogue.ui.domain;


import java.util.List;

public class Form {

    FieldIdNameValue dependsOn;
    List<FieldIdNameValue> affects = null;
    String group;
    StyledString description;
    StyledString suggestion;
    String placeholder;
    Boolean mandatory;
    Boolean immutable;
    Display display = new Display();


    public Form() {
    }

    public FieldIdNameValue getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(FieldIdNameValue dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<FieldIdNameValue> getAffects() {
        return affects;
    }

    public void setAffects(List<FieldIdNameValue> affects) {
        this.affects = affects;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public StyledString getDescription() {
        return description;
    }

    public void setDescription(StyledString description) {
        this.description = description;
    }

    public StyledString getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(StyledString suggestion) {
        this.suggestion = suggestion;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }
}
