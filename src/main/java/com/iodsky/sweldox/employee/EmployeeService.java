package com.iodsky.sweldox.employee;

import com.iodsky.sweldox.common.DuplicateField;
import com.iodsky.sweldox.common.exception.CsvImportException;
import com.iodsky.sweldox.common.exception.DuplicateFieldException;
import com.iodsky.sweldox.csvimport.CsvResult;
import com.iodsky.sweldox.csvimport.CsvService;
import com.iodsky.sweldox.organization.Department;
import com.iodsky.sweldox.organization.DepartmentService;
import com.iodsky.sweldox.organization.Position;
import com.iodsky.sweldox.organization.PositionService;
import com.iodsky.sweldox.payroll.BenefitService;
import com.iodsky.sweldox.payroll.Benefit;
import com.iodsky.sweldox.payroll.BenefitType;
import com.iodsky.sweldox.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final BenefitService benefitService;
    private final CsvService<Employee, EmployeeCsvRecord> employeeCsvService;

    public Employee createEmployee(EmployeeRequest request) {
        try {
            Employee employee = employeeMapper.toEntity(request);

            Employee supervisor = null;
            if (request.getSupervisorId() != null) {
                supervisor = getEmployeeById(request.getSupervisorId());
            }

            Department department = departmentService.getDepartmentById(request.getDepartmentId());
            Position position = positionService.getPositionById(request.getPositionId());

            employee.setSupervisor(supervisor);
            employee.setDepartment(department);
            employee.setPosition(position);

            List<Benefit> benefits = employee.getBenefits();
            benefits.forEach(b -> {
                b.setBenefitType(benefitService.getBenefitTypeById(b.getBenefitType().getId()));
            });

            return employeeRepository.save(employee);
        } catch (DataIntegrityViolationException ex) {
            throw handleDataIntegrityViolation(ex);
        }
    }

    public Page<Employee> getAllEmployees(int page, int limit, String departmentId, Long supervisorId, String status) {

        Pageable pageable = PageRequest.of(page, limit);

        if (departmentId != null) {
            return employeeRepository.findByEmploymentDetails_Department_Id(departmentId, pageable);
        } else if (supervisorId != null) {
            return employeeRepository.findByEmploymentDetails_Supervisor_Id(supervisorId, pageable);
        } else if (status != null) {
            return  employeeRepository.findByEmploymentDetails_Status(Status.valueOf(status.toUpperCase()), pageable);
        }

        return employeeRepository.findAll(pageable);
    }

    public Employee getAuthenticatedEmployee() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user.getEmployee();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee " + id + " not found"));
    }

    public Employee updateEmployeeById(Long id, EmployeeRequest request) {
        Employee employee = this.getEmployeeById(id);

        try {
            Employee supervisor = null;
            if (request.getSupervisorId() != null) {
                supervisor = getEmployeeById(request.getSupervisorId());
            }

            Department department = departmentService.getDepartmentById(request.getDepartmentId());
            Position position = positionService.getPositionById(request.getPositionId());

            employee.setSupervisor(supervisor);
            employee.setDepartment(department);
            employee.setPosition(position);

            employeeMapper.updateEntity(employee, request);

            return employeeRepository.save(employee);

        } catch (DataIntegrityViolationException ex) {
            throw handleDataIntegrityViolation(ex);
        }
    }

    @Transactional
    public void deleteEmployeeById(Long id, String status) {
        Employee employee = getEmployeeById(id);
        if (employee.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee already deleted");
        }

        Status status_;
        try {
            status_ = Status.valueOf(status.toUpperCase());
            if (status_ != Status.TERMINATED && status_ != Status.RESIGNED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be either TERMINATED or RESIGNED");
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be either TERMINATED or RESIGNED");
        }

        List<Employee> subordinates = employeeRepository.findAllBySupervisor_Id(employee.getId());
        if (!subordinates.isEmpty()) {
            subordinates.forEach(subordinate -> subordinate.setSupervisor(null));
            employeeRepository.saveAll(subordinates);
        }

        employee.setDeletedAt(Instant.now());
        employee.setStatus(status_);

        employeeRepository.save(employee);
    }

    public List<Long> getAllActiveEmployeeIds() {
        return employeeRepository.findAllActiveEmployeeIds();
    }

    @Transactional
    public Integer importEmployees(MultipartFile file) {
        try {
            LinkedHashSet<CsvResult<Employee, EmployeeCsvRecord>> records =
                    employeeCsvService.parseCsv(file.getInputStream(), EmployeeCsvRecord.class);

            BenefitType mealBenefitType = benefitService.getBenefitTypeById("MEAL");
            BenefitType clothingBenefitType = benefitService.getBenefitTypeById("CLOTHING");
            BenefitType phoneBenefitType = benefitService.getBenefitTypeById("PHONE");

            Set<String> positionTitles = records.stream()
                    .map(r -> r.source().getPosition())
                    .collect(Collectors.toSet());
            Map<String, Position> positionMap = positionService.getPositionsByTitles(positionTitles);

            Map<Employee, Long> employeeSupervisorMap = new HashMap<>();

            LinkedHashSet<Employee> employees = records.stream().map(r -> {
                Employee employee = r.entity();
                EmployeeCsvRecord csv = r.source();

                Position position = positionMap.get(csv.getPosition());
                employee.setPosition(position);
                employee.setDepartment(position.getDepartment());

                Benefit mealAllowance = Benefit.builder()
                        .benefitType(mealBenefitType)
                        .amount(csv.getMealAllowance())
                        .build();

                Benefit clothingAllowance = Benefit.builder()
                        .benefitType(clothingBenefitType)
                        .amount(csv.getClothingAllowance())
                        .build();

                Benefit phoneAllowance = Benefit.builder()
                        .benefitType(phoneBenefitType)
                        .amount(csv.getPhoneAllowance())
                        .build();

                employee.setBenefits(
                        new ArrayList<>(List.of(mealAllowance, phoneAllowance, clothingAllowance))
                );
                employee.getBenefits()
                        .forEach(b -> b.setEmployee(employee));

                employeeSupervisorMap.put(employee, csv.getSupervisorId());

                return employee;
            }).collect(Collectors.toCollection(LinkedHashSet::new));

            List<Employee> savedEmployees = employeeRepository.saveAll(employees);

            Map<Long, Employee> employeeIdMap = savedEmployees.stream()
                    .collect(Collectors.toMap(Employee::getId, e -> e));

            // Set supervisor relationships
            for (Employee employee : savedEmployees) {
                Long supervisorId = employeeSupervisorMap.get(employee);
                if (supervisorId != null) {
                    Employee supervisor = employeeIdMap.get(supervisorId);

                    if (supervisor == null) {
                        try {
                            supervisor = getEmployeeById(supervisorId);
                        } catch (ResponseStatusException e) {
                            continue;
                        }
                    }

                    employee.setSupervisor(supervisor);
                }
            }

            employeeRepository.saveAll(savedEmployees);

            return savedEmployees.size();
        } catch (IOException e) {
            throw new CsvImportException(e.getMessage());
        }
    }

    private DuplicateFieldException handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Field validation error";
        DuplicateField duplicateField = null;

        Throwable rootCause = ex.getMostSpecificCause();
        String errorMessage = rootCause.getMessage();

        if (errorMessage != null) {
            Pattern pattern = java.util.regex.Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(errorMessage);

            if (matcher.find()) {
                String field = matcher.group(1);
                String value = matcher.group(2);

                duplicateField = DuplicateField.builder()
                        .field(field)
                        .value(value)
                        .build();

                message = String.format("Duplicate value '%s' for field '%s'", value, field);
            }
        }

        return new DuplicateFieldException(message, duplicateField);
    }
}
