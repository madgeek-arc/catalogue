package gr.athenarc.catalogue.ui.domain;

public class Display {

    String placement;
    Integer order;
    Boolean isVisible;
    Boolean hasBorder = false;
    String cssClasses;
    String style;

    public Display() {}

    public Display(String placement, Integer order, Boolean isVisible, Boolean hasBorder, String cssClasses, String style) {
        this.placement = placement;
        this.order = order;
        this.isVisible = isVisible;
        this.hasBorder = hasBorder;
        this.cssClasses = cssClasses;
        this.style = style;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public Boolean getHasBorder() {
        return hasBorder;
    }

    public void setHasBorder(Boolean hasBorder) {
        this.hasBorder = hasBorder;
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
}
