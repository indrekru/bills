package com.ruubel.bills.controller;

import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.BillInstance;
import com.ruubel.bills.model.Property;
import com.ruubel.bills.model.User;
import com.ruubel.bills.service.BillService;
import com.ruubel.bills.service.PropertyService;
import com.ruubel.bills.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/bills")
public class BillController {

    private UserService userService;
    private PropertyService propertyService;
    private BillService billService;

    @Autowired
    public BillController(UserService userService, PropertyService propertyService, BillService billService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.billService = billService;
    }

    @GetMapping
    public ResponseEntity userBills(Principal principal) {
        List<Map<String, Object>> out = new ArrayList<>();
        Optional<UserDetails> maybeUser = userService.findByEmail(principal.getName());
        if(maybeUser.isPresent()) {
            User user = (User) maybeUser.get();

            List<Property> properties = propertyService.findAllByUser(user);

            for (Property property : properties) {
                List<Bill> bills = billService.findAllByProperty(property);
                for (Bill bill : bills) {
                    Optional<BillInstance> maybeBillInstance = billService.findTopByBillAndPaidAndOrderByCreatedAtDesc(bill, false);
                    if (maybeBillInstance.isPresent()) {
                        BillInstance billInstance = maybeBillInstance.get();
                        out.add(
                            new HashMap<String, Object>(){{
                                put("property", property.getName());
                                put("bill_name", bill.getName());
                                put("price", billInstance.getPrice());
                                put("created_at", billInstance.getCreatedAt());
                            }}
                        );
                    }
                }
            }
        }
        return ResponseEntity.ok(out);
    }

}
