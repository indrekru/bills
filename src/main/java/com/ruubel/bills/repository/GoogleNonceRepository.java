package com.ruubel.bills.repository;

import com.ruubel.bills.model.GoogleNonce;
import com.ruubel.bills.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoogleNonceRepository extends JpaRepository<GoogleNonce, UUID> {
    List<GoogleNonce> findAllByUser(User user);
}