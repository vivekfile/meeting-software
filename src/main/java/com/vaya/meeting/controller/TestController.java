package com.vaya.meeting.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test/protected")
    public Map<String, String> protectedEndpoint(
            Authentication authentication
    ) {
        return Map.of(
                "message", "You are authenticated!",
                "email", authentication.getName()
        );
    }
}