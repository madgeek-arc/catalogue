package gr.uoa.di.madgik.catalogue.ui.domain.types;

public final class NumberProperties implements TypeProperties {

    Number min;
    Number max;
    Integer decimals;
    String pattern;

    public NumberProperties() {
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
