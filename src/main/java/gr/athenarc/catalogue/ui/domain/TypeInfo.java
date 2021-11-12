package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class TypeInfo {

    String type;
    List<String> values;
    String vocabulary;
    boolean multiplicity = false;

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
}
