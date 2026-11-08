# Testing

## How to run

```bash
./gradlew testDebugUnitTest          # Windows: gradlew.bat testDebugUnitTest
```

Use JDK 17 or 21 (for example the JDK bundled with Android Studio). Reports are written to `app/build/reports/tests/testDebugUnitTest/index.html`.

## Last verified run

- Date: 2026-11-08, Windows 11, JDK 21 (Android Studio JBR), Gradle 8.13
- Command: `gradlew --no-daemon clean testDebugUnitTest assembleDebug`
- Result: **BUILD SUCCESSFUL — 40 tests, 0 failures, 0 errors, 0 skipped**

| Suite | Tests | Failures |
|---|---|---|
| `FinanceRulesTest` | 13 | 0 |
| `MonthKeysTest` | 5 | 0 |
| `PaletteTest` | 4 | 0 |
| `StudentDatabaseHelperTest` | 18 | 0 |

## What is covered

- Exact, partial and over-payments; money conservation of the split for every amount from 0 to 1000.
- Advances: following months only, never the source month, oldest advance consumed first, future months untouched.
- Teacher assigned after enrolment; existing teacher never replaced.
- Tariff change does not alter existing dues.
- Salary settlement, refused partial settlement, complement after a late payment.
- Payment for a month before enrolment refused; invalid amounts and months refused.
- Preview equals the recorded payment and leaves nothing in the database.
- Forced SQL failure (trigger) rolls back the whole payment.
- Database upgrade from version 13 to 14 keeps every payment and rebuilds allocations.
- Colour contrast of the palette (WCAG AA).

## Not covered

- UI rendering and navigation (no instrumented / Espresso tests yet).
- Installation over an existing version on a device.
