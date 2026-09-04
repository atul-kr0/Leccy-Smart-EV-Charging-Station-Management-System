package com.ev.EvChargingStation.service.booking;

import com.ev.EvChargingStation.dto.booking.ChargerSelectionResult;
import com.ev.EvChargingStation.entity.Booking;
import com.ev.EvChargingStation.entity.Charger;
import com.ev.EvChargingStation.entity.ChargingStation;
import com.ev.EvChargingStation.entity.Vehicle;
import com.ev.EvChargingStation.enums.BookingStatus;
import com.ev.EvChargingStation.enums.ChargerStatus;
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

        if (currentBatteryPercentage == null || targetBatteryPercentage == null) {
            throw new IllegalArgumentException("Battery percentages cannot be null.");
        }

        if (currentBatteryPercentage < 0 || currentBatteryPercentage > 100) {
            throw new IllegalArgumentException(
                    "Current battery percentage must be between 0 and 100."
            );
        }

        if (targetBatteryPercentage < 0 || targetBatteryPercentage > 100) {
            throw new IllegalArgumentException(
                    "Target battery percentage must be between 0 and 100."
            );
        }

        if (currentBatteryPercentage >= targetBatteryPercentage) {
            throw new IllegalArgumentException(
                    "Target battery percentage must be greater than current battery percentage."
            );
        }

        if (vehicle.getCatalogueVehicle().getBatteryCapacityKwh() == null
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

        if (charger.getOutputPower() == null || charger.getOutputPower() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid charger output power."
            );
        }

        double effectivePower = charger.getOutputPower();

        Double vehicleMaxChargingPower = vehicle.getCatalogueVehicle().getMaxDcChargingKw();
        if (vehicleMaxChargingPower == null || vehicleMaxChargingPower <= 0) {
            vehicleMaxChargingPower = vehicle.getCatalogueVehicle().getMaxAcChargingKw();
        }

        if (vehicleMaxChargingPower != null && vehicleMaxChargingPower > 0) {
            effectivePower = Math.min(effectivePower, vehicleMaxChargingPower);
        }

        double energyNeeded = calculateEstimatedEnergyRequired(
                vehicle,
                currentBatteryPercentage,
                targetBatteryPercentage
        );

        double chargingHours = energyNeeded / effectivePower;

        return (int) Math.ceil(chargingHours * 60);
    }

    /**
     * Waiting time excluding a booking (used by ReBalanceStation).
     */
    public Integer calculateWaitingTime(
            Charger charger,
            Long excludedBookingId
    ) {

        if (charger == null) {
            throw new IllegalArgumentException("Charger cannot be null.");
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

            if (excludedBookingId != null
                    && excludedBookingId.equals(booking.getId())) {
                continue;
            }

            if (booking.getStatus() == BookingStatus.CHARGING) {
                waitingTime += getRemainingChargingTime(booking);
            } else {
                waitingTime += getSafeDuration(
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

    private int getRemainingChargingTime(Booking booking) {

        int estimated = getSafeDuration(
                booking.getEstimatedChargingDuration()
        );

        if (booking.getCheckedInAt() == null) {
            return estimated;
        }

        long elapsedMinutes =
                Duration.between(
                        booking.getCheckedInAt(),
                        LocalDateTime.now()
                ).toMinutes();

        return Math.max(estimated - (int) elapsedMinutes, 0);
    }

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

        return new ChargerSelectionResult(
                charger,
                waitingTime,
                chargingDuration,
                waitingTime + chargingDuration
        );
    }

    /**
     * Picks the compatible operational charger that results in the earliest
     * estimated completion time.
     */
    public ChargerSelectionResult pickFastestCompletionCharger(
            ChargingStation station,
            Vehicle vehicle,
            Integer currentBatteryPercentage,
            Integer targetBatteryPercentage
    ) {

        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null.");
        }

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }

        List<Charger> chargers =
                chargerRepository.findByChargingStation(station);

        ChargerSelectionResult bestPrediction = null;

        for (Charger charger : chargers) {

            // Ignore chargers that are unavailable for use.
            if (charger.getChargerStatus() == ChargerStatus.OUT_OF_SERVICE) {
                continue;
            }

            // Skip incompatible chargers.
            if (charger.getConnectorType() != vehicle.getCatalogueVehicle().getConnectorType()) {
                continue;
            }

            ChargerSelectionResult prediction =
                    calculatePrediction(
                            charger,
                            vehicle,
                            currentBatteryPercentage,
                            targetBatteryPercentage
                    );

            if (bestPrediction == null
                    || prediction.getEstimatedCompletionTime()
                    < bestPrediction.getEstimatedCompletionTime()) {

                bestPrediction = prediction;
            }
        }

        if (bestPrediction == null) {
            throw new NoCompatibleChargerException(
                    "No compatible operational charger found."
            );
        }

        return bestPrediction;
    }

}