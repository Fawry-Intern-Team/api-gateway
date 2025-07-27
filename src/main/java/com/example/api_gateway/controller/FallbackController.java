package com.example.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/product")
    public ResponseEntity<Map<String, String>> productFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product service is currently unavailable");
        response.put("status", "Circuit breaker is open");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @RequestMapping("/fallback/auth")
    public ResponseEntity<Map<String, String>> authFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Authentication service is currently unavailable");
        response.put("status", "Circuit breaker is open");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @RequestMapping("/fallback/order")
    public ResponseEntity<Map<String, String>> orderFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Order service is currently unavailable");
        response.put("status", "Circuit breaker is open");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}