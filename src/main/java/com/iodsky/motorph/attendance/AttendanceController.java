package com.iodsky.motorph.attendance;

import com.iodsky.motorph.common.PageDto;
import com.iodsky.motorph.common.PageMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @PostMapping
    public ResponseEntity<AttendanceDto> createAttendance(@Valid @RequestBody(required = false) AttendanceDto attendanceDto) {
        Attendance attendance = attendanceService.createAttendance(attendanceDto);
        AttendanceDto dto = attendanceMapper.toDto(attendance);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping
    public ResponseEntity<PageDto<AttendanceDto>> getAllAttendances(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        Page<Attendance> attendances = attendanceService.getAllAttendances(page, limit, startDate, endDate);

        return ResponseEntity.ok(PageMapper.map(attendances, attendanceMapper::toDto));
    }

    @GetMapping("/me")
    public ResponseEntity<PageDto<AttendanceDto>> getMyAttendances(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        Page<Attendance> attendances = attendanceService
                .getEmployeeAttendances(page, limit, null, startDate, endDate);

        return ResponseEntity.ok(PageMapper.map(attendances, attendanceMapper::toDto));
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/employee/{id}")
    public ResponseEntity<PageDto<AttendanceDto>> getEmployeeAttendancesForHR(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        Page<Attendance> attendances = attendanceService
                .getEmployeeAttendances(page, limit, id, startDate, endDate);

        return ResponseEntity.ok(PageMapper.map(attendances, attendanceMapper::toDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AttendanceDto> updateAttendance(@PathVariable UUID id, @Valid @RequestBody(required = false) AttendanceDto attendanceDto) {
        Attendance attendance = attendanceService.updateAttendance(id, attendanceDto);
        AttendanceDto dto = attendanceMapper.toDto(attendance);
        return ResponseEntity.ok(dto);
    }
}
