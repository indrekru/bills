package com.ruubel.bills.job;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.ruubel.bills.model.*;
import com.ruubel.bills.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.ruubel.bills.service.UserService.ADMIN_USER_EMAIL;
import static com.ruubel.bills.service.billstrategy.AbstractBillStrategy.SENDER_EMAIL;

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
    @Scheduled(cron = "0 0 0/3 * * ?") // Every 1 hours
    public void run() throws Exception {

        log.info("Running mail-read job");

        Optional<UserDetails> userOptional = userService.findByEmail(ADMIN_USER_EMAIL);
        if (userOptional.isPresent()) {
            User user = (User) userOptional.get();

            GoogleToken googleToken = googleTokenService.getValidToken(user);
            if (googleToken != null) {
                log.info("Fetching mail..");

                Gmail gmail = gmailService.createService(googleToken);
                List<Message> messages = gmailService.getLast100Messages(gmail);
                List<Property> properties = propertyService.findAllByUser(user);

                Map<String, List<Message>> emailMessageMap = new HashMap<>();

                for (Message message : messages) {
                    String messageId = message.getId();
                    message = gmailService.getMessage(googleToken, messageId);
                    MessagePart payload = message.getPayload();
                    List<MessagePartHeader> headers = payload.getHeaders();
                    Optional<MessagePartHeader> optionalFromHeader = headers
                            .stream()
                            .filter(header -> header.getName().equals("From"))
                            .findFirst();
                    if (optionalFromHeader.isPresent()) {
                        List<Message> emailMessages = new ArrayList<>();
                        MessagePartHeader emailHeader = optionalFromHeader.get();
                        String email = emailHeader.getValue();
                        email = email.replace(">", "");
                        email = email.substring(email.indexOf("<") + 1);
                        if (emailMessageMap.containsKey(email)) {
                            emailMessages = emailMessageMap.get(email);
                        }
                        emailMessages.add(message);
                        emailMessageMap.put(email, emailMessages);
                    }
                }

                for (Property property : properties) {
                    List<Bill> bills = billService.findAllByProperty(property);
                    for (Bill bill : bills) {
                        String email = (String) bill.getParameter(SENDER_EMAIL);
                        BillType billType = bill.getBillType();
                        List<Message> emailMessages = emailMessageMap.get(email);
                        if (emailMessages == null) {
                            System.out.println("No entries found for email: " + email);
                            continue;
                        }
                        Iterator<Message> messageIterator = emailMessages.iterator();
                        while(messageIterator.hasNext()) {
                            Message message = messageIterator.next();

                            String messageId = message.getId();
                            Optional<BillInstance> maybeBillInstance = billService.findOneByExternalId(messageId);
                            if (!maybeBillInstance.isPresent()) {
                                Double toPay = billType.getToPay(bill, message, gmail);
                                if (toPay != null) {
                                    log.info("Saving new BillInstance for: '{}', price: {}", bill.getName(), toPay);
                                    BillInstance billInstance = new BillInstance(toPay, messageId, bill);
                                    billService.saveBillInstance(billInstance);
                                    messageIterator.remove();
                                }
                            } else {
                                BillInstance billInstance = maybeBillInstance.get();
                                log.info("Found BillInstance for : '{}', price: {}", bill.getName(), billInstance.getPrice());
                                messageIterator.remove();
                            }
                        }
                    }
                }
            } else {
                log.error("Couldn't get a valid google token, investigate...");
            }
        } else {
            log.error("No user found : {}", ADMIN_USER_EMAIL);
        }
        log.info("Done running mail-read job");
    }

}
