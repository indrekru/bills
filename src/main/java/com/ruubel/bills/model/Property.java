package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "property")
public class Property implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @OneToMany(mappedBy = "property")
    private List<Bill> bills;

    @Column(name = "created_at")
    @Convert(converter = TimestampConverter.class)
    private Instant createdAt;

    public Property() {
    }

    public Property(String name, User user) {
        this.name = name;
        this.user = user;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
