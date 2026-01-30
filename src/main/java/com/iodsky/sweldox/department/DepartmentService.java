package com.iodsky.sweldox.department;

import com.iodsky.sweldox.position.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsById(request.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department with ID " + request.getId() + " already exists");
        }

        Department department = Department.builder()
                .id(request.getId())
                .title(request.getTitle())
                .build();

        return departmentRepository.save(department);
    }

    public Page<Department> getAllDepartments(int pageNo, int limit) {
        Pageable pageable = PageRequest.of(pageNo, limit, Sort.by("title").ascending());
        return departmentRepository.findAll(pageable);
    }

    public Department getDepartmentById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department " + id + " not found"));
    }

    public Department updateDepartment(String id, DepartmentUpdateRequest request) {
        Department department = getDepartmentById(id);
        department.setTitle(request.getTitle());
        return departmentRepository.save(department);
    }

    public void deleteDepartment(String id) {
        Department department = getDepartmentById(id);

        // Check if department has active employees
        long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        if (employeeCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete department '" + department.getTitle() + "'. It has " + employeeCount + " active employee(s) assigned to it."
            );
        }

        // Check if department has active positions
        long positionCount = positionRepository.countPositionsByDepartmentId(id);
        if (positionCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete department '" + department.getTitle() + "'. It has " + positionCount + " active position(s) linked to it."
            );
        }

        // If already soft-deleted, perform hard-delete
        if (department.getDeletedAt() != null) {
            departmentRepository.delete(department);
            return;
        }

        // Soft delete
        department.setDeletedAt(Instant.now());
        departmentRepository.save(department);
    }

}
