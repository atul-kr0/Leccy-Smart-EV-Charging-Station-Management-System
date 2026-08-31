package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.user.UpdateProfileRequestDTO;
import com.ev.EvChargingStation.dto.user.UserProfileResponseDTO;

public interface UserService {

    UserProfileResponseDTO getMyProfile();

    UserProfileResponseDTO updateMyProfile(
            UpdateProfileRequestDTO request
    );
}
