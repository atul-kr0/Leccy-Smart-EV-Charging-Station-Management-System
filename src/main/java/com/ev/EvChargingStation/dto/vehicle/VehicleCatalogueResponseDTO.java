package com.ev.EvChargingStation.dto.vehicle;

import com.ev.EvChargingStation.enums.ConnectorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCatalogueResponseDTO {
    private Long id;
    private String manufacturer;
    private String model;
    private String variant;
    private Double batteryCapacityKwh;
    private ConnectorType connectorType;
    private Double maxAcChargingKw;
    private Double maxDcChargingKw;
    private String imagePath;
}
