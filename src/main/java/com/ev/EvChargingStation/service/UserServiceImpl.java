package com.ev.EvChargingStation.service;


import com.ev.EvChargingStation.dto.user.UpdateProfileRequestDTO;
import com.ev.EvChargingStation.dto.user.UserProfileResponseDTO;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponseDTO getMyProfile() {

        User user = getAuthenticatedUser();

        return toResponse(user);
    }

    @Override
    public UserProfileResponseDTO updateMyProfile(
            UpdateProfileRequestDTO request) {

        User user = getAuthenticatedUser();

        /*
         * Don't allow email/role/password/id
         * to be changed from profile.
         */

        user.setName(request.getName());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        /*
         * If your CustomUserDetailsService returns
         * a different UserDetails implementation,
         * use the email from Authentication.
         */

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        ));
    }

    private UserProfileResponseDTO toResponse(User user) {

        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(
                        user.getRole() != null
                                ? user.getRole().name()
                                : null
                )
                .createdAt(user.getCreatedAt())
                .build();
    }
}
