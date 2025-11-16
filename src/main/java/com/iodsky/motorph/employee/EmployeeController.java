package com.iodsky.motorph.employee;

import com.iodsky.motorph.common.PageDto;
import com.iodsky.motorph.common.PageMapper;
import com.iodsky.motorph.employee.model.Employee;
import com.iodsky.motorph.employee.request.EmployeeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
@Validated
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @PreAuthorize("hasRole('HR')")
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        Employee employee = employeeService.createEmployee(request);
        EmployeeDto dto = employeeMapper.toDto(employee);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('HR', 'IT', 'PAYROLL')")
    @GetMapping
    public ResponseEntity<PageDto<EmployeeDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) @Positive Long supervisorId,
            @RequestParam(required = false) String status
    ) {
        Page<Employee> employees = employeeService.getAllEmployees(page, limit, departmentId, supervisorId, status);

        return ResponseEntity.ok(PageMapper.map(employees, employeeMapper::toDto));
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeDto> getAuthenticatedEmployee() {
        Employee employee = employeeService.getAuthenticatedEmployee();
        EmployeeDto dto = employeeMapper.toDto(employee);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable long id) {
        Employee employee = employeeService.getEmployeeById(id);
        EmployeeDto dto = employeeMapper.toDto(employee);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('HR')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable long id, @Valid @RequestBody EmployeeRequest request) {
        Employee employee = employeeService.updateEmployeeById(id, request);
        EmployeeDto dto = employeeMapper.toDto(employee);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable long id) {
        employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(Map.of("message", "Employee deleted successfully"));
    }
}
