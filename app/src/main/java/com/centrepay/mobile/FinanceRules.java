package com.centrepay.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Financial rules without any Android dependency, so they can be unit-tested on the JVM.
 * Component order everywhere: math, french, pc, svt, center.
 */
final class FinanceRules {
    static final int COMPONENTS = 5;

    private FinanceRules() { }

    /** Proportional split (largest remainder). The result always sums to amount unless every part is zero. */
    static int[] splitAmount(int amount, int[] parts) {
        int total = 0;
        for (int part : parts) total += part;
        int[] result = new int[parts.length];
        long[] remainders = new long[parts.length];
        int assigned = 0;
        if (total == 0) return result;
        for (int index = 0; index < parts.length; index++) {
            long product = (long) amount * parts[index];
            result[index] = (int) (product / total);
            remainders[index] = product % total;
            assigned += result[index];
        }
        while (assigned < amount) {
            int best = 0;
            for (int index = 1; index < remainders.length; index++) {
                if (remainders[index] > remainders[best]) best = index;
            }
            result[best]++;
            remainders[best] = -1;
            assigned++;
        }
        return result;
    }

    /** One monthly due with what has already been applied to it. */
    static final class Due {
        final long id;
        final String month;
        final int total;
        final int[] componentTotals;
        int paid;
        final int[] paidComponents;

        Due(long id, String month, int total, int[] componentTotals, int paid, int[] paidComponents) {
            this.id = id;
            this.month = month;
            this.total = total;
            this.componentTotals = componentTotals.clone();
            this.paid = paid;
            this.paidComponents = paidComponents.clone();
        }

        int remaining() { return Math.max(0, total - paid); }

        int[] remainingComponents() {
            int[] result = new int[componentTotals.length];
            for (int index = 0; index < result.length; index++) {
                result[index] = Math.max(0, componentTotals[index] - paidComponents[index]);
            }
            return result;
        }

        void apply(int amount, int[] components) {
            paid += amount;
            for (int index = 0; index < paidComponents.length; index++) paidComponents[index] += components[index];
        }
    }

    /** One row to insert into payment_applications. */
    static final class Application {
        final long paymentId;
        final long dueId;
        final int amount;
        final int[] split;

        Application(long paymentId, long dueId, int amount, int[] split) {
            this.paymentId = paymentId;
            this.dueId = dueId;
            this.amount = amount;
            this.split = split;
        }
    }

    /** A payment surplus still available as an advance. sourceMonth is YYYY-MM, or null when unknown. */
    static final class Advance {
        final long paymentId;
        final String sourceMonth;
        int balance;

        Advance(long paymentId, String sourceMonth, int balance) {
            this.paymentId = paymentId;
            this.sourceMonth = sourceMonth;
            this.balance = balance;
        }
    }

    /** Applies up to available to the dues, in list order. Updates the dues and returns the applications. */
    static List<Application> allocate(long paymentId, int available, List<Due> dues) {
        List<Application> applications = new ArrayList<>();
        int remaining = Math.max(0, available);
        for (Due due : dues) {
            if (remaining == 0) break;
            int applied = Math.min(remaining, due.remaining());
            if (applied <= 0) continue;
            int[] split = splitAmount(applied, due.remainingComponents());
            due.apply(applied, split);
            applications.add(new Application(paymentId, due.id, applied, split));
            remaining -= applied;
        }
        return applications;
    }

    static int totalAmount(List<Application> applications) {
        int total = 0;
        for (Application application : applications) total += application.amount;
        return total;
    }

    static int[] totalSplit(List<Application> applications) {
        int[] total = new int[COMPONENTS];
        for (Application application : applications) {
            for (int index = 0; index < total.length; index++) total[index] += application.split[index];
        }
        return total;
    }

    /**
     * Existing advances (oldest first) applied to the dues of one month: the rule used when a payment is recorded
     * for that month. Advance balances are updated.
     */
    static List<Application> applyAdvances(List<Advance> advances, List<Due> dues) {
        List<Application> applications = new ArrayList<>();
        for (Advance advance : advances) {
            if (advance.balance <= 0) continue;
            List<Application> used = allocate(advance.paymentId, advance.balance, dues);
            advance.balance -= totalAmount(used);
            applications.addAll(used);
        }
        return applications;
    }

    /**
     * Automatic use of advances: an advance covers the unpaid dues of the months strictly after the month of the
     * payment that created it, up to currentMonth included, oldest month first. Advances are consumed oldest first.
     * Dues must be sorted by month. Advance balances and dues are updated.
     */
    static List<Application> applyAdvancesToFollowingMonths(List<Advance> advances, List<Due> dues, String currentMonth) {
        List<Application> applications = new ArrayList<>();
        for (Advance advance : advances) {
            if (advance.balance <= 0 || advance.sourceMonth == null) continue;
            for (Due due : dues) {
                if (advance.balance <= 0) break;
                if (due.month.compareTo(advance.sourceMonth) <= 0 || due.month.compareTo(currentMonth) > 0) continue;
                List<Application> used = allocate(advance.paymentId, advance.balance, Collections.singletonList(due));
                advance.balance -= totalAmount(used);
                applications.addAll(used);
            }
        }
        return applications;
    }

    /** A teacher's month is paid in one go for the whole remaining amount; a complement is allowed when money arrives later. */
    static boolean canRecordSettlement(int amount, int earnedAmount, int alreadyPaid) {
        int remaining = Math.max(0, earnedAmount - alreadyPaid);
        return amount > 0 && amount == remaining;
    }
}
