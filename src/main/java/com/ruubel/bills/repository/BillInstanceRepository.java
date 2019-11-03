package com.ruubel.bills.repository;

import com.ruubel.bills.model.BillInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillInstanceRepository extends JpaRepository<BillInstance, Long> {
    BillInstance findOneByExternalId(String externalId);
}
