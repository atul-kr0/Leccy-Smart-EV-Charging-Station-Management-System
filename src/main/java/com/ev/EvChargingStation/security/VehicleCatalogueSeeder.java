package com.ev.EvChargingStation.security;

import com.ev.EvChargingStation.entity.Vehicle;
import com.ev.EvChargingStation.entity.VehicleCatalogue;
import com.ev.EvChargingStation.enums.ConnectorType;
import com.ev.EvChargingStation.repository.VehicleCatalogueRepository;
import com.ev.EvChargingStation.repository.VehicleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class VehicleCatalogueSeeder implements CommandLineRunner {

    private final VehicleCatalogueRepository repository;
    private final VehicleRepository vehicleRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        JsonNode root;
        try (InputStream in = new ClassPathResource("vehicle-catalogue.json").getInputStream()) {
            root = objectMapper.readTree(in);
        }

        for (JsonNode node : root.get("vehicles")) {
            Long id = node.get("vehicleId").asLong();
            VehicleCatalogue v = repository.findById(id).orElseGet(VehicleCatalogue::new);
            v.setId(id);
            v.setManufacturer(node.get("make").asText());
            v.setModel(node.get("model").asText());
            v.setVariant(node.get("variant").asText());
            v.setBatteryCapacityKwh(node.get("batteryCapacityKwh").asDouble());
            v.setConnectorType(ConnectorType.valueOf(node.get("connectorType").asText().replace("Type 2", "TYPE2")));
            v.setMaxAcChargingKw(node.get("maxAcChargingKw").isNull() ? null : node.get("maxAcChargingKw").asDouble());
            v.setMaxDcChargingKw(node.get("maxDcChargingKw").isNull() ? null : node.get("maxDcChargingKw").asDouble());
            v.setImagePath(node.get("imagePath").asText());
            repository.save(v);
        }

        // Migrate vehicles created before the catalogue relationship existed.
        vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getCatalogueVehicle() == null)
                .forEach(vehicle -> {
                    var matches = repository.findByManufacturerIgnoreCaseAndModelIgnoreCaseOrderByVariantAsc(
                            vehicle.getManufacturer(), vehicle.getModel());
                    if (matches.size() == 1) {
                        vehicle.setCatalogueVehicle(matches.get(0));
                    } else if (!matches.isEmpty()) {
                        var exact = matches.stream().filter(c ->
                                java.util.Objects.equals(c.getBatteryCapacityKwh(), vehicle.getBatteryCapacity())
                                        && java.util.Objects.equals(c.getConnectorType(), vehicle.getConnectorType())
                                        && (vehicle.getMaxChargingPower() == null
                                            || java.util.Objects.equals(c.getMaxDcChargingKw(), vehicle.getMaxChargingPower()))
                        ).findFirst();
                        exact.ifPresent(vehicle::setCatalogueVehicle);
                    }
                    if (vehicle.getCatalogueVehicle() != null) {
                        VehicleCatalogue c = vehicle.getCatalogueVehicle();
                        vehicle.setManufacturer(c.getManufacturer());
                        vehicle.setModel(c.getModel());
                        vehicle.setBatteryCapacity(c.getBatteryCapacityKwh());
                        vehicle.setConnectorType(c.getConnectorType());
                        vehicle.setMaxChargingPower(c.getMaxDcChargingKw());
                        vehicleRepository.save(vehicle);
                    }
                });
    }
}
