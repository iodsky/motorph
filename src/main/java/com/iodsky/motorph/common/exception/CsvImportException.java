package com.iodsky.motorph.common.exception;

import org.springframework.http.HttpStatus;

public class CsvImportException extends ApiException {
    public CsvImportException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

