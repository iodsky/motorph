package com.iodsky.sweldox.position;

import com.iodsky.sweldox.department.Department;
import com.iodsky.sweldox.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PositionMapper {

    private final DepartmentService departmentService;

    public PositionDto toDto(Position position) {
        return PositionDto.builder()
                .id(position.getId())
                .departmentId(position.getDepartment() != null ? position.getDepartment().getId() : null)
                .departmentTitle(position.getDepartment() != null ? position.getDepartment().getTitle() : null)
                .title(position.getTitle())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    public Position toEntity(PositionRequest request) {
        Department department = departmentService.getDepartmentById(request.getDepartmentId());

        Position position = new Position();
        position.setId(request.getId());
        position.setDepartment(department);
        position.setTitle(request.getTitle());

        return position;
    }
}
