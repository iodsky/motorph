package com.iodsky.motorph.leave;

import com.iodsky.motorph.common.exception.BadRequestException;
import com.iodsky.motorph.common.exception.NotFoundException;
import com.iodsky.motorph.common.exception.UnauthorizedException;
import com.iodsky.motorph.employee.model.Employee;
import com.iodsky.motorph.security.user.User;
import com.iodsky.motorph.security.user.UserRole;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
                .requestDate(LocalDate.now())
                .startDate(leaveRequestDto.getStartDate())
                .endDate(leaveRequestDto.getEndDate())
                .note(leaveRequestDto.getNote())
                .leaveStatus(LeaveStatus.PENDING)
                .version(0L)
                .build();

        leaveCredit = LeaveCredit.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .type(LeaveType.VACATION)
                .credits(10.0)
                .version(0L)
                .build();
    }

    @Nested
    class CreateLeaveRequestTests {

        @Test
        void shouldCreateLeaveRequestSuccessfully() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

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
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn("anonymousUser");
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            assertThrows(UnauthorizedException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
        }

        @Test
        void shouldThrowBadRequestWhenInsufficientLeaveCredits() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveCredit.setCredits(2.0); // Only 2 days available, but 4 days required
            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);
            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(eq(1L), any(), any()))
                    .thenReturn(false);
            when(leaveRequestRepository.existsByEmployee_IdAndLeaveStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    eq(1L), anyList(), any(), any()))
                    .thenReturn(false);

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
            assertTrue(exception.getMessage().contains("Insufficient leave credits"));
        }

        @Test
        void shouldThrowBadRequestWhenStartDateIsAfterEndDate() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequestDto.setStartDate(LocalDate.of(2025, 12, 20));
            leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 15));

            assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
        }

        @Test
        void shouldThrowBadRequestWhenStartDateIsWeekend() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequestDto.setStartDate(LocalDate.of(2025, 12, 13)); // Saturday
            leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 16));

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
            assertEquals("Start date must be a weekday", exception.getMessage());
        }

        @Test
        void shouldThrowBadRequestWhenEndDateIsWeekend() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequestDto.setStartDate(LocalDate.of(2025, 12, 16));
            leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 20)); // Saturday

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
            assertEquals("End date must be a weekday", exception.getMessage());
        }

        @Test
        void shouldThrowBadRequestWhenDuplicateLeaveRequest() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(
                    eq(1L), any(), any()))
                    .thenReturn(true);

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
            assertEquals("Duplicate leave request", exception.getMessage());
        }

        @Test
        void shouldThrowBadRequestWhenOverlappingLeaveExists() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            when(leaveRequestRepository.existsByEmployee_IdAndStartDateAndEndDate(eq(1L), any(), any()))
                    .thenReturn(false);
            when(leaveRequestRepository.existsByEmployee_IdAndLeaveStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    eq(1L), anyList(), any(), any()))
                    .thenReturn(true);

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
            assertTrue(exception.getMessage().contains("overlaps"));
        }

        @Test
        void shouldThrowBadRequestWhenInvalidLeaveType() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequestDto.setLeaveType("INVALID_TYPE");

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.createLeaveRequest(leaveRequestDto));
            assertTrue(exception.getMessage().contains("Invalid leave type"));
        }
    }

    @Nested
    class GetLeaveRequestTests {

        @Test
        void shouldReturnAllLeaveRequestsWhenUserIsHR() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(hrUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            Pageable pageable = PageRequest.of(0, 10);
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest), pageable, 1);
            when(leaveRequestRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<LeaveRequest> result = leaveRequestService.getLeaveRequests(0, 10);

            assertEquals(1, result.getTotalElements());
            verify(leaveRequestRepository).findAll(any(Pageable.class));
        }

        @Test
        void shouldReturnEmployeeLeaveRequestsWhenUserIsNotHR() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

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
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn("anonymousUser");
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            assertThrows(UnauthorizedException.class, () ->
                    leaveRequestService.getLeaveRequests(0, 10));
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

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    leaveRequestService.getLeaveRequestById("LR-2025-999"));
            assertTrue(exception.getMessage().contains("LR-2025-999"));
        }
    }

    @Nested
    class UpdateLeaveRequestTests {

        @Test
        void shouldUpdateLeaveRequestSuccessfully() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

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
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(hrUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

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
        void shouldThrowUnauthorizedWhenNonHRUpdatesOtherEmployeeRequest() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            assertThrows(UnauthorizedException.class, () ->
                    leaveRequestService.updateLeaveRequest("LR-2025-001", leaveRequestDto));
        }

        @Test
        void shouldThrowBadRequestWhenUpdatingProcessedRequest() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.updateLeaveRequest("LR-2025-001", leaveRequestDto));
            assertTrue(exception.getMessage().contains("Cannot delete processed"));
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

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED));
            assertTrue(exception.getMessage().contains("already been processed"));
        }

        @Test
        void shouldThrowBadRequestWhenInsufficientCreditsForApproval() {
            leaveCredit.setCredits(2.0); // Only 2 days, but 4 days required
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));
            when(leaveCreditService.getLeaveCreditByEmployeeIdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(leaveCredit);

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED));
            assertTrue(exception.getMessage().contains("Insufficient credits"));
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

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.updateLeaveStatus("LR-2025-001", LeaveStatus.APPROVED));
            assertTrue(exception.getMessage().contains("modified by another process"));
        }
    }

    @Nested
    class DeleteLeaveRequestTests {

        @Test
        void shouldDeleteLeaveRequestSuccessfully() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            leaveRequestService.deleteLeaveRequest("LR-2025-001");

            verify(leaveRequestRepository).delete(leaveRequest);
        }

        @Test
        void shouldAllowHRToDeleteOtherEmployeeLeaveRequest() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(hrUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            leaveRequestService.deleteLeaveRequest("LR-2025-001");

            verify(leaveRequestRepository).delete(leaveRequest);
        }

        @Test
        void shouldThrowUnauthorizedWhenNonHRDeletesOtherEmployeeRequest() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequest.setEmployee(otherEmployee);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            assertThrows(UnauthorizedException.class, () ->
                    leaveRequestService.deleteLeaveRequest("LR-2025-001"));
        }

        @Test
        void shouldThrowBadRequestWhenDeletingProcessedRequest() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
            when(leaveRequestRepository.findById("LR-2025-001"))
                    .thenReturn(Optional.of(leaveRequest));

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveRequestService.deleteLeaveRequest("LR-2025-001"));
            assertTrue(exception.getMessage().contains("Cannot delete processed"));
        }
    }
}