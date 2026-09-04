package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.vehicle.VehicleCatalogueResponseDTO;

import java.util.List;

public interface VehicleCatalogueService {
    List<VehicleCatalogueResponseDTO> getAll();
    List<String> getManufacturers();
    List<VehicleCatalogueResponseDTO> getModels(String manufacturer);
    List<VehicleCatalogueResponseDTO> getVariants(String manufacturer, String model);
}
