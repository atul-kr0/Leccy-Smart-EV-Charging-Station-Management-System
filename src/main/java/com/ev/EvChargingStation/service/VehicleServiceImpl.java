package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.vehicle.VehicleRequestDTO;
import com.ev.EvChargingStation.dto.vehicle.VehicleResponseDTO;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.entity.Vehicle;
import com.ev.EvChargingStation.entity.VehicleCatalogue;
import com.ev.EvChargingStation.exception.VehicleNotFoundException;
import com.ev.EvChargingStation.mapper.VehicleMapper;
import com.ev.EvChargingStation.repository.VehicleCatalogueRepository;
import com.ev.EvChargingStation.repository.VehicleRepository;
import com.ev.EvChargingStation.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCatalogueRepository catalogueRepository;
    private final SecurityUtil securityUtil;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponseDTO addVehicle(VehicleRequestDTO request) {
        User user = securityUtil.getCurrentUser();
        VehicleCatalogue catalogue = getCatalogue(request.getCatalogueVehicleId());

        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setCatalogueVehicle(catalogue);
        syncLegacyFields(vehicle, catalogue);
        vehicle.setUser(user);

        return vehicleMapper.entityToDto(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getMyVehicles() {
        User user = securityUtil.getCurrentUser();
        return vehicleRepository.findByUserId(user.getId()).stream().map(vehicleMapper::entityToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicle(Long id) {
        Vehicle vehicle = getOwnedVehicle(id);
        return vehicleMapper.entityToDto(vehicle);
    }

    @Override
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO request) {
        Vehicle vehicle = getOwnedVehicle(id);
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        VehicleCatalogue catalogue = getCatalogue(request.getCatalogueVehicleId());
        vehicle.setCatalogueVehicle(catalogue);
        syncLegacyFields(vehicle, catalogue);
        return vehicleMapper.entityToDto(vehicleRepository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.delete(getOwnedVehicle(id));
    }

    private void syncLegacyFields(Vehicle vehicle, VehicleCatalogue catalogue) {
        vehicle.setManufacturer(catalogue.getManufacturer());
        vehicle.setModel(catalogue.getModel());
        vehicle.setBatteryCapacity(catalogue.getBatteryCapacityKwh());
        vehicle.setConnectorType(catalogue.getConnectorType());
        vehicle.setMaxChargingPower(catalogue.getMaxDcChargingKw());
    }

    private VehicleCatalogue getCatalogue(Long id) {
        if (id == null) throw new IllegalArgumentException("catalogueVehicleId is required.");
        return catalogueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid catalogueVehicleId: " + id));
    }

    private Vehicle getOwnedVehicle(Long id) {
        User user = securityUtil.getCurrentUser();
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id: " + id));
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new VehicleNotFoundException("Vehicle not found with id: " + id);
        }
        return vehicle;
    }
}
