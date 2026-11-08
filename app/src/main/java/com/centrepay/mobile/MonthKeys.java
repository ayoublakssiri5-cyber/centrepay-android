package com.centrepay.mobile;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date and month parsing independent of the phone language. Months are stored as YYYY-MM.
 * Accepts Arabic-Indic digits so that values typed under an Arabic locale stay readable.
 */
final class MonthKeys {
    private static final Pattern DATE = Pattern.compile("^(\\d{1,2})/(\\d{1,2})/(\\d{4})");
    private static final Pattern REFERENCE = Pattern.compile("^(\\d{1,2})/(\\d{4})");
    private static final Pattern STORED = Pattern.compile("^(\\d{4})-(0[1-9]|1[0-2])$");

    private MonthKeys() { }

    static String normalizeDigits(String value) {
        if (value == null) return null;
        StringBuilder builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (c >= '٠' && c <= '٩') builder.append((char) ('0' + (c - '٠')));
            else if (c >= '۰' && c <= '۹') builder.append((char) ('0' + (c - '۰')));
            else builder.append(c);
        }
        return builder.toString().trim();
    }

    static String key(int year, int month) {
        return String.format(Locale.US, "%04d-%02d", year, month);
    }

    /** "dd/MM/yyyy" to YYYY-MM, or null when the date does not exist. */
    static String monthOfDate(String date) {
        String value = normalizeDigits(date);
        if (value == null) return null;
        Matcher matcher = DATE.matcher(value);
        if (!matcher.find()) return null;
        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int year = Integer.parseInt(matcher.group(3));
        if (month < 1 || month > 12 || day < 1 || day > daysInMonth(year, month)) return null;
        return key(year, month);
    }

    /** "MM/yyyy" to YYYY-MM, or null. */
    static String monthOfReference(String reference) {
        String value = normalizeDigits(reference);
        if (value == null) return null;
        Matcher matcher = REFERENCE.matcher(value);
        if (!matcher.find()) return null;
        int month = Integer.parseInt(matcher.group(1));
        int year = Integer.parseInt(matcher.group(2));
        if (month < 1 || month > 12) return null;
        return key(year, month);
    }

    /** YYYY-MM or MM/YYYY to YYYY-MM, or null. */
    static String stored(String value) {
        String month = normalizeDigits(value);
        if (month == null) return null;
        if (STORED.matcher(month).matches()) return month;
        if (month.matches("(0[1-9]|1[0-2])/\\d{4}")) return month.substring(3) + "-" + month.substring(0, 2);
        return null;
    }

    /** YYYY-MM to the MM/YYYY form used by older records. */
    static String legacy(String storedMonth) {
        return storedMonth.substring(5) + "/" + storedMonth.substring(0, 4);
    }

    static int year(String storedMonth) { return Integer.parseInt(storedMonth.substring(0, 4)); }

    static int month(String storedMonth) { return Integer.parseInt(storedMonth.substring(5, 7)); }

    static int daysInMonth(int year, int month) {
        switch (month) {
            case 2: return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0 ? 29 : 28;
            case 4: case 6: case 9: case 11: return 30;
            default: return 31;
        }
    }
}
