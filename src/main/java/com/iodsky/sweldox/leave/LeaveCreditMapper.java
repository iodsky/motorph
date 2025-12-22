package com.iodsky.sweldox.leave;

import com.iodsky.sweldox.csvimport.CsvMapper;
import org.springframework.stereotype.Component;

@Component
public class LeaveCreditMapper implements CsvMapper<LeaveCredit, LeaveCreditCsvRecord> {

    public LeaveCreditDto toDto (LeaveCredit leaveCredit) {
        return LeaveCreditDto.builder()
                .id(leaveCredit.getId())
                .employeeId(leaveCredit.getEmployee().getId())
                .type(leaveCredit.getType().toString())
                .credits(leaveCredit.getCredits())
                .fiscalYear(leaveCredit.getFiscalYear())
                .build();
    }

    @Override
    public LeaveCredit toEntity(LeaveCreditCsvRecord leaveCreditCsvRecord) {
        return LeaveCredit.builder()
                .credits(leaveCreditCsvRecord.getCredits())
                .build();
    }
}
