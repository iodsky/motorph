package com.iodsky.motorph.leave;

import com.iodsky.motorph.common.exception.BadRequestException;
import com.iodsky.motorph.common.exception.CsvImportException;
import com.iodsky.motorph.common.exception.NotFoundException;
import com.iodsky.motorph.common.exception.UnauthorizedException;
import com.iodsky.motorph.csvimport.CsvResult;
import com.iodsky.motorph.csvimport.CsvService;
import com.iodsky.motorph.employee.EmployeeService;
import com.iodsky.motorph.employee.model.Employee;
import com.iodsky.motorph.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveCreditService {

    private final LeaveCreditRepository leaveCreditRepository;
    private final EmployeeService employeeService;
    private final CsvService<LeaveCredit, LeaveCreditCsvRecord> leaveCreditService;

    public LeaveCredit getLeaveCreditByEmployeeIdAndType(Long employeeId, LeaveType type) {
        return leaveCreditRepository.findByEmployee_IdAndType(employeeId, type)
                .orElseThrow(() -> new NotFoundException("No " + type + " leave credits found for employeeId: " + employeeId));
    }

    public List<LeaveCredit> getLeaveCreditsByEmployeeId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            throw new UnauthorizedException("Authentication error");
        }

        Long employeeId = user.getEmployee().getId();
        return leaveCreditRepository.findAllByEmployee_Id(employeeId);
    }

    public LeaveCredit updateLeaveCredit (UUID targetId, LeaveCredit updated) {
        LeaveCredit existing = leaveCreditRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Leave credit not found: " + targetId));

        existing.setCredits(updated.getCredits());

        return leaveCreditRepository.save(existing);
    }

    public Integer importLeaveCredits(MultipartFile file) {

        try {
            LinkedHashSet<CsvResult<LeaveCredit, LeaveCreditCsvRecord>> csvResults =
                    leaveCreditService.parseCsv(file.getInputStream(), LeaveCreditCsvRecord.class);

            LinkedHashSet<LeaveCredit> leaveCredits = csvResults.stream().map(r -> {
                LeaveCredit entity = r.entity();
                LeaveCreditCsvRecord csv = r.source();

                // Resolve Employee
                Employee employee = employeeService.getEmployeeById(csv.getEmployeeId());
                entity.setEmployee(employee);

                // Resolve leave type
                LeaveType type;
                try {
                    type = LeaveType.valueOf(csv.getType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid leave type: " + csv.getType());
                }

                entity.setType(type);

                return entity;
            }).collect(Collectors.toCollection(LinkedHashSet::new));

            leaveCreditRepository.saveAll(leaveCredits);

            return leaveCredits.size();
        } catch (IOException e) {
            throw new CsvImportException(e.getMessage());
        }
    }
}
