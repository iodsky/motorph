package com.iodsky.sweldox.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    @NonNull
    Page<Employee> findAll(@NonNull Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.status = :status")
    Page<Employee> findByEmploymentDetails_Status(Status status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.department.id = :departmentId")
    Page<Employee> findByEmploymentDetails_Department_Id(String departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.supervisor.id = :supervisorId")
    Page<Employee> findByEmploymentDetails_Supervisor_Id(Long supervisorId, Pageable pageable);

    @Query("""
        SELECT e.id
        FROM Employee e
        WHERE e.status NOT IN (
        com.iodsky.sweldox.employee.Status.RESIGNED,
         com.iodsky.sweldox.employee.Status.TERMINATED
         )
       """)
    List<Long> findAllActiveEmployeeIds();

    @Query("SELECT e FROM Employee  e WHERE e.deletedAt IS NULL AND e.supervisor.id = :supervisorId")
    List<Employee> findAllBySupervisor_Id(Long supervisorId);

}
