package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.auth.AuthResponseDTO;
import com.ev.EvChargingStation.dto.auth.LoginRequestDTO;
import com.ev.EvChargingStation.dto.auth.LoginResponseDTO;
import com.ev.EvChargingStation.dto.auth.RegisterRequestDTO;
import com.ev.EvChargingStation.entity.User;
import com.ev.EvChargingStation.enums.Role;
import com.ev.EvChargingStation.exception.EmailAlreadyExistsException;
import com.ev.EvChargingStation.exception.InvalidRegistrationException;
import com.ev.EvChargingStation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    // ============================================================
    // REGISTER
    // ============================================================

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {

        // --------------------------------------------------------
        // NORMALIZE INPUT
        // --------------------------------------------------------

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String phoneNumber = request.getPhoneNumber()
                .trim();


        // --------------------------------------------------------
        // EMAIL DUPLICATE CHECK
        // --------------------------------------------------------

        if (userRepository.existsByEmail(email)) {

            throw new EmailAlreadyExistsException(
                    "An account with this email already exists"
            );
        }


        // --------------------------------------------------------
        // PHONE VALIDATION
        // --------------------------------------------------------

        validatePhoneNumber(phoneNumber);


        // --------------------------------------------------------
        // PHONE DUPLICATE CHECK
        // --------------------------------------------------------

        if (userRepository.existsByPhone(phoneNumber)) {

            throw new InvalidRegistrationException(
                    "An account with this phone number already exists"
            );
        }


        // --------------------------------------------------------
        // CREATE USER
        // --------------------------------------------------------

        User user = new User();

        user.setName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setPhone(phoneNumber);
        user.setRole(Role.USER);


        // --------------------------------------------------------
        // SAVE
        // --------------------------------------------------------

        User saved = userRepository.save(user);


        // --------------------------------------------------------
        // RESPONSE
        // --------------------------------------------------------

        AuthResponseDTO response = new AuthResponseDTO();

        response.setUserId(saved.getId());
        response.setFullName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole());

        return response;
    }


    // ============================================================
    // PHONE VALIDATION
    // ============================================================

    private void validatePhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {

            throw new InvalidRegistrationException(
                    "Phone number is required"
            );
        }


        // Must be exactly 10 digits
        if (!phoneNumber.matches("^[6-9][0-9]{9}$")) {

            throw new InvalidRegistrationException(
                    "Enter a valid 10-digit Indian phone number"
            );
        }


        // Reject numbers such as:
        // 6666666666
        // 7777777777
        // 8888888888
        // 9999999999

        if (phoneNumber.matches("^[6-9](\\d)\\1{8}$")) {
            throw new InvalidRegistrationException(
                    "Enter a valid phone number"
            );
        }
    }


    // ============================================================
    // LOGIN
    // ============================================================

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );


        User user = (User) authentication.getPrincipal();


        String token = jwtService.generateToken(user);


        LoginResponseDTO response = new LoginResponseDTO();

        response.setToken(token);
        response.setType("Bearer");
        response.setUserId(user.getId());
        response.setFullName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }
}