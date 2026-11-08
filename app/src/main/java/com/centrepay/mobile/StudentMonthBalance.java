package com.centrepay.mobile;

public class StudentMonthBalance {
    public final String dueMonth;
    public final int totalAmount;
    public final int paidAmount;

    public StudentMonthBalance(String dueMonth, int totalAmount, int paidAmount) {
        this.dueMonth = dueMonth;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
    }

    public int remainingAmount() { return Math.max(0, totalAmount - paidAmount); }

    public String status() {
        if (paidAmount <= 0) return "Non payé";
        if (paidAmount >= totalAmount) return "Payé";
        return "Partiel";
    }
}
