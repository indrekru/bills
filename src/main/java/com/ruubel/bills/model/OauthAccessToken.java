package com.ruubel.bills.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "oauth_access_token")
public class OauthAccessToken {

    @Column(name = "token_id")
    private String tokenId;

    @Type(type="org.hibernate.type.BinaryType")
    @Column(name = "token")
    private String token;

    @Id
    @Column(name = "authentication_id")
    private String authenticationId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "client_id")
    private String clientId;

    @Type(type="org.hibernate.type.BinaryType")
    @Column(name = "authentication")
    private String authentication;

    @Column(name = "refresh_token")
    private String refreshToken;

    public OauthAccessToken() {
    }
}