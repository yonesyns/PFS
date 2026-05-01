package com.fleet.customer.service;

import com.fleet.customer.model.Customer;
import com.fleet.customer.model.CustomerScore;
import com.fleet.customer.repository.CustomerScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerScoreService {

    private final CustomerScoreRepository scoreRepository;
    private final CustomerService customerService;

    public CustomerScore getScore(Long customerId) {
        return scoreRepository.findByCustomerId(customerId)
                .orElseGet(() -> initScore(customerId));
    }

    public CustomerScore updateScore(Long customerId, int cancelledReservations, int lateReturns, int totalReservations) {
        CustomerScore score = scoreRepository.findByCustomerId(customerId)
                .orElseGet(() -> initScore(customerId));
        score.setTotalReservations(totalReservations);
        score.setCancelledReservations(cancelledReservations);
        score.setLateReturns(lateReturns);
        return scoreRepository.save(score);
    }

    private CustomerScore initScore(Long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        CustomerScore score = new CustomerScore();
        score.setCustomer(customer);
        return scoreRepository.save(score);
    }
}
