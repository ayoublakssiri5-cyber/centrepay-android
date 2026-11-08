package com.centrepay.mobile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FinanceRulesTest {
    // Math 120, PC 100, Centre 80 = 300 DH / month
    private static final int[] PARTS = {120, 0, 100, 0, 80};

    private static FinanceRules.Due due(long id, String month) {
        return new FinanceRules.Due(id, month, 300, PARTS, 0, new int[5]);
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int value : values) total += value;
        return total;
    }

    @Test
    public void splitExactPaymentFollowsTheTariff() {
        assertArrayEquals(new int[]{120, 0, 100, 0, 80}, FinanceRules.splitAmount(300, PARTS));
    }

    @Test
    public void splitNeverLosesOrCreatesMoney() {
        for (int amount = 0; amount <= 1000; amount++) {
            assertEquals(amount, sum(FinanceRules.splitAmount(amount, PARTS)));
        }
    }

    @Test
    public void splitWithoutPartsGivesNothing() {
        assertArrayEquals(new int[5], FinanceRules.splitAmount(100, new int[5]));
    }

    @Test
    public void partialPaymentsAddUpExactlyToTheTariff() {
        FinanceRules.Due due = due(1, "2026-09");
        List<FinanceRules.Application> first = FinanceRules.allocate(10, 101, Arrays.asList(due));
        List<FinanceRules.Application> second = FinanceRules.allocate(11, 199, Arrays.asList(due));
        assertEquals(101, FinanceRules.totalAmount(first));
        assertEquals(199, FinanceRules.totalAmount(second));
        int[] total = FinanceRules.totalSplit(first);
        int[] more = FinanceRules.totalSplit(second);
        for (int index = 0; index < total.length; index++) total[index] += more[index];
        assertArrayEquals(PARTS, total);
        assertEquals(0, due.remaining());
    }

    @Test
    public void overpaymentOnlyAllocatesTheRemainingDue() {
        FinanceRules.Due due = due(1, "2026-09");
        List<FinanceRules.Application> applications = FinanceRules.allocate(10, 450, Arrays.asList(due));
        assertEquals(300, FinanceRules.totalAmount(applications));
        assertEquals(0, due.remaining());
    }

    @Test
    public void zeroOrNegativeAmountAllocatesNothing() {
        FinanceRules.Due due = due(1, "2026-09");
        assertTrue(FinanceRules.allocate(10, 0, Arrays.asList(due)).isEmpty());
        assertTrue(FinanceRules.allocate(10, -50, Arrays.asList(due)).isEmpty());
        assertEquals(300, due.remaining());
    }

    @Test
    public void advanceCoversFollowingMonthsUpToCurrentMonth() {
        List<FinanceRules.Due> dues = new ArrayList<>(Arrays.asList(due(1, "2026-10"), due(2, "2026-11"), due(3, "2026-12")));
        List<FinanceRules.Advance> advances = new ArrayList<>(Arrays.asList(new FinanceRules.Advance(7, "2026-09", 450)));
        List<FinanceRules.Application> applications = FinanceRules.applyAdvancesToFollowingMonths(advances, dues, "2026-11");
        assertEquals(450, FinanceRules.totalAmount(applications));
        assertEquals(0, dues.get(0).remaining());
        assertEquals(150, dues.get(1).remaining());
        assertEquals(300, dues.get(2).remaining()); // December is in the future: untouched
        assertEquals(0, advances.get(0).balance);
    }

    @Test
    public void advanceNeverCoversItsOwnOrEarlierMonths() {
        List<FinanceRules.Due> dues = new ArrayList<>(Arrays.asList(due(1, "2026-08"), due(2, "2026-09")));
        List<FinanceRules.Advance> advances = new ArrayList<>(Arrays.asList(new FinanceRules.Advance(7, "2026-09", 300)));
        assertTrue(FinanceRules.applyAdvancesToFollowingMonths(advances, dues, "2026-12").isEmpty());
        assertEquals(300, advances.get(0).balance);
    }

    @Test
    public void advanceWithUnknownMonthIsLeftAlone() {
        List<FinanceRules.Due> dues = new ArrayList<>(Arrays.asList(due(1, "2026-10")));
        List<FinanceRules.Advance> advances = new ArrayList<>(Arrays.asList(new FinanceRules.Advance(7, null, 300)));
        assertTrue(FinanceRules.applyAdvancesToFollowingMonths(advances, dues, "2026-12").isEmpty());
    }

    @Test
    public void oldestAdvanceIsConsumedFirst() {
        List<FinanceRules.Due> dues = new ArrayList<>(Arrays.asList(due(1, "2026-10")));
        List<FinanceRules.Advance> advances = new ArrayList<>(Arrays.asList(
                new FinanceRules.Advance(7, "2026-09", 200), new FinanceRules.Advance(8, "2026-09", 200)));
        FinanceRules.applyAdvancesToFollowingMonths(advances, dues, "2026-10");
        assertEquals(0, advances.get(0).balance);
        assertEquals(100, advances.get(1).balance);
    }

    @Test
    public void existingAdvancesCoverTheDeclaredMonthWhenAPaymentIsRecorded() {
        List<FinanceRules.Due> dues = new ArrayList<>(Arrays.asList(due(1, "2026-10")));
        List<FinanceRules.Advance> advances = new ArrayList<>(Arrays.asList(new FinanceRules.Advance(7, "2026-09", 120)));
        List<FinanceRules.Application> applications = FinanceRules.applyAdvances(advances, dues);
        assertEquals(120, FinanceRules.totalAmount(applications));
        assertEquals(180, dues.get(0).remaining());
        assertEquals(0, advances.get(0).balance);
    }

    @Test
    public void settlementMustPayTheWholeRemainingAmount() {
        assertTrue(FinanceRules.canRecordSettlement(1000, 1000, 0));
        assertFalse(FinanceRules.canRecordSettlement(500, 1000, 0));
        assertFalse(FinanceRules.canRecordSettlement(0, 0, 0));
    }

    @Test
    public void settlementComplementIsAllowedAfterALatePayment() {
        // September paid 1000 to the teacher, then a late student payment adds 100.
        assertTrue(FinanceRules.canRecordSettlement(100, 1100, 1000));
        assertFalse(FinanceRules.canRecordSettlement(1100, 1100, 1000));
        assertFalse(FinanceRules.canRecordSettlement(100, 1000, 1000));
    }
}
