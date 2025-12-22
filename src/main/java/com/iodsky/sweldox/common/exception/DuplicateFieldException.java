package com.iodsky.sweldox.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateFieldException extends ApiException {
    public DuplicateFieldException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
