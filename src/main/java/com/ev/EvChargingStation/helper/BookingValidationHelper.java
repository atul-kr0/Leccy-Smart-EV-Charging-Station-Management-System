package com.ev.EvChargingStation.helper;

import com.ev.EvChargingStation.entity.Booking;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.enums.BookingStatus;
import com.ev.EvChargingStation.exception.BookingNotCancellableException;
import com.ev.EvChargingStation.exception.BookingNotFoundException;
import com.ev.EvChargingStation.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingValidationHelper {

    private final BookingRepository bookingRepository;
    private final UserHelper userHelper;

    public void validateNoActiveBooking(User user) {

        if (bookingRepository.existsByUserAndStatusIn(
                user,
                List.of(
                        BookingStatus.WAITING,
                        BookingStatus.NOTIFIED,
                        BookingStatus.CHARGING
                )
        )) {

            throw new IllegalStateException(
                    "You already have an active booking."
            );
        }
    }

    public Booking getOwnedBooking(Long bookingId) {

        User user = userHelper.getLoggedInUser();

        return bookingRepository.findByIdAndUser(bookingId, user)
                .orElseThrow(() ->
                        new BookingNotFoundException("Booking not found: " + bookingId));
    }

    public void validateCancellation(Booking booking) {

        BookingStatus status = booking.getStatus();

        if (status != BookingStatus.WAITING &&
                status != BookingStatus.NOTIFIED) {

            throw new BookingNotCancellableException(
                    "Booking cannot be cancelled."
            );
        }
    }

}