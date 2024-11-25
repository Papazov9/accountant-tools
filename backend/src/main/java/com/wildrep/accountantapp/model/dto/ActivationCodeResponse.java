package com.wildrep.accountantapp.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivationCodeResponse {
    private String message;
    private String username;
    private boolean isSuccess;
}
