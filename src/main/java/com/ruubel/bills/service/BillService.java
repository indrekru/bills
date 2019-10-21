package com.ruubel.bills.service;

import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.Property;
import com.ruubel.bills.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillService {

    private BillRepository billRepository;

    @Autowired
    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public List<Bill> findAllByProperty(Property property) {
        return billRepository.findAllByProperty(property);
    }
}
