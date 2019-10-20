package com.ruubel.bills.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.ruubel.bills.model.GoogleToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GmailService {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.app.client.id}")
    private String CLIENT_ID;

    @Value("${google.app.client.secret}")
    private String CLIENT_SECRET;

    @Autowired
    private PDFExtractorService pdfExtractorService;

    private Gmail service;
    private String user = "me";

    private static Credential convertToGoogleCredential(HttpTransport httpTransport, JsonFactory jsonFactory, String accessToken, String refreshToken, String clientId,String clientSecret) {
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setClientSecrets(clientId, clientSecret).build();
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);
        try {
            credential.refreshToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return credential;
    }

    public String readGmail(String accessToken, String refreshToken, String clientId, String clientSecret) throws Exception {

        String out = "";

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = convertToGoogleCredential(HTTP_TRANSPORT, JSON_FACTORY, accessToken, refreshToken, clientId, clientSecret);
        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();

        // Gets last 100 messages
        ListMessagesResponse messagesResponse = service.users().messages().list(user).execute();
        List<Message> messages = messagesResponse.getMessages();
        for (Message message : messages) {
            String messageId = message.getId();
            message = service.users().messages().get(user, messageId).execute();
            MessagePart payload = message.getPayload();
            List<MessagePartHeader> headers = payload.getHeaders();
            for (MessagePartHeader header : headers) {
                String headerName = header.getName();
                if (headerName.equals("From")) {
                    String from = header.getValue();
                    if (from.equals("hbimre <imre@heiberg.ee>")) {
                        BillType billType = BillType.KU_TATARI_60;
                        Double toPay = getToPay(messageId, payload, billType);
                        out += String.format("|%s = %s", billType, toPay);
                    } else if (from.equals("Imatra Elekter <klienditeenindus@imatraelekter.ee>")) {
                        BillType billType = BillType.IMATRA;
                        Double toPay = getToPay(messageId, payload, billType);
                        out += String.format("|%s = %s", billType, toPay);
                    }
                }
            }
        }

        return out;
    }

    private Double getToPay(String messageId, MessagePart payload, BillType billType) throws Exception {
        List<MessagePart> parts = payload.getParts();
        Double out = null;
        for (MessagePart part : parts) {
            String filename = part.getFilename().trim();
            if (!filename.isEmpty()) {
                MessagePartBody body = part.getBody();
                String attachmentId = body.getAttachmentId();
                body = service.users().messages().attachments().get(user, messageId, attachmentId).execute();
                byte[] bytes = body.decodeData();
                List<Double> prices = pdfExtractorService.extractAmounts(billType, bytes);
                out = billType.getBillMath().doTheMath(prices);
            }
        }
        return out;
    }

    public String readGmail(GoogleToken googleToken) throws Exception {
        return readGmail(googleToken.getAccessToken(), googleToken.getRefreshToken(), CLIENT_ID, CLIENT_SECRET);
    }
}
