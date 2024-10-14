package com.wildrep.accountantapp.model.dto;

import java.time.LocalDate;

public record DashboardUserResponse(String email,
                                    String username,
                                    String firstName,
                                    String lastName,
                                    LocalDate birthDate,
                                    String message) {
}
