package com.ev.EvChargingStation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // BOOKING
    // ============================================================

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<String> handleBookingNotFoundException(
            BookingNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BookingNotCancellableException.class)
    public ResponseEntity<String> handleBookingNotCancellableException(
            BookingNotCancellableException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BookingStatusInvalidException.class)
    public ResponseEntity<String> handleBookingStatusInvalidException(
            BookingStatusInvalidException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }


    // ============================================================
    // CHARGER
    // ============================================================

    @ExceptionHandler(ChargerNotFoundException.class)
    public ResponseEntity<String> handleChargerNotFoundException(
            ChargerNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ChargerUnavailableException.class)
    public ResponseEntity<String> handleChargerUnavailableException(
            ChargerUnavailableException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }


    // ============================================================
    // CHARGING SESSION
    // ============================================================

    @ExceptionHandler(ChargingSessionNotFoundException.class)
    public ResponseEntity<String> handleChargingSessionNotFoundException(
            ChargingSessionNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }


    // ============================================================
    // CHARGING STATION
    // ============================================================

    @ExceptionHandler(ChargingStationNotFoundException.class)
    public ResponseEntity<String> handleChargingStationNotFoundException(
            ChargingStationNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(StationUnavailableException.class)
    public ResponseEntity<String> handleStationUnavailableException(
            StationUnavailableException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }


    // ============================================================
    // AUTH / USER
    // ============================================================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(
            InvalidTokenException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(
            UserNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<String> handleNotificationNotFoundException(
            NotificationNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }


    // ============================================================
    // REGISTRATION VALIDATION
    // ============================================================

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRegistration(
            InvalidRegistrationException ex) {

        Map<String, String> errors = new HashMap<>();

        errors.put("phoneNumber", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }


    // ============================================================
    // VEHICLE
    // ============================================================

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<String> handleVehicleNotFoundException(
            VehicleNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }


    // ============================================================
    // LOCATION / ROUTING
    // ============================================================

    @ExceptionHandler(InvalidCoordinatesException.class)
    public ResponseEntity<String> handleInvalidCoordinatesException(
            InvalidCoordinatesException ex) {

        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(RouteServiceException.class)
    public ResponseEntity<String> handleRouteServiceException(
            RouteServiceException ex) {

        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }


    // ============================================================
    // BEAN VALIDATION ERRORS
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {

                    String field = error.getField();
                    String message = error.getDefaultMessage();

                    // Keep only the first validation error for each field.
                    errors.putIfAbsent(field, message);
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}