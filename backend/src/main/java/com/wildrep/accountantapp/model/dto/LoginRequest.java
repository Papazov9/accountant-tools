package com.wildrep.accountantapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 4, max = 20) String username,
        @NotBlank @Size(min = 6) String password
) { }
