package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.vehicle.VehicleRequestDTO;
import com.ev.EvChargingStation.dto.vehicle.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {
    VehicleResponseDTO addVehicle(VehicleRequestDTO request);
    List<VehicleResponseDTO> getMyVehicles();
    VehicleResponseDTO getVehicle(Long id);
    VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO request);
    void deleteVehicle(Long id);
}
