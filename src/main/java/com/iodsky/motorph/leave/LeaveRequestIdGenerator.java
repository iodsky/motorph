package com.iodsky.motorph.leave;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class LeaveRequestIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        LeaveRequest leaveRequest = (LeaveRequest) o;

        return leaveRequest.getCreatedAt() + "-" +
                leaveRequest.getEmployee().getId() + "-" +
                UUID.randomUUID().toString().substring(0, 8);
    }
}
