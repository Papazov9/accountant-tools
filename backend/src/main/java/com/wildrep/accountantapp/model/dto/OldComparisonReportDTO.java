package com.wildrep.accountantapp.model.dto;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

public record OldComparisonReportDTO(ByteArrayOutputStream reportStream,
                                     LocalDateTime comparisonTimestamp) {
}
