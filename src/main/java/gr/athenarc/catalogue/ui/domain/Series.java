package gr.athenarc.catalogue.ui.domain;

public class Series {

    String name;
    String referenceYear;

    public Series() {
    }

    public String getName() {
        return name;
    }

    public Series setName(String name) {
        this.name = name;
        return this;
    }

    public String getReferenceYear() {
        return referenceYear;
    }

    public Series setReferenceYear(String referenceYear) {
        this.referenceYear = referenceYear;
        return this;
    }
}
