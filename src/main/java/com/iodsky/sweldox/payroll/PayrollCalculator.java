package com.iodsky.sweldox.payroll;

import com.iodsky.sweldox.attendance.Attendance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class PayrollCalculator {

    private static final BigDecimal PHILHEALTH_RATE = BigDecimal.valueOf(0.03);
    private static final BigDecimal PHILHEALTH_MAX_PREMIUM = BigDecimal.valueOf(1800);

    private static final BigDecimal PAGIBIG_EMPLOYEE_RATE_BELOW_1500 = BigDecimal.valueOf(0.01);
    private static final BigDecimal PAGIBIG_EMPLOYEE_RATE_ABOVE_1500 = BigDecimal.valueOf(0.02);
    private static final BigDecimal PAGIBIG_MAX_EMPLOYEE_CONTRIBUTION = BigDecimal.valueOf(100);

    private static final TreeMap<BigDecimal, BigDecimal> SSS_TABLE = new TreeMap<>();
    private static final BigDecimal SSS_STARTING_MSC = BigDecimal.valueOf(3250);
    private static final BigDecimal SSS_MIN_CONTRIBUTION = BigDecimal.valueOf(135);
    private static final BigDecimal SSS_INCREMENT_RATE = BigDecimal.valueOf(22.5);

    private static final List<TaxBracket> TAX_BRACKETS = Arrays.asList(
        new TaxBracket("0", "20832", "0", "0", "0"),
        new TaxBracket("20833", "33332", "0", "0.20", "20833"),
        new TaxBracket("33333", "66666", "2500", "0.25", "33333"),
        new TaxBracket("66667", "166666", "10833", "0.30", "66667"),
        new TaxBracket("166667", "666666", "40833.33", "0.32", "166667"),
        new TaxBracket("666667", "MAX", "200833.33", "0.35", "666667")
    );

    private static final BigDecimal SEMI_MONTHLY_DIVISOR = BigDecimal.valueOf(2);
    private static final BigDecimal OVERTIME_MULTIPLIER = BigDecimal.valueOf(1.25);
    private static final int STANDARD_WORK_HOURS = 8;

    static {
        for (int i = 0; i < 44; i++) {
            BigDecimal range = SSS_STARTING_MSC.add(BigDecimal.valueOf(i).multiply(BigDecimal.valueOf(500)));
            BigDecimal amount = SSS_MIN_CONTRIBUTION.add(SSS_INCREMENT_RATE.multiply(BigDecimal.valueOf(i)));
            SSS_TABLE.put(range, amount);
        }
    }

    private PayrollCalculator() {}

    public static BigDecimal calculateTotalHours(List<Attendance> attendances) {
        return attendances.stream()
                .map(Attendance::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calculateOvertimeHours(List<Attendance> attendances) {
        return attendances.stream()
                .map(Attendance::getOvertime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calculateDailyRate(BigDecimal hourlyRate) {
        return hourlyRate.multiply(BigDecimal.valueOf(STANDARD_WORK_HOURS));
    }

    public static BigDecimal calculateRegularPay(BigDecimal hourlyRate, BigDecimal regularHours) {
        return hourlyRate.multiply(regularHours);
    }

    public static BigDecimal calculateOvertimePay(BigDecimal hourlyRate, BigDecimal overtimeHours) {
        return hourlyRate
                .multiply(overtimeHours)
                .multiply(OVERTIME_MULTIPLIER);
    }

    public static BigDecimal calculateGrossPay(BigDecimal regularPay, BigDecimal overtimePay) {
        return regularPay.add(overtimePay).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalBenefits(List<Benefit> benefits) {
        return benefits.stream()
                .map(Benefit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal getBenefitByType(List<Benefit> benefits, String type) {
        return benefits.stream()
                .filter(b -> b.getBenefitType().getId().equalsIgnoreCase(type))
                .map(Benefit::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public static BigDecimal calculatePhilhealthDeduction(BigDecimal basicSalary) {
        BigDecimal monthlyPremium = basicSalary.multiply(PHILHEALTH_RATE)
                .min(PHILHEALTH_MAX_PREMIUM);
        BigDecimal employeeShare = monthlyPremium.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        return employeeShare.divide(SEMI_MONTHLY_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePagibigDeduction(BigDecimal basicSalary) {
        BigDecimal contribution;
        if (basicSalary.compareTo(BigDecimal.valueOf(1500)) > 0) {
            contribution = basicSalary.multiply(PAGIBIG_EMPLOYEE_RATE_ABOVE_1500);
        } else {
            contribution = basicSalary.multiply(PAGIBIG_EMPLOYEE_RATE_BELOW_1500);
        }
        return contribution.min(PAGIBIG_MAX_EMPLOYEE_CONTRIBUTION)
                .divide(SEMI_MONTHLY_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateSssDeduction(BigDecimal basicSalary) {
        if (basicSalary.compareTo(SSS_STARTING_MSC) < 0) {
            return SSS_MIN_CONTRIBUTION.divide(SEMI_MONTHLY_DIVISOR, 2, RoundingMode.HALF_UP);
        }
        var entry = SSS_TABLE.floorEntry(basicSalary);
        if (entry == null) {
            return BigDecimal.ZERO;
        }
        return entry.getValue().divide(SEMI_MONTHLY_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateWithholdingTax(BigDecimal taxableIncome) {
        BigDecimal tax = TAX_BRACKETS.stream()
                .filter(bracket -> bracket.isInBracket(taxableIncome))
                .findFirst()
                .map(bracket -> bracket.calculateTax(taxableIncome))
                .orElse(BigDecimal.ZERO);

        return tax.divide(SEMI_MONTHLY_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalStatutoryDeductions(
            BigDecimal sss, BigDecimal philhealth, BigDecimal pagibig) {
        return sss.add(philhealth).add(pagibig).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTaxableIncome(BigDecimal grossPay, BigDecimal statutoryDeductions) {
        return grossPay.subtract(statutoryDeductions).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateNetPay(
            BigDecimal grossPay, BigDecimal totalBenefits,
            BigDecimal statutoryDeductions, BigDecimal withholdingTax) {
        return grossPay.add(totalBenefits)
                .subtract(statutoryDeductions)
                .subtract(withholdingTax)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static class TaxBracket {
        private final BigDecimal lowerBound;
        private final BigDecimal upperBound;
        private final BigDecimal baseTax;
        private final BigDecimal rate;
        private final BigDecimal excessOver;

        public TaxBracket(String lowerBound, String upperBound, String baseTax, String rate, String excessOver) {
            this.lowerBound = new BigDecimal(lowerBound);
            this.upperBound = upperBound.equals("MAX") ? new BigDecimal(Long.MAX_VALUE) : new BigDecimal(upperBound);
            this.baseTax = new BigDecimal(baseTax);
            this.rate = new BigDecimal(rate);
            this.excessOver = new BigDecimal(excessOver);
        }

        public boolean isInBracket(BigDecimal income) {
            return income.compareTo(lowerBound) >= 0 && income.compareTo(upperBound) <= 0;
        }

        public BigDecimal calculateTax(BigDecimal income) {
            BigDecimal excessAmount = income.subtract(excessOver);
            BigDecimal excessTax = excessAmount.multiply(rate);
            return baseTax.add(excessTax);
        }
    }
}