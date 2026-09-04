package com.ev.EvChargingStation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(
            min = 3,
            max = 50,
            message = "Full name must be between 3 and 50 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z]+(?:[ '-][A-Za-z]+)*$",
            message = "Enter a valid full name"
    )
    private String fullName;


    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Enter a valid email address"
    )
    private String email;


    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters"
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one letter, one number, and one special character"
    )
    private String password;


    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid 10-digit Indian phone number"
    )
    private String phoneNumber;
}