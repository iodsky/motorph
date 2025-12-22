package com.iodsky.sweldox.leave;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveCreditCsvRecord {

    @CsvBindByName(column = "EMPLOYEE_ID")
    private Long employeeId;

    @CsvBindByName(column = "TYPE")
    private String type;

    @CsvBindByName(column = "CREDITS")
    private double credits;

}
