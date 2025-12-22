package com.iodsky.sweldox.csvimport;

import com.opencsv.bean.AbstractBeanField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateCsvConverter extends AbstractBeanField<LocalDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected LocalDate convert(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value.trim(), FORMATTER);
    }

}
