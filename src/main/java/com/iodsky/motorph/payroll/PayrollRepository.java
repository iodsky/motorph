package com.iodsky.motorph.payroll;

import com.iodsky.motorph.payroll.model.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    Page<Payroll> findAllByPeriodStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Payroll> findAllByEmployee_IdAndPeriodStartDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    boolean existsByEmployee_IdAndPeriodStartDateAndPeriodEndDate(Long employeeId, LocalDate startDate, LocalDate endDate);
}
