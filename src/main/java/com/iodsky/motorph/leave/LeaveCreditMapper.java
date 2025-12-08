package com.iodsky.motorph.leave;

import com.iodsky.motorph.csvimport.CsvMapper;
import org.springframework.stereotype.Component;

@Component
public class LeaveCreditMapper implements CsvMapper<LeaveCredit, LeaveCreditCsvRecord> {

    public LeaveCreditDto toDto (LeaveCredit leaveCredit) {
        return LeaveCreditDto.builder()
                .type(leaveCredit.getType().toString())
                .credits(leaveCredit.getCredits())
                .build();
    }

    @Override
    public LeaveCredit toEntity(LeaveCreditCsvRecord leaveCreditCsvRecord) {
        return LeaveCredit.builder()
                .credits(leaveCreditCsvRecord.getCredits())
                .build();
    }
}
