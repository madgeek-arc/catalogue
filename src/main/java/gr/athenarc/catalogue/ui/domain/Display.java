package gr.athenarc.catalogue.ui.domain;

public class Display {

    String placement;
    Integer order;
    Boolean isVisible;
    Boolean hasBorder = false;

    public Display() {}

    public Display(String placement, Integer order, Boolean isVisible, Boolean hasBorder) {
        this.placement = placement;
        this.order = order;
        this.isVisible = isVisible;
        this.hasBorder = hasBorder;
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
}
