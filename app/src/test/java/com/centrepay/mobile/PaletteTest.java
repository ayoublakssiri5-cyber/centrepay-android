package com.centrepay.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Readability of the shared palette (WCAG AA: 4.5:1 for normal text). */
public class PaletteTest {
    private static void readable(String name, int text, int background) {
        double ratio = Palette.contrast(text, background);
        assertTrue(name + " contrast " + String.format(java.util.Locale.US, "%.2f", ratio), ratio >= 4.5);
    }

    @Test
    public void textColoursAreReadableOnSurfaceAndBackground() {
        int[] texts = {Palette.TEXT, Palette.MUTED, Palette.PRIMARY, Palette.PRIMARY_DARK, Palette.ACCENT_TEXT,
                Palette.SUCCESS, Palette.WARNING, Palette.ERROR, Palette.INFO, Palette.BLUE};
        for (int text : texts) {
            readable(Integer.toHexString(text) + " on surface", text, Palette.SURFACE);
            readable(Integer.toHexString(text) + " on background", text, Palette.BACKGROUND);
        }
    }

    @Test
    public void statusColoursAreReadableOnTheirSoftBackground() {
        readable("primary", Palette.PRIMARY, Palette.PRIMARY_SOFT);
        readable("primary dark", Palette.PRIMARY_DARK, Palette.PRIMARY_SOFT);
        readable("success", Palette.SUCCESS, Palette.SUCCESS_SOFT);
        readable("warning", Palette.WARNING, Palette.WARNING_SOFT);
        readable("error", Palette.ERROR, Palette.ERROR_SOFT);
        readable("info", Palette.INFO, Palette.INFO_SOFT);
        readable("blue", Palette.BLUE, Palette.BLUE_SOFT);
        readable("accent", Palette.ACCENT_TEXT, Palette.ACCENT_SOFT);
    }

    @Test
    public void whiteTextIsReadableOnHeadersAndButtons() {
        readable("white on primary", Palette.SURFACE, Palette.PRIMARY);
        readable("white on primary dark", Palette.SURFACE, Palette.PRIMARY_DARK);
        readable("white on success", Palette.SURFACE, Palette.SUCCESS);
        readable("white on error", Palette.SURFACE, Palette.ERROR);
    }

    @Test
    public void formerReportColoursWereNotReadable() {
        // Values used before by MonthlyReportActivity as text colours.
        assertTrue(Palette.contrast(0xff22c55e, Palette.SURFACE) < 4.5);
        assertTrue(Palette.contrast(0xfff59e0b, Palette.SURFACE) < 4.5);
    }
}
