package com.fleet.customer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Customer Service is running";
    }
}