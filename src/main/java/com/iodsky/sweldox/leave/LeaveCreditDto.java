package com.iodsky.sweldox.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LeaveCreditDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "EmployeeId is required")
    private Long employeeId;

    @NotNull(message = "Leave type is required")
    private String type;

    @Min(3)
    @Max(21)
    private double credits;

    private String fiscalYear;
}
