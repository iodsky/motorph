package com.iodsky.sweldox.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iodsky.sweldox.common.BaseModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "position")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Position extends BaseModel {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    @Column(nullable = false, unique = true)
    private String title;
}
