package com.centrepay.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MonthKeysTest {
    @Test
    public void dateGivesItsMonth() {
        assertEquals("2026-09", MonthKeys.monthOfDate("05/09/2026"));
        assertEquals("2026-09", MonthKeys.monthOfDate(" 5/9/2026 "));
    }

    @Test
    public void arabicIndicDigitsAreUnderstood() {
        assertEquals("2026-09", MonthKeys.monthOfDate("٠٥/٠٩/٢٠٢٦"));
        assertEquals("2026-10", MonthKeys.monthOfReference("۱۰/۲۰۲۶"));
    }

    @Test
    public void impossibleDatesAreRejected() {
        assertNull(MonthKeys.monthOfDate("31/02/2026"));
        assertNull(MonthKeys.monthOfDate("29/02/2026"));
        assertEquals("2028-02", MonthKeys.monthOfDate("29/02/2028"));
        assertNull(MonthKeys.monthOfDate("00/01/2026"));
        assertNull(MonthKeys.monthOfDate("10/13/2026"));
        assertNull(MonthKeys.monthOfDate(""));
        assertNull(MonthKeys.monthOfDate(null));
        assertNull(MonthKeys.monthOfDate("2026-09-05"));
    }

    @Test
    public void referenceMonth() {
        assertEquals("2026-10", MonthKeys.monthOfReference("10/2026"));
        assertNull(MonthKeys.monthOfReference("13/2026"));
        assertNull(MonthKeys.monthOfReference("2026-10"));
    }

    @Test
    public void storedAndLegacyKeys() {
        assertEquals("2026-10", MonthKeys.stored("2026-10"));
        assertEquals("2026-10", MonthKeys.stored("10/2026"));
        assertNull(MonthKeys.stored("2026-13"));
        assertEquals("10/2026", MonthKeys.legacy("2026-10"));
        assertEquals(2026, MonthKeys.year("2026-10"));
        assertEquals(10, MonthKeys.month("2026-10"));
    }
}
