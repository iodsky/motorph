package com.iodsky.motorph.leave;

import com.iodsky.motorph.csvimport.CsvMapper;
import org.springframework.stereotype.Component;

@Component
public class LeaveCreditMapper implements CsvMapper<LeaveCredit, LeaveCreditCsvRecord> {

    @Override
    public LeaveCredit toEntity(LeaveCreditCsvRecord leaveCreditCsvRecord) {
        return LeaveCredit.builder()
                .credits(leaveCreditCsvRecord.getCredits())
                .build();
    }
}
