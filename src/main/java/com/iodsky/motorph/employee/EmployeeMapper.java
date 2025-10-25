package com.iodsky.motorph.employee;

import com.iodsky.motorph.employee.model.Compensation;
import com.iodsky.motorph.employee.model.Employee;
import com.iodsky.motorph.employee.model.EmploymentDetails;
import com.iodsky.motorph.employee.model.GovernmentId;
import com.iodsky.motorph.employee.request.EmployeeRequest;
import com.iodsky.motorph.organization.Department;
import com.iodsky.motorph.organization.Position;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeDto toDto(Employee employee) {

        Employee supervisor = employee.getEmploymentDetails().getSupervisor();
        String supervisorName = supervisor != null ? supervisor.getFirstName() + " " + supervisor.getLastName() : "N/A";

        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .birthday(employee.getBirthday())
                .address(employee.getAddress())
                .phoneNumber(employee.getPhoneNumber())
                .sssNumber(employee.getGovernmentId().getSssNumber())
                .tinNumber(employee.getGovernmentId().getTinNumber())
                .philhealthNumber(employee.getGovernmentId().getPhilhealthNumber())
                .pagIbigNumber(employee.getGovernmentId().getPagIbigNumber())
                .status(employee.getEmploymentDetails().getStatus().toString())
                .supervisor(supervisorName)
                .department(employee.getEmploymentDetails().getDepartment().getTitle())
                .position(employee.getEmploymentDetails().getPosition().getTitle())
                .basicSalary(employee.getCompensation().getBasicSalary())
                .hourlyRate(employee.getCompensation().getHourlyRate())
                .semiMonthlyRate(employee.getCompensation().getSemiMonthlyRate())
                .riceSubsidy(employee.getCompensation().getRiceSubsidy())
                .clothingAllowance(employee.getCompensation().getClothingAllowance())
                .phoneAllowance(employee.getCompensation().getPhoneAllowance())
                .build();
    }

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthday(request.getBirthday())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .build();

        GovernmentId governmentId = GovernmentId.builder()
                .employee(employee)
                .sssNumber(request.getGovernmentId().getSssNumber())
                .tinNumber(request.getGovernmentId().getTinNumber())
                .philhealthNumber(request.getGovernmentId().getPhilhealthNumber())
                .pagIbigNumber(request.getGovernmentId().getPagIbigNumber())
                .build();

        EmploymentDetails employmentDetails = EmploymentDetails.builder()
                .employee(employee)
                .status(request.getEmploymentDetails().getStatus())
                .build();

        Compensation compensation = Compensation.builder()
                .employee(employee)
                .basicSalary(request.getCompensation().getBasicSalary())
                .hourlyRate(request.getCompensation().getHourlyRate())
                .semiMonthlyRate(request.getCompensation().getSemiMonthlyRate())
                .riceSubsidy(request.getCompensation().getRiceSubsidy())
                .clothingAllowance(request.getCompensation().getClothingAllowance())
                .phoneAllowance(request.getCompensation().getPhoneAllowance())
                .build();

        employee.setGovernmentId(governmentId);
        employee.setEmploymentDetails(employmentDetails);
        employee.setCompensation(compensation);

        return employee;
    }

    public void updateEntity(Employee existing, EmployeeRequest request) {

        // --- BASIC INFO ---
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setBirthday(request.getBirthday());
        existing.setAddress(request.getAddress());
        existing.setPhoneNumber(request.getPhoneNumber());

        // --- GOVERNMENT IDs ---
        if (existing.getGovernmentId() == null) {
            existing.setGovernmentId(new GovernmentId());
            existing.getGovernmentId().setEmployee(existing);
        }
        GovernmentId gov = existing.getGovernmentId();
        gov.setSssNumber(request.getGovernmentId().getSssNumber());
        gov.setTinNumber(request.getGovernmentId().getTinNumber());
        gov.setPhilhealthNumber(request.getGovernmentId().getPhilhealthNumber());
        gov.setPagIbigNumber(request.getGovernmentId().getPagIbigNumber());

        // --- EMPLOYMENT DETAILS ---
        if (existing.getEmploymentDetails() == null) {
            existing.setEmploymentDetails(new EmploymentDetails());
            existing.getEmploymentDetails().setEmployee(existing);
        }
        EmploymentDetails details = existing.getEmploymentDetails();
        details.setStatus(request.getEmploymentDetails().getStatus());

        // --- COMPENSATION ---
        if (existing.getCompensation() == null) {
            existing.setCompensation(new Compensation());
            existing.getCompensation().setEmployee(existing);
        }
        Compensation comp = existing.getCompensation();
        comp.setBasicSalary(request.getCompensation().getBasicSalary());
        comp.setHourlyRate(request.getCompensation().getHourlyRate());
        comp.setSemiMonthlyRate(request.getCompensation().getSemiMonthlyRate());
        comp.setRiceSubsidy(request.getCompensation().getRiceSubsidy());
        comp.setClothingAllowance(request.getCompensation().getClothingAllowance());
        comp.setPhoneAllowance(request.getCompensation().getPhoneAllowance());
    }
}
