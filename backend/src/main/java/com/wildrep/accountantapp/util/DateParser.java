package com.wildrep.accountantapp.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Slf4j
public class DateParser {
    private static final List<DateTimeFormatter> dateFormatters = new ArrayList<>();

    static {
        dateFormatters.add(DateTimeFormatter.ofPattern("d.M.yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("MM.dd.yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("d-M-yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        dateFormatters.add(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        dateFormatters.add(DateTimeFormatter.ofPattern("d/MM/yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("d MMM yyyy"));
        dateFormatters.add(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }

    public static LocalDate parseDate(String dateString) {
        for (DateTimeFormatter formatter : dateFormatters) {
            try {

                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                log.info("Failed to parse date: {} with format: {}", dateString, formatter.toString());
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateString);
    }
}
