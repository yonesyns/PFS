package com.fleet.document.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/documents/health")
    public String health() {
        return "Document Service is running";
    }
}