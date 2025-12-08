package com.iodsky.motorph.leave;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iodsky.motorph.employee.model.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "leave_balance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    @Enumerated(value = EnumType.STRING)
    private LeaveType type;

    private double credits;

}
