package com.iodsky.motorph.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveCreditRepository extends JpaRepository<LeaveCredit, UUID> {

    Optional<LeaveCredit> findByEmployee_IdAndType(Long employeeId, LeaveType type);
}
