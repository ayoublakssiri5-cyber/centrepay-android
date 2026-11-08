package com.centrepay.mobile;

public class StudentFinancialSummary {
    public final int dueAmount;
    public final int paidAmount;
    public final int outstandingAmount;
    public final int availableAdvance;

    public StudentFinancialSummary(int dueAmount, int paidAmount, int outstandingAmount, int availableAdvance) {
        this.dueAmount = dueAmount;
        this.paidAmount = paidAmount;
        this.outstandingAmount = outstandingAmount;
        this.availableAdvance = availableAdvance;
    }
}
