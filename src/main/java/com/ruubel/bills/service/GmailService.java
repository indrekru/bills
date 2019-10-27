package com.ruubel.bills.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
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
    public final static String USER = "me";

    @Value("${google.app.client.id}")
    private String CLIENT_ID;

    @Value("${google.app.client.secret}")
    private String CLIENT_SECRET;


    @Autowired
    public GmailService() throws Exception {
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
        return service.users().messages().get(USER, messageId).execute();
    }

    public List<Message> getLast100Messages(Gmail service) throws Exception {
        ListMessagesResponse messagesResponse = service.users().messages().list(USER).execute();
        return messagesResponse.getMessages();
    }
}
