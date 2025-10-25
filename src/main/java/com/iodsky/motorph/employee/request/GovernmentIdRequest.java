package com.iodsky.motorph.employee.request;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class GovernmentIdRequest {

    @NotEmpty
    private String sssNumber;

    @NotEmpty
    private String tinNumber;

    @NotEmpty
    private String philhealthNumber;

    @NotEmpty
    private String pagIbigNumber;
}
