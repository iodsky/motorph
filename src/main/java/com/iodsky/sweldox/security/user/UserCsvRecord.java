package com.iodsky.sweldox.security.user;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCsvRecord {

    @CsvBindByName(column = "EMPLOYEE ID")
    private Long employeeId;

    @CsvBindByName(column = "ROLE")
    private String role;

    @CsvBindByName(column = "EMAIL")
    private String email;

    @CsvBindByName(column = "PASSWORD")
    private String password;
}

