package com.iodsky.motorph.employee.request;

import com.iodsky.motorph.payroll.BenefitDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompensationRequest {

    @NotNull
    @Positive
    private BigDecimal basicSalary;

    private List<BenefitDto> benefits;

}
