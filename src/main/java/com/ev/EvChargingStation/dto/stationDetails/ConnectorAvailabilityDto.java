package com.ev.EvChargingStation.dto.stationDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorAvailabilityDto {

    private String connectorType;

    private Integer available;

    private Integer busy;

    private Integer unavailable;

    private Integer waitingTimeMinutes;
}