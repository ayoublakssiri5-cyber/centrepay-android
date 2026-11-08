package com.centrepay.mobile;

public class PaymentRecord {
    public final long id;
    public final long studentId;
    public final String studentName;
    public final String studentLevel;
    public final int amount;
    public final int allocatedAmount;
    public final int advanceAmount;
    public final int advanceBalance;
    public final int mathAmount;
    public final int frenchAmount;
    public final int pcAmount;
    public final int svtAmount;
    public final int centerAmount;
    public final String paymentDate;
    public final String referenceMonth;
    public final String note;

    public PaymentRecord(long id, long studentId, String studentName, String studentLevel, int amount,
                         int allocatedAmount, int advanceAmount, int advanceBalance, int mathAmount,
                         int frenchAmount, int pcAmount, int svtAmount, int centerAmount,
                         String paymentDate, String referenceMonth, String note) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentLevel = studentLevel;
        this.amount = amount;
        this.allocatedAmount = allocatedAmount;
        this.advanceAmount = advanceAmount;
        this.advanceBalance = advanceBalance;
        this.mathAmount = mathAmount;
        this.frenchAmount = frenchAmount;
        this.pcAmount = pcAmount;
        this.svtAmount = svtAmount;
        this.centerAmount = centerAmount;
        this.paymentDate = paymentDate;
        this.referenceMonth = referenceMonth;
        this.note = note;
    }
}
