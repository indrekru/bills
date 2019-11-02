package com.ruubel.bills.service;

import com.ruubel.bills.model.GoogleNonce;
import com.ruubel.bills.model.GoogleToken;
import com.ruubel.bills.model.User;
import com.ruubel.bills.repository.GoogleNonceRepository;
import com.ruubel.bills.repository.GoogleTokenRepository;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleTokenService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${google.app.client.id}")
    private String CLIENT_ID;

    @Value("${google.app.client.secret}")
    private String CLIENT_SECRET;

    @Value("${google.app.redirect_uri}")
    private String REDIRECT_URI;

    private GoogleTokenRepository repository;
    private GoogleNonceRepository nonceRepository;
    private HttpService httpService;

    @Autowired
    public GoogleTokenService(GoogleTokenRepository repository, GoogleNonceRepository nonceRepository, HttpService httpService) {
        this.repository = repository;
        this.nonceRepository = nonceRepository;
        this.httpService = httpService;
    }

    public GoogleToken getAccessToken(User user, String authorizationCode) {
        Optional<HttpEntity> httpEntity = httpService.post("https://www.googleapis.com/oauth2/v4/token", new HashMap<String, String>() {{
            put("code", authorizationCode);
            put("client_id", CLIENT_ID);
            put("client_secret", CLIENT_SECRET);
            put("grant_type", "authorization_code");
            put("redirect_uri", REDIRECT_URI);
        }});

        if (httpEntity.isPresent()) {
            try {
                JSONObject json = new JSONObject(EntityUtils.toString(httpEntity.get(), "UTF-8"));
                String accessToken = json.optString("access_token", null);
                if (accessToken == null) {
                    log.error("Failed getting access_token");
                    return null;
                }

                String refreshToken = json.optString("refresh_token", null);
                long expiresIn = json.optLong("expires_in");
                String scope = json.optString("scope", null);

                GoogleToken googleToken = findTopByOrderByUpdatedAtDesc();
                if (googleToken == null) {
                    googleToken = new GoogleToken(
                        accessToken,
                        refreshToken,
                        user,
                        scope,
                        expiresIn
                    );
                } else {
                    googleToken.updateAccessToken(accessToken, expiresIn);
                }

                return save(googleToken);
            } catch (Exception e) {
                log.error("Failed parsing response", e);
                return null;
            }
        }

        log.error("Failed getting access_token");
        return null;
    }

    public GoogleToken refreshTokenAndUpdateDB(GoogleToken googleToken) {
        googleToken = refreshToken(googleToken);
        if (googleToken != null) {
            return save(googleToken);
        }
        return null;
    }

    public GoogleToken refreshToken(GoogleToken googleToken) {
        Optional<HttpEntity> httpEntity = httpService.post("https://oauth2.googleapis.com/token", new HashMap<String, String>() {{
            put("refresh_token", googleToken.getRefreshToken());
            put("client_id", CLIENT_ID);
            put("client_secret", CLIENT_SECRET);
            put("grant_type", "refresh_token");
        }});

        if (httpEntity != null && httpEntity.isPresent()) {
            try {
                JSONObject json = new JSONObject(EntityUtils.toString(httpEntity.get(), "UTF-8"));
                String accessToken = json.optString("access_token", null);
                if (accessToken == null) {
                    log.error("Failed refreshing token");
                    return null;
                }
                long expiresIn = json.optLong("expires_in");
                googleToken.updateAccessToken(accessToken, expiresIn);
                return googleToken;
            } catch (Exception e) {
                log.error("Failed refreshing token", e);
                return null;
            }
        }

        log.error("Failed refreshing token");
        return null;
    }
    
    public GoogleToken getValidToken(User user) {
        GoogleToken token = findByUser(user);
        if (token == null) {
            log.error("No token in DB, grant access first");
            return null;
        }
        if (token.isExpired()) {
            return refreshTokenAndUpdateDB(token);
        }
        return token;
    }

    private GoogleToken findByUser(User user) {
        return repository.findByUser(user);
    }

    public GoogleToken findTopByOrderByUpdatedAtDesc() {
        return repository.findTopByOrderByUpdatedAtDesc();
    }

    public GoogleToken save(GoogleToken token) {
        return repository.save(token);
    }

    public void deleteAllUserNonces(User user) {
        List<GoogleNonce> nonceList = nonceRepository.findAllByUser(user);
        for (GoogleNonce nonce : nonceList) {
            nonceRepository.delete(nonce);
        }
    }

    public GoogleNonce createNonce(User user, int expiresInSeconds) {
        Instant expiresAt = Instant.now().plusSeconds(expiresInSeconds);
        GoogleNonce nonce = new GoogleNonce(user, expiresAt);
        return nonceRepository.save(nonce);
    }

    public Optional<GoogleNonce> findNonceById(UUID id) {
        return nonceRepository.findById(id);
    }

    public GoogleNonce saveNonce(GoogleNonce nonce) {
        return nonceRepository.save(nonce);
    }
}
