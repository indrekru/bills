package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;
import com.ruubel.bills.service.BillType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "bill")
public class Bill implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "sender_email")
    private String senderEmail;

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

    public Bill(String name, String senderEmail, BillType billType, Property property) {
        this.name = name;
        this.senderEmail = senderEmail;
        this.billType = billType;
        this.property = property;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public BillType getBillType() {
        return billType;
    }

    public Property getProperty() {
        return property;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
