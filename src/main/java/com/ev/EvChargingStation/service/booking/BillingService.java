package com.ev.EvChargingStation.service.booking;

import com.ev.EvChargingStation.constant.BillingConstants;
import com.ev.EvChargingStation.dto.billing.BillingSummary;
import com.ev.EvChargingStation.entity.ChargingSession;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BillingService {

    public BillingSummary generateBill(ChargingSession session){

        long chargingMinutes = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();

        double effectivePower = session.getCharger().getOutputPower();
        Double vehicleMax = session.getBooking().getVehicle().getCatalogueVehicle().getMaxDcChargingKw();
        if (vehicleMax != null && vehicleMax > 0) {
            effectivePower = Math.min(effectivePower, vehicleMax);
        }

        double energyDelivered = effectivePower * chargingMinutes / 60.0;

        double chargingAmount =
                energyDelivered
                        * session.getPricePerKwh();

        double penaltyAmount = 0;

        if(session.getEndedEarly()){

            long remainingMinutes =
                    Duration.between(
                            session.getEndTime(),
                            session.getPlannedEndTime()
                    ).toMinutes();

            penaltyAmount =
                    remainingMinutes *
                            BillingConstants.EARLY_STOP_PENALTY_PER_MINUTE;
        }

        return new BillingSummary(

                energyDelivered,
                chargingAmount,
                penaltyAmount,
                chargingAmount + penaltyAmount
        );
    }

}