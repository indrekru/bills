package com.ruubel.bills.controller;

import com.ruubel.bills.model.GoogleNonce;
import com.ruubel.bills.model.GoogleToken;
import com.ruubel.bills.model.User;
import com.ruubel.bills.service.GoogleTokenService;
import com.ruubel.bills.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/google")
public class GoogleOauthController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${google.app.client.id}")
    private String CLIENT_ID;

    @Value("${google.app.scope}")
    private String SCOPE;

    @Value("${google.app.redirect_uri}")
    private String REDIRECT_URI;

    private final static String NONCE_COOKIE_NAME = "nonce";
    private final static String FAILURE_REDIRECT = "redirect:/error";

    private GoogleTokenService googleTokenService;
    private UserService userService;

    @Autowired
    public GoogleOauthController(GoogleTokenService googleTokenService, UserService userService) {
        this.googleTokenService = googleTokenService;
        this.userService = userService;
    }

    @GetMapping("/nonce")
    public ResponseEntity nonce(Principal principal) {

        User user = (User) userService.loadUserByUsername(principal.getName());
        if (user == null) {
            log.error("No user found with username: {}", principal.getName());
            return ResponseEntity.badRequest().build();
        }

        // cleanup
        googleTokenService.deleteAllUserNonces(user);
        // create nonce
        GoogleNonce nonce = googleTokenService.createNonce(user, 30);

        return ResponseEntity.ok(new HashMap<String, Object>(){{
            put("nonce", nonce.getId());
        }});
    }

    @GetMapping("/connect")
    public String connect(@RequestParam(name = "nonce") UUID nonceId, HttpServletResponse response) {
        Optional<GoogleNonce> maybeNonce = googleTokenService.findNonceById(nonceId);
        if (maybeNonce != null && maybeNonce.isPresent()) {
            GoogleNonce nonce = maybeNonce.get();
            if (nonce.isExpired()) {
                log.error("Nonce is expired");
                return FAILURE_REDIRECT;
            }

            Duration duration = Duration.between(Instant.now(), nonce.getExpiresAt());
            int maxAge = Long.valueOf(duration.getSeconds()).intValue();
            log.info("Setting cookie maxAge to {}", maxAge);

            Cookie nonceCookie = new Cookie(NONCE_COOKIE_NAME, nonceId.toString());
            nonceCookie.setDomain("gmail-read.herokuapp.com");
            nonceCookie.setPath("/");
            nonceCookie.setMaxAge(maxAge);
            nonceCookie.setHttpOnly(true);
            nonceCookie.setSecure(true);

            response.addCookie(nonceCookie);

            String redirectUrl = String.format("https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&response_type=code&scope=%s&redirect_uri=%s&access_type=offline", CLIENT_ID, SCOPE, REDIRECT_URI);

            return String.format("redirect:%s", redirectUrl);

        }
        log.error("No nonce found");
        return FAILURE_REDIRECT;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        cookies = cookies == null ? new Cookie[]{} : cookies;

        Optional<Cookie> maybeNonceCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(NONCE_COOKIE_NAME)).findFirst();
        if (maybeNonceCookie.isPresent()) {
            UUID nonceId;
            Cookie nonceCookie = maybeNonceCookie.get();
            try {
                nonceId = UUID.fromString(nonceCookie.getValue());
            } catch (Exception e) {
                log.error("Failed parsing nonce");
                return FAILURE_REDIRECT;
            }
            Optional<GoogleNonce> optionalDbNonce = googleTokenService.findNonceById(nonceId);
            if (optionalDbNonce != null && optionalDbNonce.isPresent()) {
                GoogleNonce nonce = optionalDbNonce.get();
                if (nonce.isExpired()) {
                    log.error("Nonce is expired");
                    return FAILURE_REDIRECT;
                }
                User user = nonce.getUser();
                GoogleToken token = googleTokenService.getAccessToken(user, code);
                if (token == null) {
                    log.error("Couldn't create googleToken");
                    return FAILURE_REDIRECT;
                }
                // delete all user nonces
                googleTokenService.deleteAllUserNonces(user);
                // remove cookie
                nonceCookie.setMaxAge(0);
                response.addCookie(nonceCookie);
                return "redirect:/";
            } else {
                log.error("Nonce not found in DB");
            }
        } else {
            log.error("Nonce cookie not found");
        }
        return FAILURE_REDIRECT;
    }

}
