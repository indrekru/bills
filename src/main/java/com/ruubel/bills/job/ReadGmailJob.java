package com.ruubel.bills.job;

import com.ruubel.bills.model.GoogleToken;
import com.ruubel.bills.model.User;
import com.ruubel.bills.service.GmailService;
import com.ruubel.bills.service.GoogleTokenService;
import com.ruubel.bills.service.MailingService;
import com.ruubel.bills.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ruubel.bills.service.UserService.ADMIN_USER_EMAIL;

@Component
public class ReadGmailJob {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private UserService userService;
    private GoogleTokenService googleTokenService;
    private GmailService gmailService;
    private MailingService mailingService;

    @Autowired
    public ReadGmailJob(UserService userService, GoogleTokenService googleTokenService, GmailService gmailService, MailingService mailingService) {
        this.userService = userService;
        this.googleTokenService = googleTokenService;
        this.gmailService = gmailService;
        this.mailingService = mailingService;
    }

//    @Scheduled(cron = "0 0/5 * * * ?") // Every 5 minutes
    @Scheduled(cron = "0 0 0/12 * * ?") // Every 12 hours
    public void run() throws Exception {

        log.info("Running mail-read job");

        Optional<UserDetails> user = userService.findByEmail(ADMIN_USER_EMAIL);

        GoogleToken googleToken = googleTokenService.getValidToken((User) user.get());
        if (googleToken != null) {
            log.info("Fetching mail..");
            String readGmailResult = gmailService.readGmail(googleToken);
            mailingService.notifyMailRead(readGmailResult);
        } else {
            log.error("Couldn't get a valid token, investigate...");
        }

        log.info("Done running mail-read job");

    }

}
