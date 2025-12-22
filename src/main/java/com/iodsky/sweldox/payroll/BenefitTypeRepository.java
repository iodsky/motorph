package com.iodsky.sweldox.payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenefitTypeRepository extends JpaRepository<BenefitType, String> {
}
