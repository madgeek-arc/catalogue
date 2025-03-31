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

public class UiField {

    String id;
    String name;
    String parentId;
    String parent;
    StyledString label;
    String accessPath;
    String kind;
    TypeInfo typeInfo = new TypeInfo();
    boolean includedInSnippet;
    boolean deprecated;
    Form form;
    Display display;
    List<UiField> subFields;

    public UiField() {
        this.form = new Form();
    }

    public UiField(UiField field) {
        this.id = field.getId();
        this.name = field.getName();
        this.parentId = field.getParentId();
        this.parent = field.getParent();
        this.label = field.getLabel();
        this.accessPath = field.getAccessPath();
        this.typeInfo = field.getTypeInfo();
        this.form = field.getForm();
        this.display = field.getDisplay();
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public StyledString getLabel() {
        return label;
    }

    public void setLabel(StyledString label) {
        this.label = label;
    }

    public String getAccessPath() {
        return accessPath;
    }

    public void setAccessPath(String accessPath) {
        this.accessPath = accessPath;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public boolean isIncludedInSnippet() {
        return includedInSnippet;
    }

    public void setIncludedInSnippet(boolean includedInSnippet) {
        this.includedInSnippet = includedInSnippet;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public List<UiField> getSubFields() {
        return subFields;
    }

    public void setSubFields(List<UiField> subFields) {
        this.subFields = subFields;
    }
}
