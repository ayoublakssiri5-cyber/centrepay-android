# Changelog

## 1.1.0 — audit-driven fixes

Financial correctness
- Advances now automatically cover the following unpaid months up to the current month (previously they were only used when a new payment was entered).
- Students enrolled before a teacher was assigned to their formula are linked to that teacher (only when no teacher was set; amounts unchanged).
- A salary complement can be paid when student payments for an already-settled month arrive later.
- Payment preview runs the real computation in a rolled-back transaction, so it always matches the saved result.
- Payments for a month before enrolment are refused instead of silently becoming an advance.
- Dates are stored and parsed independently of the phone language.

Reliability and performance
- A failed ledger write rolls back the whole payment; tariff updates are transactional.
- The payment screen no longer writes to the database on every keystroke.
- Indexes added without changing the schema version; dead code removed.

Design
- Single colour palette shared by all screens, WCAG AA contrast, minimum text size 11 sp, single-line student rows.

Quality
- 40 unit tests (JUnit + Robolectric); financial rules extracted into pure Java classes.

## 1.0.0

- First version: students, tariffs, teachers, monthly dues, payments with advances, payroll, monthly report.
