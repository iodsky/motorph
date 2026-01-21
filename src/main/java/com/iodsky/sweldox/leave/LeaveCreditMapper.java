package com.iodsky.sweldox.leave;

import org.springframework.stereotype.Component;

@Component
public class LeaveCreditMapper {

    public LeaveCreditDto toDto (LeaveCredit leaveCredit) {
        return LeaveCreditDto.builder()
                .id(leaveCredit.getId())
                .employeeId(leaveCredit.getEmployee().getId())
                .type(leaveCredit.getType().toString())
                .credits(leaveCredit.getCredits())
                .fiscalYear(leaveCredit.getFiscalYear())
                .build();
    }
}
