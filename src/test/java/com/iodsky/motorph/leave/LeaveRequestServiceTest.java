package com.iodsky.motorph.leave;

import com.iodsky.motorph.common.exception.ApiException;
import com.iodsky.motorph.employee.Employee;
import com.iodsky.motorph.security.user.User;
import com.iodsky.motorph.security.user.UserRole;
import com.iodsky.motorph.security.user.UserService;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveCreditService leaveCreditService;
    @Mock private LeaveRequestMapper leaveRequestMapper;
    @Mock private UserService userService;
    @InjectMocks private LeaveRequestService leaveRequestService;

    private User hrUser;
    private User normalUser;
    private Employee employee;
    private Employee otherEmployee;
    private LeaveRequestDto leaveRequestDto;
    private LeaveRequest leaveRequest;
    private LeaveCredit leaveCredit;

    @BeforeEach
    void setUp() {
        // Setup employees
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Juan");
        employee.setLastName("Dela Cruz");

        otherEmployee = new Employee();
        otherEmployee.setId(2L);
        otherEmployee.setFirstName("Maria");
        otherEmployee.setLastName("Santos");

        // Setup users
        UserRole hrRole = new UserRole();
        hrRole.setRole("HR");

        hrUser = new User();
        hrUser.setId(UUID.randomUUID());
        hrUser.setEmployee(employee);
        hrUser.setUserRole(hrRole);

        UserRole normalRole = new UserRole();
        normalRole.setRole("EMPLOYEE");

        normalUser = new User();
        normalUser.setId(UUID.randomUUID());
        normalUser.setEmployee(employee);
        normalUser.setUserRole(normalRole);

        leaveRequestDto = LeaveRequestDto.builder()
                .leaveType("VACATION")
                .startDate(LocalDate.of(2025, 12, 16)) // Monday
                .endDate(LocalDate.of(2025, 12, 19)) // Thursday
                .note("Year end vacation")
                .build();

        leaveRequest = LeaveRequest.builder()
                .id("LR-2025-001")
                .employee(employee)
                .leaveType(LeaveType.VACATION)
                .startDate(leaveRequestDto.getStartDate())
                .endDate(leaveRequestDto.getEndDate())
                .note(leaveRequestDto.getNote())
                .leaveStatus(LeaveStatus.PENDING)
                .build();

        leaveCredit = LeaveCredit.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .type(LeaveType.VACATION)
                .credits(10.0)
                .build();
    }

    @Nested
    class CreateLeaveRequestTests {

        @Test
        void shouldCreateLeaveRequestSuccessfully() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);
            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(eq(1L), any(), any()))
                    .thenReturn(false);
            when(leaveRequestRepository.existsByEmployee_IdAndLeaveStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    eq(1L), anyList(), any(), any()))
                    .thenReturn(false);
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);

            LeaveRequest result = leaveRequestService.createLeaveRequest(leaveRequestDto);

            assertNotNull(result);
            assertEquals(employee, result.getEmployee());
            assertEquals(LeaveType.VACATION, result.getLeaveType());
            assertEquals(LeaveStatus.PENDING, result.getLeaveStatus());
            verify(leaveRequestRepository).save(any(LeaveRequest.class));
        }

        @Test
        void shouldThrowUnauthorizedWhenPrincipalIsNotUser() {
            when(userService.getAuthenticatedUser()).thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }

        @Test
        void shouldThrowBadRequestWhenInsufficientLeaveCredits() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveCredit.setCredits(2.0); // Only 2 days available, but 4 days required
            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);
            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(eq(1L), any(), any()))
                    .thenReturn(false);
            when(leaveRequestRepository.existsByEmployee_IdAndLeaveStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    eq(1L), anyList(), any(), any()))
                    .thenReturn(false);

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        void shouldThrowBadRequestWhenStartDateIsAfterEndDate() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequestDto.setStartDate(LocalDate.of(2025, 12, 20));
            leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 15));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        void shouldThrowBadRequestWhenStartDateIsWeekend() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequestDto.setStartDate(LocalDate.of(2025, 12, 13)); // Saturday
            leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 16));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertEquals("Start date must be a weekday", ex.getMessage());
        }

        @Test
        void shouldThrowBadRequestWhenEndDateIsWeekend() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequestDto.setStartDate(LocalDate.of(2025, 12, 16));
            leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 20)); // Saturday

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertEquals("End date must be a weekday", ex.getMessage());
        }

        @Test
        void shouldThrowBadRequestWhenDuplicateLeaveRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(
                    eq(1L), any(), any()))
                    .thenReturn(true);

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.CONFLICT, ex.getStatus());
            assertEquals("Duplicate leave request", ex.getMessage());
        }

        @Test
        void shouldThrowBadRequestWhenOverlappingLeaveExists() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(eq(1L), any(), any()))
                    .thenReturn(false);
            when(leaveRequestRepository.existsByEmployee_IdAndLeaveStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    eq(1L), anyList(), any(), any()))
                    .thenReturn(true);

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("overlaps"));
        }

        @Test
        void shouldThrowBadRequestWhenInvalidLeaveType() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequestDto.setLeaveType("INVALID_TYPE");

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("Invalid leave type"));
        }
    }

    @Nested
    class GetLeaveRequestTests {

        @Test
        void shouldReturnAllLeaveRequestsWhenUserIsHR() {
            when(userService.getAuthenticatedUser()).thenReturn(hrUser);

            Pageable pageable = PageRequest.of(0, 10);
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest), pageable, 1);
            when(leaveRequestRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<LeaveRequest> result = leaveRequestService.getLeaveRequests(0, 10);

            assertEquals(1, result.getTotalElements());
            verify(leaveRequestRepository).findAll(any(Pageable.class));
        }

        @Test
        void shouldReturnEmployeeLeaveRequestsWhenUserIsNotHR() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            Pageable pageable = PageRequest.of(0, 10);
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest), pageable, 1);
            when(leaveRequestRepository.findAllByEmployee_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(page);

            Page<LeaveRequest> result = leaveRequestService.getLeaveRequests(0, 10);

            assertEquals(1, result.getTotalElements());
            verify(leaveRequestRepository).findAllByEmployee_Id(eq(1L), any(Pageable.class));
        }

        @Test
        void shouldThrowUnauthorizedWhenPrincipalIsNotUser() {
            when(userService.getAuthenticatedUser()).thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.getLeaveRequests(0, 10));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }

    @Nested
    class GetLeaveRequestByIdTests {

        @Test
        void shouldReturnLeaveRequestById() {
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            LeaveRequest result = leaveRequestService.getLeaveRequestById("LR-2025-001");

            assertEquals(leaveRequest, result);
            assertEquals("LR-2025-001", result.getId());
        }

        @Test
        void shouldThrowNotFoundWhenLeaveRequestDoesNotExist() {
            when(leaveRequestRepository.findById("LR-2025-999"))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.getLeaveRequestById("LR-2025-999"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
            assertTrue(ex.getMessage().contains("LR-2025-999"));
        }
    }

    @Nested
    class UpdateLeaveRequestTests {

        @Test
        void shouldUpdateLeaveRequestSuccessfully() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveRequestMapper.updateEntity(any(LeaveRequest.class), any(LeaveRequestDto.class)))
                    .thenReturn(leaveRequest);
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);

            LeaveRequest result = leaveRequestService.updateLeaveRequest("LR-2025-001", leaveRequestDto);

            assertNotNull(result);
            verify(leaveRequestRepository).save(leaveRequest);
        }

        @Test
        void shouldAllowHRToUpdateOtherEmployeeLeaveRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(hrUser);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveRequestMapper.updateEntity(any(LeaveRequest.class), any(LeaveRequestDto.class)))
                    .thenReturn(leaveRequest);
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);

            LeaveRequest result = leaveRequestService.updateLeaveRequest("LR-2025-001", leaveRequestDto);

            assertNotNull(result);
            verify(leaveRequestRepository).save(leaveRequest);
        }

        @Test
        void shouldThrowForbiddenWhenNonHRUpdatesOtherEmployeeRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.updateLeaveRequest("LR-2025-001", leaveRequestDto));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }

        @Test
        void shouldThrowBadRequestWhenUpdatingProcessedRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.updateLeaveRequest("LR-2025-001", leaveRequestDto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("Cannot delete processed"));
        }
    }

    @Nested
    class UpdateLeaveStatusTests {

        @Test
        void shouldApproveLeaveRequestAndDeductCredits() {
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);
            when(leaveCreditService.updateLeaveCredit(any(UUID.class), any(LeaveCredit.class)))
                    .thenReturn(leaveCredit);
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);

            LeaveRequest result = leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED);

            assertEquals(LeaveStatus.APPROVED, result.getLeaveStatus());
            verify(leaveCreditService).updateLeaveCredit(any(UUID.class), any(LeaveCredit.class));
            verify(leaveRequestRepository).save(leaveRequest);
        }

        @Test
        void shouldRejectLeaveRequestWithoutDeductingCredits() {
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);

            LeaveRequest result = leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.REJECTED);

            assertEquals(LeaveStatus.REJECTED, result.getLeaveStatus());
            verify(leaveCreditService, never()).updateLeaveCredit(any(), any());
            verify(leaveRequestRepository).save(leaveRequest);
        }

        @Test
        void shouldThrowBadRequestWhenAlreadyProcessed() {
            leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("already been processed"));
        }

        @Test
        void shouldThrowBadRequestWhenInsufficientCreditsForApproval() {
            leaveCredit.setCredits(2.0); // Only 2 days, but 4 days required
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("Insufficient credits"));
        }

        @Test
        void shouldThrowBadRequestWhenOptimisticLockOccurs() {
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);
            when(leaveCreditService.updateLeaveCredit(any(UUID.class), any(LeaveCredit.class)))
                    .thenReturn(leaveCredit);
            when(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .thenThrow(new OptimisticLockException());

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("modified by another process"));
        }
    }

    @Nested
    class DeleteLeaveRequestTests {

        @Test
        void shouldDeleteLeaveRequestSuccessfully() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            leaveRequestService.deleteLeaveRequest("LR-2025-001");

            verify(leaveRequestRepository).delete(leaveRequest);
        }

        @Test
        void shouldAllowHRToDeleteOtherEmployeeLeaveRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(hrUser);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            leaveRequestService.deleteLeaveRequest("LR-2025-001");

            verify(leaveRequestRepository).delete(leaveRequest);
        }

        @Test
        void shouldThrowForbiddenWhenNonHRDeletesOtherEmployeeRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.deleteLeaveRequest("LR-2025-001"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }

        @Test
        void shouldThrowBadRequestWhenDeletingProcessedRequest() {
            when(userService.getAuthenticatedUser()).thenReturn(normalUser);

            leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            ApiException ex = assertThrows(ApiException.class, () ->
                    leaveRequestService.deleteLeaveRequest("LR-2025-001"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("Cannot delete processed"));
        }
    }
}
