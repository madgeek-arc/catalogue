package gr.athenarc.catalogue.ui.domain;

public class StyledString {

    String text;
    String cssClasses;
    String style;
    boolean showLess;

    public StyledString() {
    }

    public static StyledString of(String text) {
        StyledString styledString = new StyledString();
        styledString.setText(text);
        return styledString;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCssClasses() {
        return cssClasses;
    }

    public void setCssClasses(String cssClasses) {
        this.cssClasses = cssClasses;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean isShowLess() {
        return showLess;
    }

    public void setShowLess(boolean showLess) {
        this.showLess = showLess;
    }
}
