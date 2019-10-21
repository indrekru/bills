package com.ruubel.bills.repository;

import com.ruubel.bills.model.Property;
import com.ruubel.bills.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findAllByUser(User user);
}
