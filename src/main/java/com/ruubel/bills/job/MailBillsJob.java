package com.ruubel.bills.job;

import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.BillInstance;
import com.ruubel.bills.model.Property;
import com.ruubel.bills.model.User;
import com.ruubel.bills.service.BillService;
import com.ruubel.bills.service.MailingService;
import com.ruubel.bills.service.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MailBillsJob {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private UserService userService;
    private BillService billService;
    private MailingService mailingService;

    @Autowired
    public MailBillsJob(UserService userService, BillService billService, MailingService mailingService) {
        this.userService = userService;
        this.billService = billService;
        this.mailingService = mailingService;
    }

//    @Scheduled(cron = "0 0/5 * * * ?") // Every 5 minutes
    @Scheduled(cron = "0 0 0/6 * * ?") // Every 6 hours
    public void run() throws Exception {

        log.info("Sending unpaid bill emails");

        List<User> users = userService.findAllByActive(true);

        for (User user : users) {
            Map<String, Object> out = new HashMap<>();
            List<BillInstance> unpaidBills =  billService.findAllByPaidAndBillPropertyUser(false, user);
            log.info("Found {} unpaid bills, sorting them...", unpaidBills.size());

            Map<Property, List<BillInstance>> propertyBills = new HashMap<>();
            // map to properties
            for (BillInstance unpaidBill : unpaidBills) {
                Bill bill = unpaidBill.getBill();
                Property property = bill.getProperty();
                List<BillInstance> billInstances = new ArrayList<>();
                if (propertyBills.containsKey(property)) {
                    billInstances = propertyBills.get(property);
                }
                billInstances.add(unpaidBill);
                propertyBills.put(property, billInstances);
            }
            log.info("Done mapping to properties, map size: {}", propertyBills.size());

            // Create the JSON
            for (Map.Entry<Property, List<BillInstance>> entry : propertyBills.entrySet()) {
                Property property = entry.getKey();
                log.info("Doing property: {}" + property.getName());
                List<BillInstance> bills = entry.getValue();
                List<Map<String, Object>> billsOut = new ArrayList<>();
                for (BillInstance billInstance : bills) {
                    log.info("Doing bill price: {}", billInstance.getPrice());
                    Bill bill = billInstance.getBill();
                    billsOut.add(new HashMap<String, Object>(){{
                        put("price", billInstance.getPrice());
                        put("paid", billInstance.isPaid());
                        put("paid_at", billInstance.getPaidAt());
                        put("name", bill.getName());
                    }});
                }
                String propertyName = property.getName();
                out.put(propertyName, billsOut);
            }

            log.info("Created map size: {}", out.size());

            JSONObject jsonObject = new JSONObject(out);
            mailingService.notifyUnpaidBills(jsonObject.toString());
        }
        log.info("Done sending emails");
    }

}
