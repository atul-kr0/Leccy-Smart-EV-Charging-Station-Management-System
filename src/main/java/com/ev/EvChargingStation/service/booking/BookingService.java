package com.ev.EvChargingStation.service.booking;

import com.ev.EvChargingStation.dto.booking.BookingRequestDTO;
import com.ev.EvChargingStation.dto.booking.BookingResponseDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface BookingService {

    public BookingResponseDTO bookCharger(BookingRequestDTO request);

    List<BookingResponseDTO> getMyBookings();

}
