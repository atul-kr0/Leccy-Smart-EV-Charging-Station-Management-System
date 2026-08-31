package com.ev.EvChargingStation.repository;

import com.ev.EvChargingStation.entity.Booking;
import com.ev.EvChargingStation.entity.Charger;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {

//    List<Booking> findByChargerAndStatusInOrderByQueuePositionAsc(
//            Charger charger,
//            List<BookingStatus> statuses
//    );

    List<Booking> findByChargerAndStatusInOrderByBookedAtAsc(
            Charger charger,
            List<BookingStatus> statuses
    );

    Integer countByChargerAndStatusIn(
            Charger charger,
            List<BookingStatus> statuses
    );

    boolean existsByTokenNumber(String tokenNumber);

    boolean existsByUserAndStatusIn(
            User user,
            List<BookingStatus> statuses
    );

    Optional<Booking> findByIdAndUser(Long bookingId, User user);

    List<Booking> findByChargingStationIdAndStatusOrderByBookedAtAsc(
            Long stationId,
            BookingStatus status
    );

    Optional<Booking> findFirstByChargerIdAndStatusOrderByBookedAtAsc(
            Long chargerId,
            BookingStatus status
    );

    Optional<Booking> findFirstByChargerIdAndStatusInOrderByBookedAtAsc(
            Long chargerId,
            List<BookingStatus> statuses
    );

    Optional<Booking> findByTokenNumber(String token);

    List<Booking> findByStatusAndNotifiedAtBefore(
            BookingStatus status,
            LocalDateTime notifiedAt
    );

    Optional<Booking> findByUserAndStatus(
            User user,
            BookingStatus status
    );

    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);
}
