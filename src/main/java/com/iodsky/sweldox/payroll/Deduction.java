package com.iodsky.sweldox.payroll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iodsky.sweldox.common.BaseModel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "deduction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Deduction extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "payroll_id")
    @JsonIgnore
    private Payroll payroll;

    @ManyToOne
    @JoinColumn(name = "deduction_code")
    private DeductionType deductionType;

    private BigDecimal amount;


}
