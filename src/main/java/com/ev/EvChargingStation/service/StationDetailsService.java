package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.stationDetails.ConnectorAvailabilityDto;
import com.ev.EvChargingStation.dto.stationDetails.StationDetailsRequestDto;
import com.ev.EvChargingStation.dto.stationDetails.StationDetailsResponseDto;
import com.ev.EvChargingStation.entity.Charger;
import com.ev.EvChargingStation.entity.ChargingStation;
import com.ev.EvChargingStation.enums.ChargerStatus;
import com.ev.EvChargingStation.exception.ChargingStationNotFoundException;
import com.ev.EvChargingStation.repository.ChargerRepository;
import com.ev.EvChargingStation.repository.ChargingStationRepository;
import com.ev.EvChargingStation.service.booking.ChargerPickerService;
import com.ev.EvChargingStation.service.recommendation.RouteService;
import com.ev.EvChargingStation.service.recommendation.model.RouteInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StationDetailsService {

    private final ChargingStationRepository chargingStationRepository;

    private final ChargerRepository chargerRepository;

    private final ChargerPickerService chargerPickerService;

    private final RouteService routeService;


    // =========================================================
    // STATION DETAILS
    // =========================================================

    public StationDetailsResponseDto getStationDetails(
            StationDetailsRequestDto request
    ) {

        if (request == null || request.getStationId() == null) {

            throw new IllegalArgumentException(
                    "Station ID is required."
            );
        }


        ChargingStation station =
                chargingStationRepository
                        .findById(request.getStationId())
                        .orElseThrow(() ->
                                new ChargingStationNotFoundException(
                                        "Charging station not found."
                                )
                        );


        // -----------------------------------------------------
        // GET CHARGERS
        // -----------------------------------------------------

        List<Charger> chargers =
                chargerRepository.findByChargingStationId(
                        station.getId()
                );


        // -----------------------------------------------------
        // CONNECTOR AVAILABILITY + WAITING TIME
        // -----------------------------------------------------

        List<ConnectorAvailabilityDto>
                connectorAvailability =
                buildConnectorAvailability(
                        chargers
                );


        // -----------------------------------------------------
        // REAL ROAD DISTANCE + ETA
        // -----------------------------------------------------

        Double distanceKm = null;

        Integer estimatedDriveTimeMinutes = null;


        if (
                request.getUserLatitude() != null
                        && request.getUserLongitude() != null
                        && station.getLatitude() != null
                        && station.getLongitude() != null
        ) {

            try {

                RouteInfo route =
                        routeService.getRoute(
                                request.getUserLatitude(),
                                request.getUserLongitude(),
                                station.getLatitude(),
                                station.getLongitude()
                        );


                if (route != null) {

                    distanceKm =
                            route.drivingDistanceKm();

                    estimatedDriveTimeMinutes =
                            route.estimatedTravelTimeMinutes();
                }

            } catch (Exception e) {

                /*
                 * Routing failure should NOT make
                 * the station popup fail.
                 *
                 * Distance/ETA simply remain null.
                 */

                System.err.println(
                        "Unable to calculate route for station "
                                + station.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        return new StationDetailsResponseDto(

                station.getId(),

                station.getStationName(),

                station.getAddress(),

                station.getLatitude(),

                station.getLongitude(),

                station.getPricePerKwh(),

                station.getRating(),

                distanceKm,

                estimatedDriveTimeMinutes,

                connectorAvailability
        );
    }


    // =========================================================
    // CONNECTOR AVAILABILITY
    // =========================================================

    private List<ConnectorAvailabilityDto>
    buildConnectorAvailability(
            List<Charger> chargers
    ) {

        if (
                chargers == null
                        || chargers.isEmpty()
        ) {

            return new ArrayList<>();
        }


        Map<String, List<Charger>> grouped =
                chargers.stream()

                        .filter(
                                charger ->
                                        charger.getConnectorType()
                                                != null
                        )

                        .collect(
                                Collectors.groupingBy(
                                        charger ->
                                                charger
                                                        .getConnectorType()
                                                        .name()
                                )
                        );


        List<ConnectorAvailabilityDto> result =
                new ArrayList<>();


        for (
                Map.Entry<String, List<Charger>> entry :
                grouped.entrySet()
        ) {

            String connectorType =
                    entry.getKey();

            List<Charger> connectorChargers =
                    entry.getValue();


            int available = 0;

            int busy = 0;

            int unavailable = 0;


            // -------------------------------------------------
            // COUNT PHYSICAL CHARGER STATUS
            // -------------------------------------------------

            for (
                    Charger charger :
                    connectorChargers
            ) {

                if (
                        charger.getChargerStatus()
                                == ChargerStatus.AVAILABLE
                ) {

                    available++;

                } else if (
                        charger.getChargerStatus()
                                == ChargerStatus.BUSY
                ) {

                    busy++;

                } else if (
                        charger.getChargerStatus()
                                == ChargerStatus.OUT_OF_SERVICE
                ) {

                    unavailable++;
                }
            }


            // -------------------------------------------------
            // EFFECTIVE WAITING TIME
            // -------------------------------------------------

            int waitingTime =
                    calculateConnectorWaitingTime(
                            connectorChargers
                    );


            result.add(
                    new ConnectorAvailabilityDto(

                            connectorType,

                            available,

                            busy,

                            unavailable,

                            waitingTime
                    )
            );
        }


        // Keep UI ordering predictable.

        result.sort(
                Comparator.comparing(
                        ConnectorAvailabilityDto
                                ::getConnectorType
                )
        );


        return result;
    }


    // =========================================================
    // EFFECTIVE WAITING TIME
    // =========================================================

    private int calculateConnectorWaitingTime(
            List<Charger> chargers
    ) {

        Integer lowestWaitingTime = null;


        for (
                Charger charger :
                chargers
        ) {

            /*
             * OUT_OF_SERVICE:
             *
             * This charger cannot serve the user.
             */

            if (
                    charger.getChargerStatus()
                            == ChargerStatus.OUT_OF_SERVICE
            ) {

                continue;
            }


            /*
             * IMPORTANT:
             *
             * Do NOT return 0 just because the physical
             * charger status is AVAILABLE.
             *
             * There may already be a WAITING or NOTIFIED
             * booking assigned to this charger.
             *
             * ChargerPickerService.calculateWaitingTime()
             * knows about:
             *
             *   CHARGING
             *   NOTIFIED
             *   WAITING
             *
             * and calculates the effective queue for us.
             */

            int waitingTime =
                    chargerPickerService
                            .calculateWaitingTime(
                                    charger
                            );


            /*
             * Multiple chargers of the same connector type
             * work in parallel.
             *
             * Therefore we want the charger that becomes
             * available first.
             *
             * Example:
             *
             * CCS2 #1 -> 25 min
             * CCS2 #2 -> 60 min
             *
             * Result -> 25 min
             *
             * NOT 85 min.
             */

            if (
                    lowestWaitingTime == null
                            || waitingTime
                            < lowestWaitingTime
            ) {

                lowestWaitingTime =
                        waitingTime;
            }
        }


        /*
         * Every charger of this connector type is
         * OUT_OF_SERVICE.
         */

        if (lowestWaitingTime == null) {

            return -1;
        }


        return lowestWaitingTime;
    }
}