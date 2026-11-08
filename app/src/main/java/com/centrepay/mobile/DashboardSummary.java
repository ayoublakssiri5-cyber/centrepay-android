package com.centrepay.mobile;

public class DashboardSummary {
    public final int activeStudents;
    public final int receivedAmount;
    public final int outstandingAmount;
    public final int centerAmount;
    public final int teacherAmount;
    public final int advanceAmount;

    public DashboardSummary(int activeStudents, int receivedAmount, int outstandingAmount,
                            int centerAmount, int teacherAmount, int advanceAmount) {
        this.activeStudents = activeStudents;
        this.receivedAmount = receivedAmount;
        this.outstandingAmount = outstandingAmount;
        this.centerAmount = centerAmount;
        this.teacherAmount = teacherAmount;
        this.advanceAmount = advanceAmount;
    }
}
