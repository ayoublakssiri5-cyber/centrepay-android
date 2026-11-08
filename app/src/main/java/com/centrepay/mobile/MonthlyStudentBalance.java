package com.centrepay.mobile;

public class MonthlyStudentBalance {
    public final long studentId;
    public final String studentName;
    public final String level;
    public final int totalAmount;
    public final int paidAmount;
    public final int remainingAmount;

    public MonthlyStudentBalance(long studentId, String studentName, String level, int totalAmount, int paidAmount) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.level = level;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = Math.max(0, totalAmount - paidAmount);
    }
}
