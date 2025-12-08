package com.iodsky.motorph.leave;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class LeaveRequestIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        LeaveRequest leaveRequest = (LeaveRequest) o;

        return leaveRequest.getRequestDate() + "-" + leaveRequest.getEmployee().getId();
    }
}
