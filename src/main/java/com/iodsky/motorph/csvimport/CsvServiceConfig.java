package com.iodsky.motorph.csvimport;

import com.iodsky.motorph.employee.EmployeeCsvRecord;
import com.iodsky.motorph.employee.EmployeeMapper;
import com.iodsky.motorph.employee.Employee;
import com.iodsky.motorph.leave.LeaveCredit;
import com.iodsky.motorph.leave.LeaveCreditCsvRecord;
import com.iodsky.motorph.leave.LeaveCreditMapper;
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

    @Bean
    public CsvService<LeaveCredit, LeaveCreditCsvRecord> leaveCreditCsvService(LeaveCreditMapper leaveCreditMapper) {
        return new CsvService<>(leaveCreditMapper);
    }
}

