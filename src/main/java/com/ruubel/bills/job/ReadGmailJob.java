package com.ruubel.bills.job;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
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

    @Scheduled(cron = "0 0/1 * * * ?") // Every 5 minutes
//    @Scheduled(cron = "0 0 0/12 * * ?") // Every 12 hours
    public void run() throws Exception {

        log.info("Running mail-read job");

        Optional<UserDetails> userOptional = userService.findByEmail(ADMIN_USER_EMAIL);
        if (userOptional.isPresent()) {
            User user = (User) userOptional.get();

            GoogleToken googleToken = googleTokenService.getValidToken(user);
            if (googleToken != null) {
                log.info("Fetching mail..");

                List<Message> messages = gmailService.getLast100Messages(googleToken);
                List<Property> properties = propertyService.findAllByUser(user);

                for (Message message : messages) {
                    String messageId = message.getId();
                    message = gmailService.getMessage(googleToken, messageId); //service.users().messages().get(user, messageId).execute();
                    MessagePart payload = message.getPayload();
                    List<MessagePartHeader> headers = payload.getHeaders();
                    Optional<MessagePartHeader> optionalFromHeader = headers
                        .stream()
                        .filter(header -> header.getName().equals("From"))
                        .findFirst();
                    if (optionalFromHeader.isPresent()) {
                        MessagePartHeader fromHeader = optionalFromHeader.get();
                        String senderEmail = fromHeader.getValue();
                        for (Property property : properties) {
                            List<Bill> bills = billService.findAllByProperty(property);
                            for (Bill bill : bills) {
                                String targetSenderEmail = bill.getSenderEmail();
                                if (senderEmail.contains(targetSenderEmail)) {
                                    log.info("Found email from sender: {}", targetSenderEmail);
                                }
                            }
                        }
                    }
                }

//                String readGmailResult = gmailService.readGmail(googleToken);
//                mailingService.notifyMailRead(readGmailResult);
            } else {
                log.error("Couldn't get a valid google token, investigate...");
            }
        } else {
            log.error("No user found : {}", ADMIN_USER_EMAIL);
        }

        log.info("Done running mail-read job");

    }

}
