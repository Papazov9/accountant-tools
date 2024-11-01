package com.wildrep.accountantapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryRecordResponse {

    private String batchId;
    private String status;
    private LocalDateTime date;
    private String message;
}
