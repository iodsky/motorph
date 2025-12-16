package com.iodsky.motorph.organization;

import com.iodsky.motorph.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public Department getDepartmentById(String id) {
        return departmentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department " + id + " not found"));
    }

}
