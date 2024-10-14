package com.wildrep.accountantapp.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(min = 4, max = 20) String username,
        @NotBlank @Size(min = 6) String password,
        @NotBlank @Size(min = 6) String confirmPassword,
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate
) {
}
