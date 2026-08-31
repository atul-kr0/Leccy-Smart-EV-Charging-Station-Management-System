package com.ev.EvChargingStation.dto.stationDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationDetailsRequestDto {

    private Long stationId;

    private Double userLatitude;

    private Double userLongitude;
}