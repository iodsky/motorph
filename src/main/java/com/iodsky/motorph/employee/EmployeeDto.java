package com.iodsky.motorph.employee;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeDto {

    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String address;
    private String phoneNumber;
    private String sssNumber;
    private String tinNumber;
    private String philhealthNumber;
    private String pagIbigNumber;
    private String supervisor;
    private String position;
    private String department;
    private String status;
    private BigDecimal basicSalary;
    private BigDecimal hourlyRate;
    private BigDecimal semiMonthlyRate;
    private BigDecimal riceSubsidy;
    private BigDecimal clothingAllowance;
    private BigDecimal phoneAllowance;

}
