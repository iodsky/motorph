package com.iodsky.motorph.csvimport;

import com.iodsky.motorph.employee.EmployeeCsvRecord;
import com.iodsky.motorph.employee.EmployeeMapper;
import com.iodsky.motorph.employee.model.Employee;
import com.iodsky.motorph.security.user.User;
import com.iodsky.motorph.security.user.UserCsvRecord;
import com.iodsky.motorph.security.user.UserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvServiceConfig {

    @Bean
    public CsvService<Employee, EmployeeCsvRecord> employeeCsvService(EmployeeMapper employeeMapper) {
        return new CsvService<>(employeeMapper);
    }

    @Bean
    public CsvService<User, UserCsvRecord> userCsvService(UserMapper userMapper) {
        return new CsvService<>(userMapper);
    }
}

