package com.wildrep.accountantapp.model.dto;

public record DashboardUserResponse(String email,
                                    String username,
                                    String firstName,
                                    String lastName,
                                    String gender,
                                    boolean isAcknowledge,
                                    String message) {
}
