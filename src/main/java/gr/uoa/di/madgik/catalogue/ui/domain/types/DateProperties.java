package gr.uoa.di.madgik.catalogue.ui.domain.types;

public final class DateProperties implements TypeProperties {

    boolean formatToString = false;

    public DateProperties() {
    }

    public boolean isFormatToString() {
        return formatToString;
    }

    public void setFormatToString(boolean formatToString) {
        this.formatToString = formatToString;
    }
}
