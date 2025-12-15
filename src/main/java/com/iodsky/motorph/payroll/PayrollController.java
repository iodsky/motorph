package com.iodsky.motorph.payroll;

import com.iodsky.motorph.common.PageDto;
import com.iodsky.motorph.common.PageMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayrollMapper payrollMapper;

    @PreAuthorize("hasRole('PAYROLL')")
    @PostMapping
    public ResponseEntity<?> createPayroll(
            @RequestBody PayrollRequest request)  {

        if (request.getEmployeeId() == null) {
            Integer created = payrollService.createPayrollBatch(
                    request.getPeriodStartDate(),
                    request.getPeriodEndDate(),
                    request.getPayDate());
            return new ResponseEntity<>(Map.of("recordsCreated", created), HttpStatus.CREATED);
        }

        Payroll payroll = payrollService.createPayroll(
                request.getEmployeeId(),
                request.getPeriodStartDate(),
                request.getPeriodEndDate(),
                request.getPayDate());
        return new ResponseEntity<>(payrollMapper.toDto(payroll), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('PAYROLL')")
    @GetMapping
    public ResponseEntity<PageDto<PayrollDto>> getAllPayroll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) LocalDate periodStartDate,
            @RequestParam(required = false) LocalDate periodEndDate
    ) {

        Page<Payroll> payroll = payrollService.getAllPayroll(page, limit, periodStartDate, periodEndDate);

        return ResponseEntity.ok(PageMapper.map(payroll, payrollMapper::toDto));
    }

    @GetMapping("/me")
    public ResponseEntity<PageDto<PayrollDto>> getAllEmployeePayroll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) LocalDate periodStartDate,
            @RequestParam(required = false) LocalDate periodEndDate
    ) {
        Page<Payroll> payroll = payrollService.getAllEmployeePayroll(page, limit, periodStartDate, periodEndDate);

        return ResponseEntity.ok(PageMapper.map(payroll, payrollMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollDto> getPayrollById(@PathVariable("id") UUID id) {
        Payroll payroll = payrollService.getPayrollById(id);
        return ResponseEntity.ok(payrollMapper.toDto(payroll));
    }
}
