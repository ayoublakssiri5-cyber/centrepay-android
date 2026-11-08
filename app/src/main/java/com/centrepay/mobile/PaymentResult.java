package com.centrepay.mobile;

public class PaymentResult {
    public final long paymentId;
    public final int allocatedFromPayment;
    public final int advanceCreated;
    public final int advanceAppliedBeforePayment;
    public final int outstandingAfterPayment;

    public PaymentResult(long paymentId, int allocatedFromPayment, int advanceCreated,
                         int advanceAppliedBeforePayment, int outstandingAfterPayment) {
        this.paymentId = paymentId;
        this.allocatedFromPayment = allocatedFromPayment;
        this.advanceCreated = advanceCreated;
        this.advanceAppliedBeforePayment = advanceAppliedBeforePayment;
        this.outstandingAfterPayment = outstandingAfterPayment;
    }
}
