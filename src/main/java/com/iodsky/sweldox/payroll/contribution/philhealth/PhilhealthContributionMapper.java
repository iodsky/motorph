package com.iodsky.sweldox.payroll.contribution.philhealth;

import org.springframework.stereotype.Component;

@Component
public class PhilhealthContributionMapper {

    public PhilhealthContributionDto toDto(PhilhealthContribution entity) {
        if (entity == null) {
            return null;
        }

        return PhilhealthContributionDto.builder()
                .id(entity.getId())
                .premiumRate(entity.getPremiumRate())
                .maxSalaryCap(entity.getMaxSalaryCap())
                .minSalaryFloor(entity.getMinSalaryFloor())
                .fixedContribution(entity.getFixedContribution())
                .effectiveDate(entity.getEffectiveDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
