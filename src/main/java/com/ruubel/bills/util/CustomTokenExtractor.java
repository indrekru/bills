package com.ruubel.bills.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class CustomTokenExtractor implements TokenExtractor {

    public final static String AUTH_COOKIE_NAME = "token";

    private BearerTokenExtractor bearerTokenExtractor;

    public CustomTokenExtractor() {
        this.bearerTokenExtractor = new BearerTokenExtractor();
    }

    @Override
    public Authentication extract(HttpServletRequest httpServletRequest) {

        Authentication authentication;

        // Check the headers
        authentication = bearerTokenExtractor.extract(httpServletRequest);
        if (authentication != null) {
            return authentication;
        }

        // Check the cookies
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            cookies = new Cookie[]{};
        }
        Optional<Cookie> cookieOptional = Arrays
                .stream(cookies).filter(cookie -> cookie.getName().equals(AUTH_COOKIE_NAME))
                .findFirst();
        if (cookieOptional.isPresent()) {
            Cookie cookie = cookieOptional.get();
            authentication = new PreAuthenticatedAuthenticationToken(cookie.getValue(), "");
            return authentication;
        }
        return null;
    }
}
