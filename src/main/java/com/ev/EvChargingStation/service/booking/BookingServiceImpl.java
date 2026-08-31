package com.ev.EvChargingStation.service.booking;

import com.ev.EvChargingStation.dto.booking.BookingRequestDTO;
import com.ev.EvChargingStation.dto.booking.BookingResponseDTO;
import com.ev.EvChargingStation.dto.booking.ChargerSelectionResult;
import com.ev.EvChargingStation.entity.Booking;
import com.ev.EvChargingStation.entity.ChargingStation;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.entity.Vehicle;
import com.ev.EvChargingStation.enums.BookingStatus;
import com.ev.EvChargingStation.enums.StationStatus;
import com.ev.EvChargingStation.exception.StationUnavailableException;
import com.ev.EvChargingStation.helper.QueueHelper;
import com.ev.EvChargingStation.helper.StationHelper;
import com.ev.EvChargingStation.helper.BookingValidationHelper;
import com.ev.EvChargingStation.helper.VehicleHelper;
import com.ev.EvChargingStation.mapper.BookingMapper;
import com.ev.EvChargingStation.repository.BookingRepository;
import com.ev.EvChargingStation.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService{

    private final VehicleHelper vehicleHelper;
    private final ChargerPickerService chargerPickerService;
    private final StationHelper stationHelper;
    private final QueueHelper queueHelper;
    private final TokenService tokenService;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final BookingValidationHelper bookingValidator;
    private final NotifyNextService notifyNextService;
    private final SecurityUtil securityUtil;

    @Override
    public BookingResponseDTO bookCharger(BookingRequestDTO request){

        Vehicle vehicle = vehicleHelper.validateUsersVehicle(request.getVehicleId());

        bookingValidator.validateNoActiveBooking(vehicle.getUser());

        ChargingStation station = stationHelper.getStation(request.getStationId());


        if (station.getStationStatus() != StationStatus.ACTIVE) {
            throw new StationUnavailableException("This station is currently unavailable.");
        }

        ChargerSelectionResult selection = chargerPickerService
                .pickFastestCompletionCharger(
                        station,
                        vehicle,
                        request.getCurrentBatteryPercentage(),
                        request.getTargetBatteryPercentage()
                );

//        Integer queuePosition = queueHelper.calculateQueuePosition(selection.getCharger());

        String token = tokenService.generateUniqueToken();

        Booking booking = bookingMapper.toEntity(request);

        booking.setUser(vehicle.getUser());
        booking.setVehicle(vehicle);
        booking.setChargingStation(station);
        booking.setCharger(selection.getCharger());

//        booking.setQueuePosition(queuePosition);
        booking.setTokenNumber(token);

        booking.setEstimatedChargingDuration(selection.getEstimatedChargingDuration());

        booking.setStatus(BookingStatus.WAITING);
        booking.setBookedAt(LocalDateTime.now());

        booking = bookingRepository.save(booking);

        Long stationId = booking.getChargingStation().getId();

        notifyNextService.notifyEligibleBookings(stationId);

        return bookingMapper.entityToDto(booking);
    }

    @Override
    public List<BookingResponseDTO> getMyBookings() {

        User user = securityUtil.getCurrentUser();

        return bookingRepository
                .findByUserIdOrderByBookedAtDesc(user.getId())
                .stream()
                .map(bookingMapper::entityToDto)
                .toList();
    }

}
