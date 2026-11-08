package com.centrepay.mobile;

public class TeacherBalance {
    public final Teacher teacher;
    public final int mathAmount;
    public final int frenchAmount;
    public final int pcAmount;
    public final int svtAmount;
    public final int earnedAmount;
    public final int paidAmount;
    public final int remainingAmount;
    public final String dueMonth;

    public TeacherBalance(Teacher teacher, int mathAmount, int frenchAmount, int pcAmount, int svtAmount,
                          int paidAmount) {
        this(teacher, mathAmount, frenchAmount, pcAmount, svtAmount, paidAmount, "");
    }

    public TeacherBalance(Teacher teacher, int mathAmount, int frenchAmount, int pcAmount, int svtAmount,
                          int paidAmount, String dueMonth) {
        this.teacher = teacher;
        this.mathAmount = mathAmount;
        this.frenchAmount = frenchAmount;
        this.pcAmount = pcAmount;
        this.svtAmount = svtAmount;
        this.earnedAmount = mathAmount + frenchAmount + pcAmount + svtAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = Math.max(0, earnedAmount - paidAmount);
        this.dueMonth = dueMonth == null ? "" : dueMonth;
    }
}
