package com.iodsky.sweldox.common.exception;


import com.iodsky.sweldox.common.DuplicateField;
import lombok.Getter;

@Getter
public class DuplicateFieldException extends RuntimeException {

    private DuplicateField duplicateField;

    public DuplicateFieldException(String message, DuplicateField error) {
        super(message);
        this.duplicateField = error;
    }

}
