package com.iodsky.motorph.leave;

import com.iodsky.motorph.common.PageDto;
import com.iodsky.motorph.common.PageMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveCreditService leaveCreditService;
    private final LeaveRequestService leaveRequestService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveCreditMapper leaveCreditMapper;

    @PostMapping
    public ResponseEntity<LeaveRequestDto> createLeave(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequest leaveRequest = leaveRequestService.createLeaveRequest(dto);

        return new ResponseEntity<>(leaveRequestMapper.toDto(leaveRequest), HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<PageDto<LeaveRequestDto>> getLeaveRequests(
            @RequestParam(defaultValue = "0") @Min(0) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        Page<LeaveRequest> page = leaveRequestService.getLeaveRequests(pageNum, limit);

        return ResponseEntity.ok(PageMapper.map(page, leaveRequestMapper::toDto));
    }

    @GetMapping("/{leaveRequestId}")
    public ResponseEntity<LeaveRequestDto> getLeaveRequestById(@PathVariable String leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(leaveRequestId);

        return ResponseEntity.ok(leaveRequestMapper.toDto(leaveRequest));
    }

    @PutMapping("/{leaveRequestId}")
    public ResponseEntity<LeaveRequestDto> updateLeaveRequest(
            @PathVariable String leaveRequestId,
            @Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequest leaveRequest = leaveRequestService.updateLeaveRequest(leaveRequestId, dto);

        return ResponseEntity.ok(leaveRequestMapper.toDto(leaveRequest));
    }

    @PreAuthorize("hasRole('HR')")
    @PatchMapping("/{leaveRequestId}")
    public ResponseEntity<LeaveRequestDto> updateLeaveStatus(
            @PathVariable String leaveRequestId,
            @Valid @RequestBody UpdateLeaveStatusDto dto) {
        LeaveRequest leaveRequest = leaveRequestService.updateLeaveStatus(leaveRequestId, dto.getStatus());

        return ResponseEntity.ok(leaveRequestMapper.toDto(leaveRequest));
    }

    @DeleteMapping("/{leaveRequestId}")
    public ResponseEntity<Map<String, String>> deleteLeaveRequest(@PathVariable String leaveRequestId) {
        leaveRequestService.deleteLeaveRequest(leaveRequestId);
        return ResponseEntity.ok(Map.of("success", "true"));
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping(value = "/credits", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LeaveCreditDto>> initializeEmployeeLeaveCredits(@Valid @RequestBody InitializeEmployeeLeaveCreditsDto dto) {
        List<LeaveCreditDto> leaveCredits = leaveCreditService.initializeEmployeeLeaveCredits(dto)
                .stream()
                .map(leaveCreditMapper::toDto)
                .toList();
        return new ResponseEntity<>(leaveCredits, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping(value = "/credits", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importLeaveCredits(@RequestPart MultipartFile file) {
        Integer count = leaveCreditService.importLeaveCredits(file);
        return new ResponseEntity<>(Map.of("recordsCreated", count), HttpStatus.OK);
    }

    @GetMapping("/credits")
    public ResponseEntity<List<LeaveCreditDto>> getLeaveCredits() {
        List<LeaveCreditDto> credits = leaveCreditService.getLeaveCreditsByEmployeeId()
                .stream().map(leaveCreditMapper::toDto).toList();
        return ResponseEntity.ok(credits);
    }

    @DeleteMapping("/credits/employee/{employeeId}")
    public ResponseEntity<Map<String, String>> deleteLeaveCreditsByEmployeeId(@PathVariable Long employeeId) {
        leaveCreditService.deleteLeaveCreditsByEmployeeId(employeeId);
        return ResponseEntity.ok(Map.of("success", "true"));
    }

}
