package com.iodsky.motorph.employee.request;

import com.iodsky.motorph.employee.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmploymentDetailsRequest {

    private Long supervisorId;

    @NotNull(message = "Position is required")
    private String positionId;

    @NotNull(message = "Department is required")
    private String departmentId;

    @NotNull(message = "Status is required")
    private Status status;
}
