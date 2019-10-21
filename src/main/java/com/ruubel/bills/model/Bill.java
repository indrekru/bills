package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;

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

    @ManyToOne
    @JoinColumn(name = "property")
    private Property property;

    @Column(name = "created_at")
    @Convert(converter = TimestampConverter.class)
    private Instant createdAt;

    public Bill() {
    }

    public Bill(String name, String senderEmail, Property property) {
        this.name = name;
        this.senderEmail = senderEmail;
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

    public Property getProperty() {
        return property;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
