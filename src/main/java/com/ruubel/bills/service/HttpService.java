package com.ruubel.bills.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
            log.error("Failed calling: " + url, e);
        }
        return Optional.empty();
    }

    public Optional<HttpEntity> postSelfSigned(String url, Map<String, String> params) {

        try {
            CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
            HttpPost post = new HttpPost(url);

            JSONObject json = new JSONObject(params);

            post.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5EQTRRamd5UlVVNU16STBOek5DUkVWQ1FqTkJNRFExTlRkRE1qWXdNamMxUWtNeFJUYzVOQSJ9.eyJpc3MiOiJodHRwczovL29wZGV2cG9ydGFsLmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1ZGJlYzA4YTMxZjUzYzBkZWY3N2ZkZTgiLCJhdWQiOlsiT3BEZXZQb3J0YWxBcGkiLCJodHRwczovL29wZGV2cG9ydGFsLmV1LmF1dGgwLmNvbS91c2VyaW5mbyJdLCJpYXQiOjE1NzI3OTc3MjIsImV4cCI6MTU3MjgwNDkyMiwiYXpwIjoiWXpIaE9XaVNIbDc0ZnBDd2Ezc2N5V1c5cWMyUTg1SU0iLCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIn0.GLTVXjQUBzQhi_ePkDg7aMryXAQ50k67RfMYcxmjxT1IWK84tW92gt5kE3XPq4cTDfOIXHxMpPBsXFs6bN0su0OqbOhQe2WOs09b-EMq4bnAXOK0fZ8QFiXkQIuI_A5oSCd2uA4ZbjLnlTQOjS7UXDCg5GeA1CkkT2UTWeKi4UAZU1uL6hqM0iWI8LFU2z1lGnHdVYjhFESo8KsykVwp4YlAHRwsgRGk4ew-icWZ98MEZspdQeUM-n3CkozRvAX9BHVGlYVX5pJ-46tCDX-T5FVcSH0zfr_8x7BTnpd8Y24iAGOBpaZsfWp62uuu85-3Q13zGZc7kePi-U36lNhVKg");
            post.setHeader("x-request-id", "123");

            post.setEntity(new StringEntity(json.toString()));

            CloseableHttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();

            return Optional.of(entity);

        } catch (KeyManagementException | IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }

}
