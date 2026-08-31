package com.ev.EvChargingStation.repository;

import com.ev.EvChargingStation.entity.Booking;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

}
