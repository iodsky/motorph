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
import com.iodsky.motorph.security.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveCreditServiceTest {

    @Mock private LeaveCreditRepository leaveCreditRepository;
    @Mock private EmployeeService employeeService;
    @Mock private CsvService<LeaveCredit, LeaveCreditCsvRecord> leaveCreditCsvService;
    @InjectMocks private LeaveCreditService leaveCreditService;

    private User normalUser;
    private Employee employee;
    private LeaveCredit vacationCredit;
    private LeaveCredit sickCredit;

    @BeforeEach
    void setUp() {
        // Setup employee
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Juan");
        employee.setLastName("Dela Cruz");

        // Setup user
        UserRole normalRole = new UserRole();
        normalRole.setRole("EMPLOYEE");

        normalUser = new User();
        normalUser.setId(UUID.randomUUID());
        normalUser.setEmployee(employee);
        normalUser.setUserRole(normalRole);

        // Setup leave credits
        vacationCredit = LeaveCredit.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .type(LeaveType.VACATION)
                .credits(10.0)
                .version(0L)
                .build();

        sickCredit = LeaveCredit.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .type(LeaveType.SICK)
                .credits(5.0)
                .version(0L)
                .build();
    }

    @Nested
    class GetLeaveCreditByEmployeeIdAndTypeTests {

        @Test
        void shouldReturnLeaveCreditSuccessfully() {
            when(leaveCreditRepository.findByEmployee_IdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(Optional.of(vacationCredit));

            LeaveCredit result = leaveCreditService.getLeaveCreditByEmployeeIdAndType(1L, LeaveType.VACATION);

            assertNotNull(result);
            assertEquals(LeaveType.VACATION, result.getType());
            assertEquals(10.0, result.getCredits());
        }

        @Test
        void shouldThrowNotFoundWhenLeaveCreditDoesNotExist() {
            when(leaveCreditRepository.findByEmployee_IdAndType(eq(1L), eq(LeaveType.VACATION)))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    leaveCreditService.getLeaveCreditByEmployeeIdAndType(1L, LeaveType.VACATION));
            assertTrue(exception.getMessage().contains("VACATION"));
            assertTrue(exception.getMessage().contains("employeeId: 1"));
        }
    }

    @Nested
    class GetLeaveCreditsByEmployeeIdTests {

        @Test
        void shouldReturnAllLeaveCreditsForAuthenticatedEmployee() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn(normalUser);
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            when(leaveCreditRepository.findAllByEmployee_Id(eq(1L)))
                    .thenReturn(List.of(vacationCredit, sickCredit));

            List<LeaveCredit> result = leaveCreditService.getLeaveCreditsByEmployeeId();

            assertEquals(2, result.size());
            verify(leaveCreditRepository).findAllByEmployee_Id(eq(1L));
        }

        @Test
        void shouldThrowUnauthorizedWhenPrincipalIsNotUser() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);
            when(authentication.getPrincipal()).thenReturn("anonymousUser");
            when(context.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(context);

            assertThrows(UnauthorizedException.class, () ->
                    leaveCreditService.getLeaveCreditsByEmployeeId());
        }
    }

    @Nested
    class UpdateLeaveCreditTests {

        @Test
        void shouldUpdateLeaveCreditSuccessfully() {
            UUID creditId = vacationCredit.getId();
            LeaveCredit updatedCredit = LeaveCredit.builder()
                    .id(creditId)
                    .employee(employee)
                    .type(LeaveType.VACATION)
                    .credits(8.0)
                    .version(1L)
                    .build();

            when(leaveCreditRepository.findById(creditId))
                    .thenReturn(Optional.of(vacationCredit));
            when(leaveCreditRepository.save(any(LeaveCredit.class)))
                    .thenReturn(updatedCredit);

            LeaveCredit result = leaveCreditService.updateLeaveCredit(creditId, updatedCredit);

            assertNotNull(result);
            assertEquals(8.0, result.getCredits());
            verify(leaveCreditRepository).save(vacationCredit);
        }

        @Test
        void shouldThrowNotFoundWhenLeaveCreditDoesNotExist() {
            UUID creditId = UUID.randomUUID();
            when(leaveCreditRepository.findById(creditId))
                    .thenReturn(Optional.empty());

            LeaveCredit updatedCredit = LeaveCredit.builder()
                    .credits(8.0)
                    .build();

            assertThrows(NotFoundException.class, () ->
                    leaveCreditService.updateLeaveCredit(creditId, updatedCredit));
        }
    }

    @Nested
    class ImportLeaveCreditsTests {

        @Test
        void shouldImportLeaveCreditsSuccessfully() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
            when(file.getInputStream()).thenReturn(inputStream);

            LeaveCreditCsvRecord csvRecord1 = new LeaveCreditCsvRecord();
            csvRecord1.setEmployeeId(1L);
            csvRecord1.setType("VACATION");
            csvRecord1.setCredits(10.0);

            LeaveCreditCsvRecord csvRecord2 = new LeaveCreditCsvRecord();
            csvRecord2.setEmployeeId(1L);
            csvRecord2.setType("SICK");
            csvRecord2.setCredits(5.0);

            LeaveCredit entity1 = LeaveCredit.builder()
                    .credits(10.0)
                    .build();

            LeaveCredit entity2 = LeaveCredit.builder()
                    .credits(5.0)
                    .build();

            CsvResult<LeaveCredit, LeaveCreditCsvRecord> result1 = new CsvResult<>(entity1, csvRecord1);
            CsvResult<LeaveCredit, LeaveCreditCsvRecord> result2 = new CsvResult<>(entity2, csvRecord2);

            LinkedHashSet<CsvResult<LeaveCredit, LeaveCreditCsvRecord>> csvResults = new LinkedHashSet<>();
            csvResults.add(result1);
            csvResults.add(result2);

            when(leaveCreditCsvService.parseCsv(any(InputStream.class), eq(LeaveCreditCsvRecord.class)))
                    .thenReturn(csvResults);
            when(employeeService.getEmployeeById(1L)).thenReturn(employee);
            when(leaveCreditRepository.saveAll(any())).thenReturn(List.of(entity1, entity2));

            Integer count = leaveCreditService.importLeaveCredits(file);

            assertEquals(2, count);
            verify(leaveCreditRepository).saveAll(any());
        }

        @Test
        void shouldThrowBadRequestWhenInvalidLeaveType() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
            when(file.getInputStream()).thenReturn(inputStream);

            LeaveCreditCsvRecord csvRecord = new LeaveCreditCsvRecord();
            csvRecord.setEmployeeId(1L);
            csvRecord.setType("INVALID_TYPE");
            csvRecord.setCredits(10.0);

            LeaveCredit entity = LeaveCredit.builder()
                    .credits(10.0)
                    .build();

            CsvResult<LeaveCredit, LeaveCreditCsvRecord> result = new CsvResult<>(entity, csvRecord);

            LinkedHashSet<CsvResult<LeaveCredit, LeaveCreditCsvRecord>> csvResults = new LinkedHashSet<>();
            csvResults.add(result);

            when(leaveCreditCsvService.parseCsv(any(InputStream.class), eq(LeaveCreditCsvRecord.class)))
                    .thenReturn(csvResults);
            when(employeeService.getEmployeeById(1L)).thenReturn(employee);

            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    leaveCreditService.importLeaveCredits(file));
            assertTrue(exception.getMessage().contains("Invalid leave type"));
        }

        @Test
        void shouldThrowCsvImportExceptionWhenIOExceptionOccurs() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getInputStream()).thenThrow(new IOException("File read error"));

            assertThrows(CsvImportException.class, () ->
                    leaveCreditService.importLeaveCredits(file));
        }

        @Test
        void shouldResolveEmployeeCorrectly() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
            when(file.getInputStream()).thenReturn(inputStream);

            LeaveCreditCsvRecord csvRecord = new LeaveCreditCsvRecord();
            csvRecord.setEmployeeId(1L);
            csvRecord.setType("VACATION");
            csvRecord.setCredits(10.0);

            LeaveCredit entity = LeaveCredit.builder()
                    .credits(10.0)
                    .build();

            CsvResult<LeaveCredit, LeaveCreditCsvRecord> result = new CsvResult<>(entity, csvRecord);

            LinkedHashSet<CsvResult<LeaveCredit, LeaveCreditCsvRecord>> csvResults = new LinkedHashSet<>();
            csvResults.add(result);

            when(leaveCreditCsvService.parseCsv(any(InputStream.class), eq(LeaveCreditCsvRecord.class)))
                    .thenReturn(csvResults);
            when(employeeService.getEmployeeById(1L)).thenReturn(employee);
            when(leaveCreditRepository.saveAll(any())).thenReturn(List.of(entity));

            leaveCreditService.importLeaveCredits(file);

            verify(employeeService).getEmployeeById(1L);
        }
    }
}

