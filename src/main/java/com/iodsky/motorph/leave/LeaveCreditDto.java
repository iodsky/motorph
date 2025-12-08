package com.iodsky.motorph.leave;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveCreditDto {
    private String type;
    private double credits;
}
