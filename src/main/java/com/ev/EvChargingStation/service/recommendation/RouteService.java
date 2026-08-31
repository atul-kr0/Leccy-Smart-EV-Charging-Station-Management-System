package com.ev.EvChargingStation.service.recommendation;

import com.ev.EvChargingStation.service.recommendation.model.CandidateStation;
import com.ev.EvChargingStation.service.recommendation.model.RouteInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final OpenRouteServiceClient openRouteServiceClient;


    /**
     * Get route information between user and station.
     *
     * Used by StationDetailsService as well as
     * the recommendation system.
     */
    public RouteInfo getRoute(
            double userLatitude,
            double userLongitude,
            double stationLatitude,
            double stationLongitude
    ) {

        return openRouteServiceClient.getRoute(
                userLatitude,
                userLongitude,
                stationLatitude,
                stationLongitude
        );
    }


    /**
     * Enrich recommendation candidates with
     * real road distance and driving ETA.
     */
    public List<CandidateStation> enrichCandidatesWithRoutes(
            List<CandidateStation> candidates,
            double userLatitude,
            double userLongitude
    ) {

        for (CandidateStation candidate : candidates) {

            RouteInfo route =
                    getRoute(
                            userLatitude,
                            userLongitude,
                            candidate.getStation().getLatitude(),
                            candidate.getStation().getLongitude()
                    );

            candidate.setDrivingDistanceKm(
                    route.drivingDistanceKm()
            );

            candidate.setEstimatedTravelTimeMinutes(
                    route.estimatedTravelTimeMinutes()
            );
        }

        return candidates;
    }
}