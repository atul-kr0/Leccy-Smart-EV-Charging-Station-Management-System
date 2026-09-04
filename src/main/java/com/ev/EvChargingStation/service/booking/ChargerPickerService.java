package com.ev.EvChargingStation.service.booking;

import com.ev.EvChargingStation.dto.booking.ChargerSelectionResult;
import com.ev.EvChargingStation.entity.Booking;
import com.ev.EvChargingStation.entity.Charger;
import com.ev.EvChargingStation.entity.ChargingStation;
import com.ev.EvChargingStation.entity.Vehicle;
import com.ev.EvChargingStation.enums.BookingStatus;
import com.ev.EvChargingStation.enums.ChargerStatus;
import com.ev.EvChargingStation.enums.ConnectorType;
import com.ev.EvChargingStation.exception.NoCompatibleChargerException;
import com.ev.EvChargingStation.repository.BookingRepository;
import com.ev.EvChargingStation.repository.ChargerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChargerPickerService {

    private final BookingRepository bookingRepository;
    private final ChargerRepository chargerRepository;

    /**
     * Calculates estimated energy required (kWh).
     */
    public double calculateEstimatedEnergyRequired(
            Vehicle vehicle,
            Integer currentBatteryPercentage,
            Integer targetBatteryPercentage
    ) {

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }

        if (currentBatteryPercentage == null
                || targetBatteryPercentage == null) {

            throw new IllegalArgumentException(
                    "Battery percentages cannot be null."
            );
        }

        if (currentBatteryPercentage < 0
                || currentBatteryPercentage > 100) {

            throw new IllegalArgumentException(
                    "Current battery percentage must be between 0 and 100."
            );
        }

        if (targetBatteryPercentage < 0
                || targetBatteryPercentage > 100) {

            throw new IllegalArgumentException(
                    "Target battery percentage must be between 0 and 100."
            );
        }

        if (currentBatteryPercentage >= targetBatteryPercentage) {
            throw new IllegalArgumentException(
                    "Target battery percentage must be greater than current battery percentage."
            );
        }

        if (vehicle.getCatalogueVehicle() == null
                || vehicle.getCatalogueVehicle().getBatteryCapacityKwh() == null
                || vehicle.getCatalogueVehicle().getBatteryCapacityKwh() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid vehicle battery capacity."
            );
        }

        return vehicle.getCatalogueVehicle().getBatteryCapacityKwh()
                * (targetBatteryPercentage - currentBatteryPercentage)
                / 100.0;
    }

    /**
     * Calculates estimated charging duration (minutes).
     *
     * TYPE2  -> AC charging -> vehicle max AC power
     * CCS2   -> DC charging -> vehicle max DC power
     * CHADEMO -> DC charging -> vehicle max DC power
     *
     * The actual charging power is the lower of:
     *
     *     charger output power
     *     vehicle maximum charging power
     */
    public Integer calculateEstimatedChargingDuration(
            Vehicle vehicle,
            Charger charger,
            Integer currentBatteryPercentage,
            Integer targetBatteryPercentage
    ) {

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }

        if (charger == null) {
            throw new IllegalArgumentException("Charger cannot be null.");
        }

        if (charger.getOutputPower() == null
                || charger.getOutputPower() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid charger output power."
            );
        }

        double effectivePower = charger.getOutputPower();

        Double vehicleMaxChargingPower =
                getVehicleMaxChargingPower(
                        vehicle,
                        charger
                );

        if (vehicleMaxChargingPower != null
                && vehicleMaxChargingPower > 0) {

            effectivePower =
                    Math.min(
                            effectivePower,
                            vehicleMaxChargingPower
                    );
        }

        if (effectivePower <= 0) {
            throw new IllegalArgumentException(
                    "Invalid effective charging power."
            );
        }

        double energyNeeded =
                calculateEstimatedEnergyRequired(
                        vehicle,
                        currentBatteryPercentage,
                        targetBatteryPercentage
                );

        double chargingHours =
                energyNeeded / effectivePower;

        return (int) Math.ceil(chargingHours * 60);
    }

    /**
     * Returns the vehicle's maximum charging power applicable
     * to the charger being evaluated.
     */
    private Double getVehicleMaxChargingPower(
            Vehicle vehicle,
            Charger charger
    ) {

        ConnectorType chargerConnector =
                charger.getConnectorType();

        if (chargerConnector == null) {
            return null;
        }

        /*
         * Type 2 is AC charging.
         */
        if (chargerConnector == ConnectorType.TYPE2) {

            return vehicle
                    .getCatalogueVehicle()
                    .getMaxAcChargingKw();
        }

        /*
         * CCS2 and CHAdeMO are DC charging.
         */
        if (chargerConnector == ConnectorType.CCS2
                || chargerConnector == ConnectorType.CHADEMO) {

            return vehicle
                    .getCatalogueVehicle()
                    .getMaxDcChargingKw();
        }

        return null;
    }

    /**
     * Waiting time excluding a booking.
     *
     * Used by ReBalanceStation.
     */
    public Integer calculateWaitingTime(
            Charger charger,
            Long excludedBookingId
    ) {

        if (charger == null) {
            throw new IllegalArgumentException(
                    "Charger cannot be null."
            );
        }

        List<Booking> bookings =
                bookingRepository.findByChargerAndStatusInOrderByBookedAtAsc(
                        charger,
                        List.of(
                                BookingStatus.CHARGING,
                                BookingStatus.NOTIFIED,
                                BookingStatus.WAITING
                        )
                );

        int waitingTime = 0;

        for (Booking booking : bookings) {

            /*
             * Ignore the booking being rebalanced.
             */
            if (excludedBookingId != null
                    && excludedBookingId.equals(booking.getId())) {

                continue;
            }

            /*
             * Currently charging booking:
             * only count its remaining charging time.
             */
            if (booking.getStatus() == BookingStatus.CHARGING) {

                waitingTime +=
                        getRemainingChargingTime(booking);

            } else {

                /*
                 * NOTIFIED and WAITING bookings are ahead
                 * in the queue, so their estimated charging
                 * duration contributes to waiting time.
                 */
                waitingTime +=
                        getSafeDuration(
                                booking.getEstimatedChargingDuration()
                        );
            }
        }

        return waitingTime;
    }

    /**
     * Normal waiting time.
     */
    public Integer calculateWaitingTime(Charger charger) {
        return calculateWaitingTime(charger, null);
    }

    /**
     * Calculates remaining charging time for a currently
     * charging booking.
     */
    private int getRemainingChargingTime(Booking booking) {

        int estimated =
                getSafeDuration(
                        booking.getEstimatedChargingDuration()
                );

        /*
         * If the charging session has not recorded a
         * check-in time, use the complete estimated duration.
         */
        if (booking.getCheckedInAt() == null) {
            return estimated;
        }

        long elapsedMinutes =
                Duration.between(
                        booking.getCheckedInAt(),
                        LocalDateTime.now()
                ).toMinutes();

        return Math.max(
                estimated - (int) elapsedMinutes,
                0
        );
    }

    /**
     * Prevents null estimated durations from silently
     * corrupting waiting-time calculations.
     */
    private int getSafeDuration(Integer duration) {

        if (duration == null) {
            throw new IllegalStateException(
                    "Estimated charging duration is missing."
            );
        }

        return duration;
    }

    /**
     * Used by BookingService.
     */
    public ChargerSelectionResult calculatePrediction(
            Charger charger,
            Vehicle vehicle,
            Integer currentBatteryPercentage,
            Integer targetBatteryPercentage
    ) {

        return calculatePrediction(
                charger,
                vehicle,
                currentBatteryPercentage,
                targetBatteryPercentage,
                null
        );
    }

    /**
     * Used by ReBalanceStation.
     */
    public ChargerSelectionResult calculatePrediction(
            Charger charger,
            Vehicle vehicle,
            Integer currentBatteryPercentage,
            Integer targetBatteryPercentage,
            Long excludedBookingId
    ) {

        int waitingTime =
                calculateWaitingTime(
                        charger,
                        excludedBookingId
                );

        int chargingDuration =
                calculateEstimatedChargingDuration(
                        vehicle,
                        charger,
                        currentBatteryPercentage,
                        targetBatteryPercentage
                );

        /*
         * This is the actual metric Leccy uses to determine
         * which charger allows the user to finish charging first.
         *
         * Completion Time =
         *     Waiting Time + Charging Duration
         */
        int estimatedCompletionTime =
                waitingTime + chargingDuration;

        return new ChargerSelectionResult(
                charger,
                waitingTime,
                chargingDuration,
                estimatedCompletionTime
        );
    }

    /**
     * Picks the compatible operational charger that results
     * in the earliest estimated completion time.
     *
     * Primary criterion:
     *     lowest waiting time + charging duration
     *
     * Tie breakers:
     *     1. Lower charging duration
     *     2. Lower waiting time
     */
    public ChargerSelectionResult pickFastestCompletionCharger(
            ChargingStation station,
            Vehicle vehicle,
            Integer currentBatteryPercentage,
            Integer targetBatteryPercentage
    ) {

        if (station == null) {
            throw new IllegalArgumentException(
                    "Station cannot be null."
            );
        }

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null."
            );
        }

        List<Charger> chargers =
                chargerRepository.findByChargingStation(station);

        ChargerSelectionResult bestPrediction = null;

        for (Charger charger : chargers) {

            /*
             * Ignore chargers that are out of service.
             *
             * AVAILABLE and BUSY chargers are both considered,
             * because a BUSY charger can become available after
             * its current queue finishes.
             */
            if (charger.getChargerStatus()
                    == ChargerStatus.OUT_OF_SERVICE) {

                continue;
            }

            /*
             * Skip chargers that the vehicle cannot use.
             */
            if (!isCompatible(vehicle, charger)) {
                continue;
            }

            ChargerSelectionResult prediction =
                    calculatePrediction(
                            charger,
                            vehicle,
                            currentBatteryPercentage,
                            targetBatteryPercentage
                    );

            /*
             * PRIMARY CRITERION:
             *
             * Choose the charger with the lowest
             * total completion time.
             *
             * Completion Time =
             *     Waiting Time + Charging Duration
             */
            if (bestPrediction == null
                    || prediction.getEstimatedCompletionTime()
                    < bestPrediction.getEstimatedCompletionTime()) {

                bestPrediction = prediction;

            } else if (
                    prediction.getEstimatedCompletionTime()
                            == bestPrediction
                            .getEstimatedCompletionTime()
            ) {

                /*
                 * TIE BREAKER #1:
                 *
                 * If both chargers finish at the same time,
                 * prefer the charger requiring less actual
                 * charging time.
                 */
                if (prediction.getEstimatedChargingDuration()
                        < bestPrediction
                        .getEstimatedChargingDuration()) {

                    bestPrediction = prediction;

                } else if (
                        prediction.getEstimatedChargingDuration()
                                == bestPrediction
                                .getEstimatedChargingDuration()
                ) {

                    /*
                     * TIE BREAKER #2:
                     *
                     * If charging duration is also identical,
                     * prefer the charger with less waiting time.
                     */
                    if (prediction.getWaitingTime()
                            < bestPrediction.getWaitingTime()) {

                        bestPrediction = prediction;
                    }
                }
            }
        }

        if (bestPrediction == null) {
            throw new NoCompatibleChargerException(
                    "No compatible operational charger found."
            );
        }

        return bestPrediction;
    }

    /**
     * Determines whether a vehicle can use a charger.
     *
     * Compatibility rules:
     *
     * CCS2 vehicle -> CCS2 charger       YES
     * CCS2 vehicle -> TYPE2 charger      YES
     * CCS2 vehicle -> CHADEMO charger    NO
     *
     * TYPE2 vehicle -> TYPE2 charger      YES
     * TYPE2 vehicle -> CCS2 charger       NO
     * TYPE2 vehicle -> CHADEMO charger    NO
     *
     * CHADEMO vehicle -> CHADEMO charger  YES
     * CHADEMO vehicle -> TYPE2 charger   NO
     * CHADEMO vehicle -> CCS2 charger    NO
     */
    private boolean isCompatible(
            Vehicle vehicle,
            Charger charger
    ) {

        if (vehicle.getCatalogueVehicle() == null) {
            return false;
        }

        ConnectorType vehicleConnector =
                vehicle
                        .getCatalogueVehicle()
                        .getConnectorType();

        ConnectorType chargerConnector =
                charger.getConnectorType();

        if (vehicleConnector == null
                || chargerConnector == null) {

            return false;
        }

        /*
         * Same connector type.
         */
        if (vehicleConnector == chargerConnector) {
            return true;
        }

        /*
         * CCS2 is a combined AC/DC inlet.
         *
         * Therefore a CCS2 vehicle can use a normal
         * Type 2 AC charger.
         */
        return vehicleConnector == ConnectorType.CCS2
                && chargerConnector == ConnectorType.TYPE2;
    }
}