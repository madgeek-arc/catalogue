package gr.athenarc.catalogue.ui.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.athenarc.catalogue.ui.service.JpaConverter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.List;

@Entity
@Table(name = "SECTION")
public class Section {

    @Id
    String id;
    String name;

    @Column(length = 2000)
    String description;

    @Min(0)
    @Column(name = "order_value", columnDefinition = "integer default 0")
    int order = 0;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
//@Convert(converter = JpaConverter.class)
    List<UiField> fields;

    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="chapter_id", nullable=false)
    @JsonIgnore
    Chapter chapter;

    public Section() {}

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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<UiField> getFields() {
        return fields;
    }

    public void setFields(List<UiField> fields) {
        this.fields = fields;
    }
}
