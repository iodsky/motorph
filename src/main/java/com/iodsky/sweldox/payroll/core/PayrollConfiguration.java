package com.iodsky.sweldox.payroll.core;

import com.iodsky.sweldox.payroll.contribution.pagIbig.PagibigContribution;
import com.iodsky.sweldox.payroll.contribution.philhealth.PhilhealthContribution;
import com.iodsky.sweldox.payroll.contribution.sss.SssContribution;
import com.iodsky.sweldox.payroll.tax.IncomeTaxBracket;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Holds preloaded payroll configuration data to avoid repeated database queries
 * when calculating multiple payrolls for the same period.
 */
@Getter
@Builder
public class PayrollConfiguration {
    private PhilhealthContribution philhealthContribution;
    private PagibigContribution pagibigContribution;
    private SssContribution sssContribution;
    private List<IncomeTaxBracket> incomeTaxBrackets;
}
