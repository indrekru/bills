package com.ruubel.bills.service;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Email;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailingService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${mailing.enabled}")
    private boolean mailingEnabled;

    // Free service, who cares...
    private String apiKey = "406970c11818cb958bea6a9b6bd6b2e0";
    private String otherApiKey = "7d9a8dbb758d0da19c1321bdf37f2f20";

    public MailingService() {}

    public void notifyUnpaidBills(String content) {
        send("Unpaid bills", content);
    }

    public void notifyMailReadFail() {
        send("Read-mail failed", "Couldn't refresh token or something");
    }

    private void send(String subject, String content) {
        if (!mailingEnabled) {
            log.warn("Mailing disabled");
            return;
        }
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(apiKey, otherApiKey);
        request = new MailjetRequest(Email.resource)
                .property(Email.FROMEMAIL, "jutowapab@poly-swarm.com")
                .property(Email.FROMNAME, "Gmail-read")
                .property(Email.SUBJECT, subject)
                .property(Email.TEXTPART, content)
                .property(Email.RECIPIENTS, new JSONArray()
                        .put(new JSONObject()
                                .put("Email", "indrekruubel@gmail.com")));
        try {
            response = client.post(request);
        } catch (MailjetException e) {
            e.printStackTrace();
            return;
        } catch (MailjetSocketTimeoutException e) {
            e.printStackTrace();
            return;
        }
        log.info(response.getData().toString());
    }
}
