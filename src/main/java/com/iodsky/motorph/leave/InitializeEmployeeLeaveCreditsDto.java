package com.iodsky.motorph.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InitializeEmployeeLeaveCreditsDto {
    @NotNull(message = "EmployeeId is required")
    private Long employeeId;

    @Pattern(regexp = "\\d{4}-\\d{4}", message = "Fiscal year must be in format YYYY-YYYY")
    private String fiscalYear;
}
