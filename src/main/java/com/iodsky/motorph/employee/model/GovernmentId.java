package com.iodsky.motorph.employee.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "government_id")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GovernmentId {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "sss_no")
    private String sssNumber;

    @Column(name = "tin_no")
    private String tinNumber;

    @Column(name = "philhealth_no")
    private String philhealthNumber;

    @Column(name = "pagibig_no")
    private String pagIbigNumber;
}
