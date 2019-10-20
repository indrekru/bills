package com.ruubel.bills.config;

import com.ruubel.bills.util.CustomTokenExtractor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServer extends ResourceServerConfigurerAdapter {

    public final static String RESOURCE_ID = "api-resource";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID);
        resources.tokenExtractor(new CustomTokenExtractor());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/v1/google/connect").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/google/callback").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                .anyRequest().authenticated()
        ;
    }
}
