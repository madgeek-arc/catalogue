package gr.athenarc.catalogue.ui.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.athenarc.catalogue.ui.service.JpaConverter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.List;

@Entity
@Table(name = "CHAPTER")
public class Chapter {

    @Id
    private String id;
    private String name;

    @Column(length = 2000)
    private String description;

    private String subType;

    @Min(0)
    @Column(name = "order_value", columnDefinition = "integer default 0")
    private int order;

    @OneToMany(mappedBy = "chapter", cascade= CascadeType.ALL)
//    @Convert(converter = JpaConverter.class)
    private List<Section> sections;

    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="model_id", nullable=false)
    @JsonIgnore
    Model model;

    public Chapter() {}

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
