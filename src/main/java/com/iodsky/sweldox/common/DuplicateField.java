package com.iodsky.sweldox.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DuplicateField {
    private String field;
    private String value;
}