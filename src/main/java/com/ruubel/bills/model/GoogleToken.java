package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "google_token")
public class GoogleToken implements Serializable {

    @Id
    @GeneratedValue
    @Column
    private UUID id;

    @Column
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private String scope;

    @OneToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @Column
    private Long expiresIn;

    @Convert(converter = TimestampConverter.class)
    @Column
    private Instant expiresAt;

    @Convert(converter = TimestampConverter.class)
    @Column
    private Instant createdAt;

    @Convert(converter = TimestampConverter.class)
    @Column
    private Instant updatedAt;

    public GoogleToken() {
    }

    public GoogleToken(String accessToken, String refreshToken, User user, String scope, Long expiresIn) {
        Instant now = Instant.now();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.scope = scope;
        this.expiresIn = expiresIn;
        this.createdAt = now;
        this.updatedAt = now;
        this.expiresAt = getExpiresAt(expiresIn);
    }

    private Instant getExpiresAt(long expiresIn) {
        return Instant.now().plus(expiresIn, ChronoUnit.SECONDS);
    }

    public UUID getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void updateAccessToken(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresAt = getExpiresAt(expiresIn);
        this.updatedAt = Instant.now();
    }

    public boolean isExpired() {
        Instant now = Instant.now();
        return now.equals(this.expiresAt) || now.isAfter(this.expiresAt);
    }

    @Override
    public String toString() {
        return "GoogleToken{" +
                "id=" + id +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", scope='" + scope + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
