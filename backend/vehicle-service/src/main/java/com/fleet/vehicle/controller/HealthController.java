package com.fleet.vehicle.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/vehicles/health")
    public String health() {
        return "Vehicle Service is running";
    }
}