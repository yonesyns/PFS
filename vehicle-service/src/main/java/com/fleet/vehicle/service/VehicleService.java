package com.fleet.vehicle.service;

import com.fleet.vehicle.dto.VehicleDTO;
import com.fleet.vehicle.model.Vehicle;
import com.fleet.vehicle.model.VehicleStatus;
import com.fleet.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Cacheable(value = "vehicles", key = "#id")
    public VehicleDTO getVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return mapToDTO(vehicle);
    }
    
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @CacheEvict(value = "vehicles", key = "#result.id")
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) {
        Vehicle vehicle = mapToEntity(vehicleDTO);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        Vehicle saved = vehicleRepository.save(vehicle);
        kafkaTemplate.send("vehicle-events", "vehicle.created", saved);
        return mapToDTO(saved);
    }
    
    @CacheEvict(value = "vehicles", key = "#id")
    public VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        
        vehicle.setMake(vehicleDTO.getMake());
        vehicle.setModel(vehicleDTO.getModel());
        vehicle.setYear(vehicleDTO.getYear());
        vehicle.setColor(vehicleDTO.getColor());
        vehicle.setLicensePlate(vehicleDTO.getLicensePlate());
        vehicle.setMileage(vehicleDTO.getMileage());
        vehicle.setLocation(vehicleDTO.getLocation());
        
        Vehicle updated = vehicleRepository.save(vehicle);
        kafkaTemplate.send("vehicle-events", "vehicle.updated", updated);
        return mapToDTO(updated);
    }
    
    public List<VehicleDTO> getAvailableVehicles() {
        return vehicleRepository.findAvailableVehicles().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    private VehicleDTO mapToDTO(Vehicle vehicle) {
        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setVin(vehicle.getVin());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setColor(vehicle.getColor());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setStatus(vehicle.getStatus());
        dto.setType(vehicle.getType());
        dto.setMileage(vehicle.getMileage());
        dto.setLocation(vehicle.getLocation());
        dto.setCustomerId(vehicle.getCustomerId());
        return dto;
    }
    
    private Vehicle mapToEntity(VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(dto.getVin());
        vehicle.setMake(dto.getMake());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        vehicle.setColor(dto.getColor());
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setType(dto.getType());
        vehicle.setMileage(dto.getMileage());
        vehicle.setLocation(dto.getLocation());
        vehicle.setCustomerId(dto.getCustomerId());
        return vehicle;
    }
}