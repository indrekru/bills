package com.ruubel.bills.repository;

import com.ruubel.bills.model.SebOauthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SebOauthTokenRepository extends JpaRepository<SebOauthToken, UUID> {
}
