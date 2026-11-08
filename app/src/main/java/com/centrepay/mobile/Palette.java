package com.centrepay.mobile;

/**
 * Single colour palette shared by every screen (previously each Activity declared its own shades).
 * Text colours reach at least 4.5:1 contrast on SURFACE and on their *_SOFT background (WCAG AA), see PaletteTest.
 */
final class Palette {
    // Brand
    static final int PRIMARY = 0xff0f766e;       // buttons, active tabs, links
    static final int PRIMARY_DARK = 0xff0b5a5c;  // headers, status bar
    static final int PRIMARY_SOFT = 0xffe6f4f2;  // selected chips, info cards
    static final int ACCENT = 0xffd9a83e;        // "Pay" of the logo, decorative only
    static final int ACCENT_SOFT = 0xfffff3db;
    static final int ACCENT_TEXT = 0xff8a6500;   // accent used as text

    // Neutrals
    static final int BACKGROUND = 0xfff6fafb;
    static final int SURFACE = 0xffffffff;
    static final int BORDER = 0xffdfe7ea;
    static final int TEXT = 0xff0f172a;
    static final int MUTED = 0xff5b6b80;

    // Status (text colour + soft background)
    static final int SUCCESS = 0xff15803d;
    static final int SUCCESS_SOFT = 0xffdcfce7;
    static final int WARNING = 0xffb45309;
    static final int WARNING_SOFT = 0xfffef3c7;
    static final int ERROR = 0xffb91c1c;
    static final int ERROR_SOFT = 0xfffee2e2;
    static final int INFO = 0xff0e7490;
    static final int INFO_SOFT = 0xffe0f2fe;
    static final int BLUE = 0xff1d4ed8;
    static final int BLUE_SOFT = 0xffdbeafe;

    private Palette() { }

    /** WCAG contrast ratio between two opaque colours. */
    static double contrast(int first, int second) {
        double a = luminance(first), b = luminance(second);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(int color) {
        return 0.2126 * channel((color >> 16) & 0xff) + 0.7152 * channel((color >> 8) & 0xff) + 0.0722 * channel(color & 0xff);
    }

    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
