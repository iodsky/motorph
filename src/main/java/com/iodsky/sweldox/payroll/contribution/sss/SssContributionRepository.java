package com.iodsky.sweldox.payroll.contribution.sss;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SssContributionRepository extends JpaRepository<SssContribution, UUID>, JpaSpecificationExecutor<SssContribution> {

    @Query("SELECT s FROM SssContribution s WHERE s.effectiveDate = (SELECT MAX(s2.effectiveDate) FROM SssContribution s2 WHERE s2.effectiveDate <= :date AND s2.deletedAt IS NULL) AND s.deletedAt IS NULL")
    Optional<SssContribution> findLatestByEffectiveDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM SssContribution s WHERE s.effectiveDate <= :date AND s.deletedAt IS NULL ORDER BY s.effectiveDate DESC")
    java.util.List<SssContribution> findAllByEffectiveDateBefore(@Param("date") LocalDate date);

}
