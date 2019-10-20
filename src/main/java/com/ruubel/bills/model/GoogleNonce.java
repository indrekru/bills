package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "google_nonce")
public class GoogleNonce {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @Column(name = "expired")
    @Convert(converter = TimestampConverter.class)
    private Instant expired;

    @Column(name = "expires_at")
    @Convert(converter = TimestampConverter.class)
    private Instant expiresAt;

    public GoogleNonce() {
    }

    public GoogleNonce(User user, Instant expiresAt) {
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public Instant getExpired() {
        return expired;
    }

    public void setExpired(Instant expired) {
        this.expired = expired;
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        if (expired != null) {
            return true;
        }
        Instant now = Instant.now();
        return now.equals(this.expiresAt) || now.isAfter(this.expiresAt);
    }
}
