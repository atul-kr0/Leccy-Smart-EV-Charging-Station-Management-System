package com.ev.EvChargingStation.controller;

import com.ev.EvChargingStation.dto.booking.BookingRequestDTO;
import com.ev.EvChargingStation.dto.booking.BookingResponseDTO;
import com.ev.EvChargingStation.service.booking.BookingService;
import com.ev.EvChargingStation.service.booking.CancelBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CancelBookingService cancelBookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> bookCharger(
            @Valid @RequestBody BookingRequestDTO request) {

        BookingResponseDTO response = bookingService.bookCharger(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId) {

        cancelBookingService.cancelBooking(bookingId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {

        return ResponseEntity.ok(bookingService.getMyBookings());
    }
}