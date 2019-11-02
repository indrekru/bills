package com.ruubel.bills.job;

import com.ruubel.bills.model.GoogleToken;
import com.ruubel.bills.service.GoogleTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevGmailParser {

    private GoogleTokenService tokenService;

    private GoogleToken token;

    @Autowired
    public DevGmailParser() {
        token = new GoogleToken(null, "", null, null, 3600l);
//        token = tokenService.refreshToken(token);
    }

}
