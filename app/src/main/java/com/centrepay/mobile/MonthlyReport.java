package com.centrepay.mobile;

import java.util.List;

public class MonthlyReport {
    public final String month;
    public final int receivedAmount;
    public final int centerAmount;
    public final int teachersEarnedAmount;
    public final int teachersPaidAmount;
    public final int cashAfterTeacherPayments;
    public final int paidCount;
    public final int partialCount;
    public final int unpaidCount;
    public final List<MonthlyStudentBalance> unpaidStudents;

    public MonthlyReport(String month, int receivedAmount, int centerAmount, int teachersEarnedAmount,
                         int teachersPaidAmount, int paidCount, int partialCount, int unpaidCount,
                         List<MonthlyStudentBalance> unpaidStudents) {
        this.month = month;
        this.receivedAmount = receivedAmount;
        this.centerAmount = centerAmount;
        this.teachersEarnedAmount = teachersEarnedAmount;
        this.teachersPaidAmount = teachersPaidAmount;
        this.cashAfterTeacherPayments = receivedAmount - teachersPaidAmount;
        this.paidCount = paidCount;
        this.partialCount = partialCount;
        this.unpaidCount = unpaidCount;
        this.unpaidStudents = unpaidStudents;
    }
}
