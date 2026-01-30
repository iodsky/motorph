package com.iodsky.sweldox.payroll.contribution.pagIbig;

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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payroll-config/pagibig")
@PreAuthorize("hasRole('PAYROLL')")
@Validated
@RequiredArgsConstructor
@Tag(name = "Payroll Configuration - Pag-IBIG", description = "Manage Pag-IBIG contribution configurations")
public class PagibigContributionController {

    private final PagibigContributionService pagibigContributionService;
    private final PagibigContributionMapper pagibigContributionMapper;

    @PostMapping
    @Operation(summary = "Create Pag-IBIG configuration", description = "Create a new Pag-IBIG contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<PagibigContributionDto>> createPagibigContribution(
            @Valid @RequestBody PagibigContributionRequest request) {
        PagibigContribution contribution = pagibigContributionService.createPagibigContribution(request);
        return ResponseFactory.created(
                "Pag-IBIG contribution configuration created successfully",
                pagibigContributionMapper.toDto(contribution)
        );
    }

    @GetMapping
    @Operation(summary = "Get all Pag-IBIG configurations", description = "Retrieve all Pag-IBIG contribution configurations with pagination. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<List<PagibigContributionDto>>> getAllPagibigContributions(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int pageNo,
            @Parameter(description = "Number of items per page (1-100)") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @Parameter(description = "Filter by effective date (on or before)") @RequestParam(required = false) LocalDate effectiveDate
    ) {
        Page<PagibigContribution> page = pagibigContributionService.getAllPagibigContributions(pageNo, limit, effectiveDate);
        List<PagibigContributionDto> contributions = page.getContent().stream()
                .map(pagibigContributionMapper::toDto)
                .toList();

        return ResponseFactory.ok(
                "Pag-IBIG contribution configurations retrieved successfully",
                contributions,
                PaginationMeta.of(page)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Pag-IBIG configuration by ID", description = "Retrieve a specific Pag-IBIG contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<PagibigContributionDto>> getPagibigContributionById(
            @Parameter(description = "Configuration ID") @PathVariable UUID id) {
        PagibigContribution contribution = pagibigContributionService.getPagibigContributionById(id);
        return ResponseFactory.ok(
                "Pag-IBIG contribution configuration retrieved successfully",
                pagibigContributionMapper.toDto(contribution)
        );
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest Pag-IBIG configuration", description = "Retrieve the latest Pag-IBIG contribution configuration for a given date. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<PagibigContributionDto>> getLatestPagibigContribution(
            @Parameter(description = "Date to check (defaults to today)") @RequestParam(required = false) LocalDate date
    ) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        PagibigContribution contribution = pagibigContributionService.getLatestPagibigContribution(effectiveDate);
        return ResponseFactory.ok(
                "Latest Pag-IBIG contribution configuration retrieved successfully",
                pagibigContributionMapper.toDto(contribution)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Pag-IBIG configuration", description = "Update an existing Pag-IBIG contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<PagibigContributionDto>> updatePagibigContribution(
            @Parameter(description = "Configuration ID") @PathVariable UUID id,
            @Valid @RequestBody PagibigContributionRequest request) {
        PagibigContribution contribution = pagibigContributionService.updatePagibigContribution(id, request);
        return ResponseFactory.ok(
                "Pag-IBIG contribution configuration updated successfully",
                pagibigContributionMapper.toDto(contribution)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Pag-IBIG configuration", description = "Soft delete a Pag-IBIG contribution configuration. Requires PAYROLL role.")
    public ResponseEntity<ApiResponse<DeleteResponse>> deletePagibigContribution(
            @Parameter(description = "Configuration ID") @PathVariable UUID id) {
        pagibigContributionService.deletePagibigContribution(id);
        return ResponseFactory.ok(
                "Pag-IBIG contribution configuration deleted successfully",
                new DeleteResponse("PagibigContribution", id)
        );
    }
}
