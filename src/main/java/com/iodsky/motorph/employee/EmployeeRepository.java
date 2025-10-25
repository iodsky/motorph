package com.iodsky.motorph.employee;

import com.iodsky.motorph.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByEmploymentDetails_Status(Status status);

    List<Employee> findByEmploymentDetails_Department_Id(String departmentId);

    List<Employee> findByEmploymentDetails_Supervisor_Id(Long supervisorId);

}
