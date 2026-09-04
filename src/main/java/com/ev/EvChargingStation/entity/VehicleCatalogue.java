package com.ev.EvChargingStation.entity;

import com.ev.EvChargingStation.enums.ConnectorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle_catalogue", uniqueConstraints = @UniqueConstraint(columnNames = {"manufacturer", "model", "variant"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCatalogue {

    @Id
    private Long id;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String variant;

    @Column(nullable = false)
    private Double batteryCapacityKwh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectorType connectorType;

    private Double maxAcChargingKw;
    private Double maxDcChargingKw;

    @Column(nullable = false)
    private String imagePath;
}
