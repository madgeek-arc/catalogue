package gr.athenarc.catalogue.ui.domain;

public class Display {

    String placement;
    Integer order = 0;
    Boolean visible = true;
    Boolean hasBorder = false;
    String cssClasses;
    String style;

    public Display() {}

    public Display(String placement, Integer order, Boolean visible, Boolean hasBorder, String cssClasses, String style) {
        this.placement = placement;
        this.order = order;
        this.visible = visible;
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
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
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
