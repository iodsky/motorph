package com.iodsky.sweldox.payroll.contribution.pagIbig;

import org.springframework.stereotype.Component;

@Component
public class PagibigContributionMapper {

    public PagibigContributionDto toDto(PagibigContribution entity) {
        if (entity == null) {
            return null;
        }

        return PagibigContributionDto.builder()
                .id(entity.getId())
                .employeeRate(entity.getEmployeeRate())
                .employerRate(entity.getEmployerRate())
                .lowIncomeThreshold(entity.getLowIncomeThreshold())
                .lowIncomeEmployeeRate(entity.getLowIncomeEmployeeRate())
                .maxSalaryCap(entity.getMaxSalaryCap())
                .effectiveDate(entity.getEffectiveDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
