package com.ruubel.bills.job;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.ruubel.bills.model.Bill;
import com.ruubel.bills.model.GoogleToken;
import com.ruubel.bills.model.Property;
import com.ruubel.bills.model.User;
import com.ruubel.bills.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.ruubel.bills.service.UserService.ADMIN_USER_EMAIL;

@Component
public class ReadGmailJob {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private UserService userService;
    private GoogleTokenService googleTokenService;
    private GmailService gmailService;
    private MailingService mailingService;
    private PropertyService propertyService;
    private BillService billService;

    @Autowired
    public ReadGmailJob(
            UserService userService,
            GoogleTokenService googleTokenService,
            GmailService gmailService,
            MailingService mailingService,
            PropertyService propertyService,
            BillService billService
    ) {
        this.userService = userService;
        this.googleTokenService = googleTokenService;
        this.gmailService = gmailService;
        this.mailingService = mailingService;
        this.propertyService = propertyService;
        this.billService = billService;
    }

//    @Scheduled(cron = "0 0/5 * * * ?") // Every 5 minutes
    @Scheduled(cron = "0 0 0/12 * * ?") // Every 12 hours
    public void run() throws Exception {

        log.info("Running mail-read job");

        Optional<UserDetails> userOptional = userService.findByEmail(ADMIN_USER_EMAIL);
        if (userOptional.isPresent()) {
            User user = (User) userOptional.get();

            GoogleToken googleToken = googleTokenService.getValidToken(user);
            if (googleToken != null) {
                log.info("Fetching mail..");

                String out = "<h1>Bills</h1>";
                Double totalToPay = 0.0;

                Gmail gmail = gmailService.createService(googleToken);
                List<Message> messages = gmailService.getLast100Messages(gmail);
                List<Property> properties = propertyService.findAllByUser(user);

                for (Message message : messages) {
                    String messageId = message.getId();
                    message = gmailService.getMessage(googleToken, messageId);
                    for (Property property : properties) {
                        List<Bill> bills = billService.findAllByProperty(property);
                        for (Bill bill : bills) {
                            BillType billType = bill.getBillType();
                            Double toPay = billType.getToPay(bill, message, gmail);
                            if (toPay != null) {
                                totalToPay += toPay;
                                out += String.format("<p>%s: %s</p>", billType, toPay);
                            }
                        }
                    }
                }
                out += "<p>-----------------</p>";
                out += "<h2>Total: " + totalToPay + "</h2>";
                mailingService.notifyMailRead(out);
            } else {
                log.error("Couldn't get a valid google token, investigate...");
            }
        } else {
            log.error("No user found : {}", ADMIN_USER_EMAIL);
        }
        log.info("Done running mail-read job");
    }

}
