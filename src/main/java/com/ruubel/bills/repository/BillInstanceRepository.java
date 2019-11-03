package com.ruubel.bills.repository;

import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.BillInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillInstanceRepository extends JpaRepository<BillInstance, Long> {
    Optional<BillInstance> findOneByExternalId(String externalId);
    Optional<BillInstance> findTopByBillAndPaidOrderByCreatedAtDesc(Bill bill, boolean paid);
}
