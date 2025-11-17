package com.iodsky.motorph.payroll.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_benefits")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayrollBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "payroll_id")
    @JsonIgnore
    private Payroll payroll;

    @ManyToOne
    @JoinColumn(name = "benefit_type_id")
    private BenefitType benefitType;

    private BigDecimal amount;

}
