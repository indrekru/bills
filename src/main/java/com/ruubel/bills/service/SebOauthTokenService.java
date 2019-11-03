package com.ruubel.bills.service;

import com.ruubel.bills.repository.SebOauthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SebOauthTokenService {

    private SebOauthTokenRepository repository;

    @Autowired
    public SebOauthTokenService(SebOauthTokenRepository repository) {
        this.repository = repository;
    }
}
