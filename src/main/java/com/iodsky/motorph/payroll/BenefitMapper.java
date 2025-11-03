package com.iodsky.motorph.payroll;

import com.iodsky.motorph.payroll.model.Benefit;
import org.springframework.stereotype.Component;

@Component
public class BenefitMapper {

    public BenefitDto toDto(Benefit benefit) {
        if (benefit == null) {
            return null;
        }

        return BenefitDto.builder()
                .benefit(benefit.getBenefitType().getType())
                .amount(benefit.getAmount())
                .build();
    }

}
