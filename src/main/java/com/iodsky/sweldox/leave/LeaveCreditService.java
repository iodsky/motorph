package com.iodsky.sweldox.leave;

import com.iodsky.sweldox.employee.EmployeeService;
import com.iodsky.sweldox.employee.Employee;
import com.iodsky.sweldox.security.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveCreditService {

    private final LeaveCreditRepository leaveCreditRepository;
    private final EmployeeService employeeService;

    private static final double DEFAULT_VACATION_CREDITS = 14.0;
    private static final double DEFAULT_SICK_CREDITS = 7.0;
    private static final double DEFAULT_BEREAVEMENT_CREDITS = 5.0;

    @Transactional
    public List<LeaveCredit> initializeEmployeeLeaveCredits(InitializeEmployeeLeaveCreditsDto dto) {
        Employee employee = employeeService.getEmployeeById(dto.getEmployeeId());

        String fiscalYear = dto.getFiscalYear();
        if (fiscalYear == null || fiscalYear.isBlank()) {
            int currentYear = LocalDate.now().getYear();
            fiscalYear = currentYear + "-" + (currentYear + 1);
        }

        final String finalFiscalYear = fiscalYear;
        boolean exists = leaveCreditRepository.existsByEmployee_IdAndFiscalYear(employee.getId(), finalFiscalYear);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave credits already exists for employee " + employee.getId());
        }

        List<LeaveCredit> leaveCredits = List.of(
                LeaveCredit.builder()
                        .employee(employee)
                        .type(LeaveType.VACATION)
                        .fiscalYear(finalFiscalYear)
                        .credits(DEFAULT_VACATION_CREDITS)
                        .build(),
                LeaveCredit.builder()
                        .employee(employee)
                        .type(LeaveType.SICK)
                        .fiscalYear(finalFiscalYear)
                        .credits(DEFAULT_SICK_CREDITS)
                        .build(),
                LeaveCredit.builder()
                        .employee(employee)
                        .type(LeaveType.BEREAVEMENT)
                        .fiscalYear(finalFiscalYear)
                        .credits(DEFAULT_BEREAVEMENT_CREDITS)
                        .build()
        );

        return leaveCreditRepository.saveAll(leaveCredits);
    }

    public LeaveCredit getLeaveCreditByEmployeeIdAndType(Long employeeId, LeaveType type) {
        return leaveCreditRepository.findByEmployee_IdAndType(employeeId, type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No " + type + " leave credits found for employeeId: " + employeeId));
    }

    public List<LeaveCredit> getLeaveCreditsByEmployeeId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to access this resource");
        }

        Long employeeId = user.getEmployee().getId();
        return leaveCreditRepository.findAllByEmployee_Id(employeeId);
    }

    public LeaveCredit updateLeaveCredit (UUID targetId, LeaveCredit updated) {
        LeaveCredit existing = leaveCreditRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave credit not found: " + targetId));

        existing.setCredits(updated.getCredits());

        return leaveCreditRepository.save(existing);
    }

    public void deleteLeaveCreditsByEmployeeId(Long employeeId) {
        List<LeaveCredit> credits = leaveCreditRepository.findAllByEmployee_Id(employeeId);
        leaveCreditRepository.deleteAll(credits);
    }

}
