package com.ev.EvChargingStation.controller;

import com.ev.EvChargingStation.dto.user.UpdateProfileRequestDTO;
import com.ev.EvChargingStation.dto.user.UserProfileResponseDTO;
import com.ev.EvChargingStation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile() {

        return ResponseEntity.ok(
                userService.getMyProfile()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request) {

        return ResponseEntity.ok(
                userService.updateMyProfile(request)
        );
    }
}