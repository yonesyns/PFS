package com.fleet.customer.dto;

import lombok.Data;

@Data
public class BookingReference {
    private String bookingId;
    private String status;
    private String vehicleId;
    private String startDate;
    private String endDate;
}
