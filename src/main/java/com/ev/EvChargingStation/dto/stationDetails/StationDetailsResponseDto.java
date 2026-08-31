package com.ev.EvChargingStation.dto.stationDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationDetailsResponseDto {

    private Long id;

    private String stationName;

    private String address;

    private Double latitude;

    private Double longitude;

    private Double pricePerKwh;

    private Double rating;

    private Double distanceKm;

    private Integer estimatedDriveTimeMinutes;

    private List<ConnectorAvailabilityDto>
            connectorAvailability;
}