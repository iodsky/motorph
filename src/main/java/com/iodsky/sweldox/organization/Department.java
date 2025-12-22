package com.iodsky.sweldox.organization;

import com.iodsky.sweldox.common.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "department")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Department extends BaseModel {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, unique = true)
    private String title;
}
