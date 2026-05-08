package com.fleet.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HealthController {

    @GetMapping("/payments/health")
    public String health() {
        return "Paymentt Service is running";
    }


}
