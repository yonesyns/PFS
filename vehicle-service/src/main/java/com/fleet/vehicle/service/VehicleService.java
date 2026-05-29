package com.fleet.vehicle.service;

import com.fleet.commons.dto.PageResponse;
import com.fleet.commons.exception.BadRequestException;
import com.fleet.commons.exception.ResourceConflictException;
import com.fleet.commons.exception.ResourceNotFoundException;
import com.fleet.vehicle.client.CustomerServiceClient;
import com.fleet.vehicle.dto.*;
import com.fleet.vehicle.entity.Vehicle;
import com.fleet.vehicle.entity.VehicleStatus;
import com.fleet.vehicle.mapper.VehicleMapper;
import com.fleet.vehicle.messaging.VehicleEventPublisher;
import com.fleet.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleEventPublisher eventPublisher;
    private final CustomerServiceClient customerServiceClient;

    public VehicleResponse createVehicle(VehicleRequest request) {
        log.info("Creating vehicle with plate: {}", request.getPlateNumber());

        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new ResourceConflictException("Plate number already exists: " + request.getPlateNumber());
        }
        if (vehicleRepository.existsByVin(request.getVin())) {
            throw new ResourceConflictException("VIN already exists: " + request.getVin());
        }

        if (request.getCustomerId() != null) {
            validateCustomerExists(request.getCustomerId());
        }

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setStatus(determineInitialStatus(request));

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created with id: {}", saved.getId());

        eventPublisher.publishVehicleCreated(saved);

        if (saved.getCustomerId() != null) {
            eventPublisher.publishVehicleAssigned(saved, null, saved.getCustomerId());
        }

        return vehicleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID id) {
        Vehicle vehicle = findVehicleById(id);
        return vehicleMapper.toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getAllVehicles(Pageable pageable) {
        Page<Vehicle> page = vehicleRepository.findAll(pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehiclesByCustomer(UUID customerId, Pageable pageable) {
        Page<Vehicle> page = vehicleRepository.findByCustomerId(customerId, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehiclesByStatus(VehicleStatus status, Pageable pageable) {
        Page<Vehicle> page = vehicleRepository.findByStatus(status, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getExpiringInsurance(int days, Pageable pageable) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        Page<Vehicle> page = vehicleRepository.findExpiringInsurance(expiryDate, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getMaintenanceDue() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return vehicleMapper.toResponseList(vehicleRepository.findMaintenanceDue(sixMonthsAgo));
    }

    public VehicleResponse updateVehicle(UUID id, VehicleRequest request) {
        Vehicle vehicle = findVehicleById(id);

        if (!vehicle.getPlateNumber().equals(request.getPlateNumber()) && 
                vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new ResourceConflictException("Plate number already exists: " + request.getPlateNumber());
        }
        if (!vehicle.getVin().equals(request.getVin()) && 
                vehicleRepository.existsByVin(request.getVin())) {
            throw new ResourceConflictException("VIN already exists: " + request.getVin());
        }

        UUID oldCustomerId = vehicle.getCustomerId();

        if (request.getCustomerId() != null && !request.getCustomerId().equals(oldCustomerId)) {
            validateCustomerExists(request.getCustomerId());
        }

        vehicleMapper.updateEntity(vehicle, request);
        Vehicle updated = vehicleRepository.save(vehicle);

        log.info("Vehicle updated: {}", id);

        if (oldCustomerId == null && updated.getCustomerId() != null) {
            eventPublisher.publishVehicleAssigned(updated, null, updated.getCustomerId());
        } else if (oldCustomerId != null && !oldCustomerId.equals(updated.getCustomerId())) {
            eventPublisher.publishVehicleAssigned(updated, oldCustomerId, updated.getCustomerId());
        }

        return vehicleMapper.toResponse(updated);
    }

    public VehicleResponse updateStatus(UUID id, VehicleStatusUpdateRequest request) {
        Vehicle vehicle = findVehicleById(id);
        VehicleStatus oldStatus = vehicle.getStatus();

        vehicle.setStatus(request.getStatus());
        Vehicle updated = vehicleRepository.save(vehicle);

        log.info("Vehicle {} status changed from {} to {}", id, oldStatus, request.getStatus());

        if (oldStatus != request.getStatus()) {
            eventPublisher.publishVehicleStatusChanged(updated, oldStatus, request.getStatus(), request.getReason());
        }

        return vehicleMapper.toResponse(updated);
    }

    public VehicleResponse assignVehicle(UUID id, VehicleAssignRequest request) {
        Vehicle vehicle = findVehicleById(id);
        UUID oldCustomerId = vehicle.getCustomerId();

        validateCustomerExists(request.getCustomerId());

        vehicle.setCustomerId(request.getCustomerId());
        vehicle.setStatus(VehicleStatus.ACTIVE);
        Vehicle updated = vehicleRepository.save(vehicle);

        log.info("Vehicle {} assigned to customer {}", id, request.getCustomerId());
        eventPublisher.publishVehicleAssigned(updated, oldCustomerId, request.getCustomerId());

        return vehicleMapper.toResponse(updated);
    }

    public VehicleResponse unassignVehicle(UUID id) {
        Vehicle vehicle = findVehicleById(id);
        UUID oldCustomerId = vehicle.getCustomerId();

        if (oldCustomerId == null) {
            throw new BadRequestException("Vehicle is not assigned to any customer");
        }

        vehicle.setCustomerId(null);
        vehicle.setStatus(VehicleStatus.ORPHANED);
        Vehicle updated = vehicleRepository.save(vehicle);

        log.info("Vehicle {} unassigned from customer {}", id, oldCustomerId);
        eventPublisher.publishVehicleUnassigned(updated, oldCustomerId);

        return vehicleMapper.toResponse(updated);
    }

    public void deleteVehicle(UUID id) {
        Vehicle vehicle = findVehicleById(id);
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted: {}", id);
        eventPublisher.publishVehicleDeleted(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle findVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
    }

    public void handleCustomerDeleted(UUID customerId) {
        log.info("Handling customer deletion: {}", customerId);
        int updated = vehicleRepository.orphanVehiclesByCustomerId(customerId);
        log.info("Orphaned {} vehicles for customer {}", updated, customerId);
    }

    public void handleCustomerSuspended(UUID customerId) {
        log.info("Handling customer suspension: {}", customerId);
        int updated = vehicleRepository.updateStatusByCustomerId(customerId, VehicleStatus.INACTIVE);
        log.info("Deactivated {} vehicles for customer {}", updated, customerId);
    }

    public void handleCustomerReactivated(UUID customerId) {
        log.info("Handling customer reactivation: {}", customerId);
        int updated = vehicleRepository.updateStatusByCustomerId(customerId, VehicleStatus.ACTIVE);
        log.info("Reactivated {} vehicles for customer {}", updated, customerId);
    }

    public void checkExpiredInsurance() {
        log.info("Checking expired insurance...");
        LocalDate today = LocalDate.now();
        List<Vehicle> expired = vehicleRepository.findExpiringInsurance(today, Pageable.unpaged()).getContent();

        for (Vehicle vehicle : expired) {
            if (vehicle.getStatus() == VehicleStatus.ACTIVE) {
                vehicle.setStatus(VehicleStatus.INACTIVE);
                vehicleRepository.save(vehicle);
                eventPublisher.publishVehicleStatusChanged(vehicle, VehicleStatus.ACTIVE, VehicleStatus.INACTIVE, "Insurance expired");
                log.info("Vehicle {} deactivated due to expired insurance", vehicle.getId());
            }
        }
    }

    public void checkMaintenanceDue() {
        log.info("Checking maintenance due...");
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Vehicle> due = vehicleRepository.findMaintenanceDue(sixMonthsAgo);

        for (Vehicle vehicle : due) {
            if (vehicle.getStatus() == VehicleStatus.ACTIVE) {
                vehicle.setStatus(VehicleStatus.MAINTENANCE);
                vehicleRepository.save(vehicle);
                eventPublisher.publishVehicleStatusChanged(vehicle, VehicleStatus.ACTIVE, VehicleStatus.MAINTENANCE, "Technical inspection overdue");
                log.info("Vehicle {} set to MAINTENANCE due to overdue inspection", vehicle.getId());
            }
        }
    }

    private void validateCustomerExists(UUID customerId) {
        Boolean exists = customerServiceClient.customerExists(customerId).block();
        if (Boolean.FALSE.equals(exists)) {
            throw new BadRequestException("Customer does not exist or is not active: " + customerId);
        }
    }

    private VehicleStatus determineInitialStatus(VehicleRequest request) {
        if (request.getCustomerId() == null) {
            return VehicleStatus.ORPHANED;
        }
        if (request.getInsuranceExpiryDate() != null && request.getInsuranceExpiryDate().isBefore(LocalDate.now())) {
            return VehicleStatus.INACTIVE;
        }
        return VehicleStatus.ACTIVE;
    }

    private PageResponse<VehicleResponse> buildPageResponse(Page<Vehicle> page) {
        return PageResponse.<VehicleResponse>builder()
                .content(vehicleMapper.toResponseList(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
