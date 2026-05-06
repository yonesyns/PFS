package com.fleet.customer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/customers/health")
    public String health() {
        return "Customer Service is running";
    }
}