package com.centrepay.mobile;

/** What recording a payment does, or would do: situation of the month, allocation and advances. */
public class PaymentPreview {
    public static final int OK = 0;
    public static final int INVALID = 1;
    public static final int BEFORE_ENROLLMENT = 2;

    public final int status;
    /** Due amount of the month (0 when no due exists). */
    public final int monthDue;
    /** Amount already applied to the month before this operation. */
    public final int monthPaidBefore;
    /** Existing advances applied to the month before the new payment. */
    public final int advanceApplied;
    /** Part of the new payment applied to the month. */
    public final int allocated;
    /** Surplus of the new payment kept as an advance. */
    public final int advanceCreated;
    /** Split of the allocated amount: math, french, pc, svt, center. */
    public final int[] split;
    /** Saved payment, or null for a preview or an error. */
    public final PaymentResult result;

    public PaymentPreview(int status, int monthDue, int monthPaidBefore, int advanceApplied, int allocated,
                          int advanceCreated, int[] split, PaymentResult result) {
        this.status = status;
        this.monthDue = monthDue;
        this.monthPaidBefore = monthPaidBefore;
        this.advanceApplied = advanceApplied;
        this.allocated = allocated;
        this.advanceCreated = advanceCreated;
        this.split = split;
        this.result = result;
    }

    static PaymentPreview error(int status) {
        return new PaymentPreview(status, 0, 0, 0, 0, 0, new int[FinanceRules.COMPONENTS], null);
    }

    /** What is still due for the month once existing advances are used. */
    public int remainingBeforePayment() {
        return Math.max(0, monthDue - monthPaidBefore - advanceApplied);
    }
}
