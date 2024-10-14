package com.wildrep.accountantapp.model.dto;

import lombok.Builder;

@Builder
public record LoginResponse(String message, String username, String token) {
}
