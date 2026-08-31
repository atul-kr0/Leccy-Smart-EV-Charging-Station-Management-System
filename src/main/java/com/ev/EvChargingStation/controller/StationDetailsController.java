package com.ev.EvChargingStation.controller;

import com.ev.EvChargingStation.dto.stationDetails.StationDetailsRequestDto;
import com.ev.EvChargingStation.dto.stationDetails.StationDetailsResponseDto;
import com.ev.EvChargingStation.service.StationDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/station-details")
@RequiredArgsConstructor
public class StationDetailsController {

    private final StationDetailsService stationDetailsService;


    @PostMapping
    public ResponseEntity<StationDetailsResponseDto> getStationDetails(
            @RequestBody StationDetailsRequestDto request
    ) {

        StationDetailsResponseDto response =
                stationDetailsService.getStationDetails(request);

        return ResponseEntity.ok(response);
    }
}