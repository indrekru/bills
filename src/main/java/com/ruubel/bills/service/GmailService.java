package com.ruubel.bills.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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

    private JsonFactory JSON_FACTORY;
    private NetHttpTransport HTTP_TRANSPORT;
    private String APPLICATION_NAME = "Gmail read API";

    @Value("${google.app.client.id}")
    private String CLIENT_ID;

    @Value("${google.app.client.secret}")
    private String CLIENT_SECRET;

    private PDFExtractorService pdfExtractorService;

    private String user = "me";

    @Autowired
    public GmailService(PDFExtractorService pdfExtractorService) throws Exception {
        this.pdfExtractorService = pdfExtractorService;
        this.JSON_FACTORY = JacksonFactory.getDefaultInstance();
        this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    }

    private Credential convertToGoogleCredential(GoogleToken googleToken) {
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
            .build();
        credential.setAccessToken(googleToken.getAccessToken());
        credential.setRefreshToken(googleToken.getRefreshToken());
        try {
            credential.refreshToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return credential;
    }

    public Gmail createService(GoogleToken googleToken) {
        Credential credential = convertToGoogleCredential(googleToken);
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    public Message getMessage(GoogleToken googleToken, String messageId) throws Exception {
        Gmail service = createService(googleToken);
        return service.users().messages().get(user, messageId).execute();
    }

    public List<Message> getLast100Messages(GoogleToken googleToken) throws Exception {
        Gmail service = createService(googleToken);

        ListMessagesResponse messagesResponse = service.users().messages().list(user).execute();
        return messagesResponse.getMessages();
    }

    public String readGmail(GoogleToken googleToken) throws Exception {

        String out = "";

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = convertToGoogleCredential(googleToken);
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();

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
                        Double toPay = getToPay(messageId, payload, billType, service);
                        out += String.format("|%s = %s", billType, toPay);
                    } else if (from.equals("Imatra Elekter <klienditeenindus@imatraelekter.ee>")) {
                        BillType billType = BillType.IMATRA;
                        Double toPay = getToPay(messageId, payload, billType, service);
                        out += String.format("|%s = %s", billType, toPay);
                    }
                }
            }
        }

        return out;
    }

    public Double getToPay(String messageId, MessagePart payload, BillType billType, Gmail service) throws Exception {
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
}
