package com.iodsky.sweldox.payroll.contribution.sss;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SssContributionMapper {

    public SssContributionDto toDto(SssContribution entity) {
        if (entity == null) {
            return null;
        }

        return SssContributionDto.builder()
                .id(entity.getId())
                .totalSss(entity.getTotalSss())
                .employeeSss(entity.getEmployeeRate())
                .employerSss(entity.getEmployerRate())
                .salaryBrackets(entity.getSalaryBrackets().stream()
                        .map(this::toBracketDto)
                        .collect(Collectors.toList()))
                .effectiveDate(entity.getEffectiveDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SssContributionDto.SalaryBracketDto toBracketDto(SssContribution.SalaryBracket bracket) {
        return SssContributionDto.SalaryBracketDto.builder()
                .minSalary(bracket.getMinSalary())
                .maxSalary(bracket.getMaxSalary())
                .msc(bracket.getMsc())
                .build();
    }
}
