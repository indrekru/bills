package com.ruubel.bills.controller;


import com.ruubel.bills.service.HttpService;
import com.ruubel.bills.service.SebOauthTokenService;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/seb")
public class SebOBController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HttpService httpService;
    private SebOauthTokenService tokenService;

    private String CLIENT_ID = "indrekruubel@gmail.com";

    private String SCOPE = "accounts payments consents account.lists funds.confirmations";

    private String REDIRECT_URI = "https://gmail-read.herokuapp.com/api/v1/seb/callback"; //"http://localhost:8080/api/v1/seb/callback";

    @Autowired
    public SebOBController(HttpService httpService, SebOauthTokenService tokenService) {
        this.httpService = httpService;
        this.tokenService = tokenService;
    }

    @GetMapping("/connect")
    public String connect() {

        return String.format("redirect:https://developer.baltics.sebgroup.com/ib-emulator/EEUHEE2X?response_type=code&redirect_uri=%s&client_id=%s&scopes=%s&state=1&act2=psd2oauth",
                REDIRECT_URI, CLIENT_ID, SCOPE);

    }

    @GetMapping("/callback")
    public ResponseEntity callback(@RequestParam String code) {

        Optional<HttpEntity> httpEntity = httpService.postSelfSigned("https://pback-live.obdevportal.eu/v2/oauth/token", new HashMap<String, String>(){{
            put("grant_type", "authorization_code");
            put("code", code);
            put("redirect_uri", "https://dev.obdevportal.eu/callback-emulator");
        }});

        if (httpEntity.isPresent()) {
            log.info("Got response");
            try {
                JSONObject json = new JSONObject(EntityUtils.toString(httpEntity.get(), "UTF-8"));
                if (!json.has("access_token")) {
                    return ResponseEntity.badRequest().build();
                }

                return ResponseEntity.ok(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        log.info("Response is empty");

        return ResponseEntity.badRequest().build();
    }

}
