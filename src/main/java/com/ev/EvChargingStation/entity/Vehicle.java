package com.ev.EvChargingStation.entity;

import com.ev.EvChargingStation.enums.ConnectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Registration number is required")
    @Pattern(
            regexp = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$",
            message = "Enter a valid vehicle registration number (e.g. MH12AB1234)"
    )
    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogue_vehicle_id")
    private VehicleCatalogue catalogueVehicle;

    // Legacy columns retained temporarily so existing rows can be migrated safely.
    @Deprecated
    private String manufacturer;

    @Deprecated
    private String model;

    @Deprecated
    private Double batteryCapacity;

    @Deprecated
    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;

    @Deprecated
    private Double maxChargingPower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "vehicle")
    private List<Booking> bookings = new ArrayList<>();
}
