package com.ruubel.bills.repository;

import com.ruubel.bills.model.GoogleToken;
import com.ruubel.bills.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GoogleTokenRepository extends JpaRepository<GoogleToken, UUID> {
    GoogleToken findTopByOrderByUpdatedAtDesc();
    GoogleToken findByUser(User user);
}
