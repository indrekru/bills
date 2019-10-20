package com.ruubel.bills.model;

import com.ruubel.bills.converter.TimestampConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

import static com.ruubel.bills.service.UserService.ROLE_USER;

@Entity
@Table(name = "identity")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @OneToOne(mappedBy = "user")
    private GoogleToken googleToken;

    @Column(name = "active")
    private boolean active;


    @Column(name = "created_at")
    @Convert(converter = TimestampConverter.class)
    private Instant createdAt;

    public User() {
    }

    public User(String email, String password, boolean active) {
        this.email = email;
        this.password = password;
        this.active = active;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<GrantedAuthority>(){{
            add(new SimpleGrantedAuthority(ROLE_USER));
        }};
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
