package com.ruubel.bills.model;


import com.ruubel.bills.converter.TimestampConverter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bill_instance")
public class BillInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "price")
    private Double price;

    @Column(name = "paid")
    private boolean paid;

    @ManyToOne
    @JoinColumn(name = "bill")
    private Bill bill;

    @Column(name = "paid_at")
    @Convert(converter = TimestampConverter.class)
    private Instant paidAt;

    @Column(name = "created_at")
    @Convert(converter = TimestampConverter.class)
    private Instant createdAt;

    public BillInstance() {
    }

    public BillInstance(Double price, String externalId, Bill bill) {
        this.price = price;
        this.externalId = externalId;
        this.paid = false;
        this.bill = bill;
        this.paidAt = null;
        this.createdAt = Instant.now();
    }

    public Double getPrice() {
        return price;
    }
}
