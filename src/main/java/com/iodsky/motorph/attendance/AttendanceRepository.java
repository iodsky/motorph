package com.iodsky.motorph.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Optional<Attendance> findByEmployee_IdAndDate(Long employeeId, LocalDate date);

    Page<Attendance> findAllByDate(LocalDate date, Pageable pageable);

    Page<Attendance> findAllByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Attendance> findByEmployee_IdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Attendance> findByEmployee_IdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

}