package com.iodsky.motorph.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iodsky.motorph.common.BaseModel;
import com.iodsky.motorph.organization.Department;
import com.iodsky.motorph.organization.Position;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "employment_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmploymentDetails extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = true)
    private Employee supervisor;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private Status status;
}
