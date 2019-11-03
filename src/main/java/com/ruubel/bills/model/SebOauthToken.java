package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "seb_token")
public class SebOauthToken implements Serializable {

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

    public SebOauthToken() {
    }

    public SebOauthToken(String accessToken, String refreshToken, String scope, User user, Long expiresIn) {
        Instant now = Instant.now();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.user = user;
        this.expiresIn = expiresIn;
        this.expiresAt = getExpiresAt(expiresIn);
        this.createdAt = now;
        this.updatedAt = now;
    }

    private Instant getExpiresAt(long expiresIn) {
        return Instant.now().plus(expiresIn, ChronoUnit.SECONDS);
    }
}
