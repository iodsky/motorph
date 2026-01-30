package com.iodsky.sweldox.payroll.contribution.sss;

import com.iodsky.sweldox.common.response.ApiResponse;
import com.iodsky.sweldox.common.response.DeleteResponse;
import com.iodsky.sweldox.common.response.PaginationMeta;
import com.iodsky.sweldox.common.response.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payroll-config/sss")
@PreAuthorize("hasRole('PAYROLL')")
@Validated
@RequiredArgsConstructor
@Tag(name = "Payroll Configuration - SSS", description = "Manage SSS contribution configurations")
public class SssContributionController {

    private final SssContributionService sssContributionService;
    private final SssContributionMapper sssContributionMapper;

    @PostMapping
    @Operation(summary = "Create SSS configuration", description = "Create a new SSS contribution configuration with salary brackets. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<SssContributionDto>> createSssContribution(
            @Valid @RequestBody SssContributionRequest request) {
        SssContribution contribution = sssContributionService.createSssContribution(request);
        return ResponseFactory.created(
                "SSS contribution configuration created successfully",
                sssContributionMapper.toDto(contribution)
        );
    }

    @GetMapping
    @Operation(summary = "Get all SSS configurations", description = "Retrieve all SSS contribution configurations with pagination and filters. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<List<SssContributionDto>>> getAllSssContributions(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int pageNo,
            @Parameter(description = "Number of items per page (1-100)") @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @Parameter(description = "Filter by effective date") @RequestParam(required = false) LocalDate effectiveDate
    ) {
        Page<SssContribution> page = sssContributionService.getAllSssContributions(
                pageNo, limit, effectiveDate);
        List<SssContributionDto> contributions = page.getContent().stream()
                .map(sssContributionMapper::toDto)
                .toList();

        return ResponseFactory.ok(
                "SSS contribution configurations retrieved successfully",
                contributions,
                PaginationMeta.of(page)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SSS configuration by ID", description = "Retrieve a specific SSS contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<SssContributionDto>> getSssContributionById(
            @Parameter(description = "Configuration ID") @PathVariable UUID id) {
        SssContribution contribution = sssContributionService.getSssContributionById(id);
        return ResponseFactory.ok(
                "SSS contribution configuration retrieved successfully",
                sssContributionMapper.toDto(contribution)
        );
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup SSS contribution by salary", description = "Find the SSS contribution configuration for a given salary and date. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<SssContributionDto>> getSssContributionBySalary(
            @Parameter(description = "Salary amount") @RequestParam BigDecimal salary,
            @Parameter(description = "Date to check (defaults to today)") @RequestParam(required = false) LocalDate date
    ) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        SssContribution contribution = sssContributionService.getSssContributionBySalaryAndDate(salary, effectiveDate);
        return ResponseFactory.ok(
                "SSS contribution configuration found for salary",
                sssContributionMapper.toDto(contribution)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update SSS configuration", description = "Update an existing SSS contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<SssContributionDto>> updateSssContribution(
            @Parameter(description = "Configuration ID") @PathVariable UUID id,
            @Valid @RequestBody SssContributionRequest request) {
        SssContribution contribution = sssContributionService.updateSssContribution(id, request);
        return ResponseFactory.ok(
                "SSS contribution configuration updated successfully",
                sssContributionMapper.toDto(contribution)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SSS configuration", description = "Soft delete an SSS contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<DeleteResponse>> deleteSssContribution(
            @Parameter(description = "Configuration ID") @PathVariable UUID id) {
        sssContributionService.deleteSssContribution(id);
        return ResponseFactory.ok(
                "SSS contribution configuration deleted successfully",
                new DeleteResponse("SssContribution", id.toString())
        );
    }
}
