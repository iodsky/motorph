package com.iodsky.sweldox.csvimport;

import com.iodsky.sweldox.employee.EmployeeCsvRecord;
import com.iodsky.sweldox.employee.EmployeeMapper;
import com.iodsky.sweldox.employee.Employee;
import com.iodsky.sweldox.leave.LeaveCredit;
import com.iodsky.sweldox.leave.LeaveCreditCsvRecord;
import com.iodsky.sweldox.leave.LeaveCreditMapper;
import com.iodsky.sweldox.security.user.User;
import com.iodsky.sweldox.security.user.UserCsvRecord;
import com.iodsky.sweldox.security.user.UserMapper;
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

