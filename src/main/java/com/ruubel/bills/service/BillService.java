package com.ruubel.bills.service;

import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.BillInstance;
import com.ruubel.bills.model.Property;
import com.ruubel.bills.model.User;
import com.ruubel.bills.repository.BillInstanceRepository;
import com.ruubel.bills.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillService {

    private BillRepository billRepository;
    private BillInstanceRepository billInstanceRepository;

    @Autowired
    public BillService(BillRepository billRepository, BillInstanceRepository billInstanceRepository) {
        this.billRepository = billRepository;
        this.billInstanceRepository = billInstanceRepository;
    }

    public List<Bill> findAllByProperty(Property property) {
        return billRepository.findAllByProperty(property);
    }

    public Optional<BillInstance> findOneByExternalId(String externalId) {
        return billInstanceRepository.findOneByExternalId(externalId);
    }

    public BillInstance saveBillInstance(BillInstance billInstance) {
        return billInstanceRepository.save(billInstance);
    }

    public Optional<BillInstance> findTopByBillAndPaidAndOrderByCreatedAtDesc(Bill bill, boolean paid) {
        return billInstanceRepository.findTopByBillAndPaidOrderByCreatedAtDesc(bill, paid);
    }

    public List<BillInstance> findAllByPaidAndBillPropertyUser(boolean paid, User user) {
        return billInstanceRepository.findAllByPaidAndBillPropertyUser(paid, user);
    }
}
