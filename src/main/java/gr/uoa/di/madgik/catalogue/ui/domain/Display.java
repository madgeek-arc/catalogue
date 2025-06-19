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
