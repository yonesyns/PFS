package com.fleet.customer.controller;

import com.fleet.customer.model.CustomerScore;
import com.fleet.customer.service.CustomerScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/{customerId}/score")
@RequiredArgsConstructor
public class CustomerScoreController {

    private final CustomerScoreService scoreService;

    @GetMapping
    public ResponseEntity<CustomerScore> getScore(@PathVariable Long customerId) {
        return ResponseEntity.ok(scoreService.getScore(customerId));
    }

    @PutMapping
    public ResponseEntity<CustomerScore> updateScore(
            @PathVariable Long customerId,
            @RequestParam int totalReservations,
            @RequestParam int cancelledReservations,
            @RequestParam int lateReturns) {
        return ResponseEntity.ok(scoreService.updateScore(customerId, cancelledReservations, lateReturns, totalReservations));
    }
}
