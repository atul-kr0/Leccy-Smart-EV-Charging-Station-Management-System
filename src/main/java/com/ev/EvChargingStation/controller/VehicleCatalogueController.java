package com.ev.EvChargingStation.controller;

import com.ev.EvChargingStation.dto.vehicle.VehicleCatalogueResponseDTO;
import com.ev.EvChargingStation.service.VehicleCatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-catalogue")
@RequiredArgsConstructor
public class VehicleCatalogueController {

    private final VehicleCatalogueService service;

    @GetMapping
    public List<VehicleCatalogueResponseDTO> getAll() { return service.getAll(); }

    @GetMapping("/manufacturers")
    public List<String> getManufacturers() { return service.getManufacturers(); }

    @GetMapping("/models")
    public List<VehicleCatalogueResponseDTO> getModels(@RequestParam String manufacturer) { return service.getModels(manufacturer); }

    @GetMapping("/variants")
    public List<VehicleCatalogueResponseDTO> getVariants(@RequestParam String manufacturer, @RequestParam String model) {
        return service.getVariants(manufacturer, model);
    }
}
