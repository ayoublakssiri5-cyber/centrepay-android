package com.centrepay.mobile;

public class TeacherLevelBalance {
    public final String level;
    public final int mathAmount;
    public final int frenchAmount;
    public final int pcAmount;
    public final int svtAmount;
    public final int totalAmount;

    public TeacherLevelBalance(String level, int mathAmount, int frenchAmount, int pcAmount, int svtAmount) {
        this.level = level;
        this.mathAmount = mathAmount;
        this.frenchAmount = frenchAmount;
        this.pcAmount = pcAmount;
        this.svtAmount = svtAmount;
        this.totalAmount = mathAmount + frenchAmount + pcAmount + svtAmount;
    }
}
