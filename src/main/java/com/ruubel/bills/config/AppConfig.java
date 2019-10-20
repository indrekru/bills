package com.ruubel.bills.config;

import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class AppConfig {


    public AppConfig() {
        configureUTC();
    }

    public void configureUTC() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

}
