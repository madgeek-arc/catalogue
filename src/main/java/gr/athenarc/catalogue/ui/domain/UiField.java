package gr.athenarc.catalogue.ui.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.athenarc.catalogue.ui.converter.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "FIELD")
public class UiField {

    @Id
    String id;
    String name;
    String parentId;
    String parent;
    String accessPath;
    boolean includedInSnippet;
    boolean deprecated;

    @Convert(converter = StyledStringJpaConverter.class)
    StyledString label;

    @Convert(converter = TypeInfoJpaConverter.class)
    TypeInfo typeInfo = new TypeInfo();

    @Convert(converter = FormJpaConverter.class)
    Form form = new Form();

    @Convert(converter = DisplayJpaConverter.class)
    Display display = new Display();

    @OneToMany(cascade = CascadeType.ALL)
//    @Convert(converter = ListUiFieldJpaConverter.class)
    List<UiField> subFields;

    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="section_id", nullable=false)
    @JsonIgnore
    Section section;

    public UiField() {}

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
