package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;
import com.ruubel.bills.service.BillType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "bill")
@TypeDefs({
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Bill implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_type")
    private BillType billType;

    @ManyToOne
    @JoinColumn(name = "property")
    private Property property;

    @Column(name = "created_at")
    @Convert(converter = TimestampConverter.class)
    private Instant createdAt;

    public Bill() {
    }

    public Bill(String name, BillType billType, Property property, Map<String, Object> parameters) {
        this.name = name;
        this.billType = billType;
        this.property = property;
        this.parameters = parameters;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BillType getBillType() {
        return billType;
    }

    public Property getProperty() {
        return property;
    }

    public Object getParameter(String key) {
        if (parameters != null && parameters.containsKey(key)) {
            return parameters.get(key);
        }
        return null;
    }
}
