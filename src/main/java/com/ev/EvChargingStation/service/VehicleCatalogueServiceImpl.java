package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.vehicle.VehicleCatalogueResponseDTO;
import com.ev.EvChargingStation.entity.VehicleCatalogue;
import com.ev.EvChargingStation.repository.VehicleCatalogueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleCatalogueServiceImpl implements VehicleCatalogueService {

    private final VehicleCatalogueRepository repository;

    @Override
    public List<VehicleCatalogueResponseDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<String> getManufacturers() {
        return repository.findAll().stream()
                .map(VehicleCatalogue::getManufacturer)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<VehicleCatalogueResponseDTO> getModels(String manufacturer) {
        return repository.findByManufacturerIgnoreCaseOrderByModelAscVariantAsc(manufacturer).stream()
                .collect(Collectors.toMap(
                        VehicleCatalogue::getModel,
                        this::toDto,
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new))
                .values().stream().toList();
    }

    @Override
    public List<VehicleCatalogueResponseDTO> getVariants(String manufacturer, String model) {
        return repository.findByManufacturerIgnoreCaseAndModelIgnoreCaseOrderByVariantAsc(manufacturer, model)
                .stream().map(this::toDto).toList();
    }

    private VehicleCatalogueResponseDTO toDto(VehicleCatalogue v) {
        return new VehicleCatalogueResponseDTO(v.getId(), v.getManufacturer(), v.getModel(), v.getVariant(),
                v.getBatteryCapacityKwh(), v.getConnectorType(), v.getMaxAcChargingKw(), v.getMaxDcChargingKw(), v.getImagePath());
    }
}
