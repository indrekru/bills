package com.ruubel.bills.controller;

import com.ruubel.bills.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/user")
public class ApiUserController {

    private UserService userService;

    @Autowired
    public ApiUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity me(Principal principal) {
        UserDetails userDetails = userService.loadUserByUsername(principal.getName());
        return ResponseEntity.ok(new HashMap<String, Object>(){{
            put("name", userDetails.getUsername());
            put("password", userDetails.getPassword());
            put("enabled", userDetails.isEnabled());
        }});
    }

}
