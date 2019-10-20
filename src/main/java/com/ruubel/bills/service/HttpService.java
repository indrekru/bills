package com.ruubel.bills.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class HttpService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Optional<HttpEntity> post(String url, Map<String, String> params) {
        try {
            Form form = Form.form();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                form.add(entry.getKey(), entry.getValue());
            }

            HttpResponse httpResponse = Request.Post(url)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyForm(form.build())
                    .execute()
                    .returnResponse();
            HttpEntity httpResponseEntity = httpResponse.getEntity();
            return Optional.of(httpResponseEntity);
        } catch (Exception e) {
            log.error("Failed calling: " + url);
        }
        return Optional.empty();
    }

}
