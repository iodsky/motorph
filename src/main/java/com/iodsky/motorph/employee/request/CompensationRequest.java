package com.iodsky.motorph.employee.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompensationRequest {

    @NotNull
    @Positive
    private BigDecimal basicSalary;

    @NotNull
    @Positive
    private BigDecimal hourlyRate;

    @NotNull
    @Positive
    private BigDecimal semiMonthlyRate;

    @NotNull
    @Positive
    private BigDecimal riceSubsidy;

    @NotNull
    @Positive
    private BigDecimal clothingAllowance;

    @NotNull
    @Positive
    private BigDecimal phoneAllowance;

}
