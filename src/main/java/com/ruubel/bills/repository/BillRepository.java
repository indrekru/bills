package com.ruubel.bills.repository;

import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByProperty(Property property);
}
