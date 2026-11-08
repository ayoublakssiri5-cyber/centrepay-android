package com.centrepay.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Scenarios on a real SQLite database (Robolectric). Months are relative to today because dues follow the clock. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class StudentDatabaseHelperTest {
    private StudentDatabaseHelper db;

    @Before
    public void setUp() {
        RuntimeEnvironment.getApplication().deleteDatabase("centrepay.db");
        db = new StudentDatabaseHelper(RuntimeEnvironment.getApplication());
    }

    @After
    public void tearDown() {
        db.close();
    }

    /** First day of the month, monthsFromNow months from the current month, as dd/MM/yyyy. */
    private static String firstDay(int monthsFromNow) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, monthsFromNow);
        return String.format(Locale.US, "01/%02d/%04d", c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    /** MM/yyyy of the month monthsFromNow months from the current month. */
    private static String reference(int monthsFromNow) {
        return firstDay(monthsFromNow).substring(3);
    }

    private static String stored(int monthsFromNow) {
        String ref = reference(monthsFromNow);
        return ref.substring(3) + "-" + ref.substring(0, 2);
    }

    /** 1AC Math + PC: 300 DH = Math 120 + PC 100 + Centre 80. */
    private long addStudent(String enrollment, long mathTeacher, long pcTeacher) {
        return db.addStudent("Test", "Eleve", "0600000000", "0611111111", "1AC",
                TariffOption.packKey(true, false, true, false), enrollment, "Math, PC",
                300, 120, 0, 100, 0, 80, mathTeacher, 0, pcTeacher, 0);
    }

    private int count(String sql, String... args) {
        Cursor cursor = db.getReadableDatabase().rawQuery(sql, args);
        int value = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return value;
    }

    private StudentMonthBalance balance(long studentId, int monthsFromNow) {
        return db.getMonthBalances(stored(monthsFromNow)).get(studentId);
    }

    @Test
    public void exactPayment() {
        long student = addStudent(firstDay(0), 0, 0);
        PaymentResult result = db.recordPayment(student, 300, firstDay(0), reference(0), "");
        assertNotNull(result);
        assertEquals(300, result.allocatedFromPayment);
        assertEquals(0, result.advanceCreated);
        assertEquals(0, balance(student, 0).remainingAmount());
    }

    @Test
    public void partialPayment() {
        long student = addStudent(firstDay(0), 0, 0);
        db.recordPayment(student, 100, firstDay(0), reference(0), "");
        assertEquals(200, balance(student, 0).remainingAmount());
        db.recordPayment(student, 200, firstDay(0), reference(0), "");
        assertEquals(0, balance(student, 0).remainingAmount());
        // The split of both payments adds up exactly to the tariff.
        assertEquals(120, count("SELECT SUM(math_amount) FROM payment_applications"));
        assertEquals(100, count("SELECT SUM(pc_amount) FROM payment_applications"));
        assertEquals(80, count("SELECT SUM(center_amount) FROM payment_applications"));
    }

    @Test
    public void overpaymentBecomesAnAdvance() {
        long student = addStudent(firstDay(0), 0, 0);
        PaymentResult result = db.recordPayment(student, 450, firstDay(0), reference(0), "");
        assertEquals(300, result.allocatedFromPayment);
        assertEquals(150, result.advanceCreated);
        assertEquals(150, db.getFinancialSummary(student).availableAdvance);
    }

    @Test
    public void advanceIsUsedForTheNextMonthWithoutANewPayment() { // FIN-01
        long student = addStudent(firstDay(-1), 0, 0);
        db.recordPayment(student, 600, firstDay(-1), reference(-1), "");
        db.refreshMonthlyDuesAndAdvances();
        assertEquals(0, balance(student, 0).remainingAmount());
        assertEquals(0, db.getFinancialSummary(student).availableAdvance);
        assertEquals(0, db.getFinancialSummary(student).outstandingAmount);
    }

    @Test
    public void advanceDoesNotCoverAFutureMonthBeforeItStarts() { // FIN-01
        long student = addStudent(firstDay(0), 0, 0);
        db.recordPayment(student, 600, firstDay(0), reference(0), "");
        db.refreshMonthlyDuesAndAdvances();
        assertEquals(300, db.getFinancialSummary(student).availableAdvance);
    }

    @Test
    public void monthlyReportCountsTheAdvanceAsPaid() { // FIN-01
        long student = addStudent(firstDay(-1), 0, 0);
        db.recordPayment(student, 600, firstDay(-1), reference(-1), "");
        MonthlyReport report = db.getMonthlyReport(stored(0));
        assertEquals(300, report.receivedAmount);
        assertTrue(report.unpaidStudents.isEmpty());
    }

    @Test
    public void severalTeachersAndSubjects() {
        long math = db.addTeacher("Prof Math", "0622222222", "1AC", "Mathématiques");
        long pc = db.addTeacher("Prof PC", "0633333333", "1AC", "Physique-Chimie");
        long student = addStudent(firstDay(0), math, pc);
        db.recordPayment(student, 300, firstDay(0), reference(0), "");
        assertEquals(120, db.getTeacherBalance(math, stored(0)).earnedAmount);
        assertEquals(100, db.getTeacherBalance(pc, stored(0)).earnedAmount);
    }

    @Test
    public void teacherAssignedAfterEnrollmentReceivesTheShare() { // FIN-02
        long tariff = db.addTariffOption("1AC", TariffOption.packKey(true, false, true, false), 300, 120, 0, 100, 0, 80);
        long student = addStudent(firstDay(0), 0, 0);
        db.recordPayment(student, 300, firstDay(0), reference(0), "");
        long math = db.addTeacher("Prof Math", "0622222222", "1AC", "Mathématiques");
        db.saveTariffTeacherAssignment(tariff, "math", math);
        assertEquals(math, db.getStudent(student).mathTeacherId);
        assertEquals(120, db.getTeacherBalance(math, stored(0)).earnedAmount);
        assertEquals(0, db.getStudent(student).pcTeacherId); // other subjects untouched
    }

    @Test
    public void existingTeacherIsNeverReplaced() { // FIN-02
        long tariff = db.addTariffOption("1AC", TariffOption.packKey(true, false, true, false), 300, 120, 0, 100, 0, 80);
        long first = db.addTeacher("Prof A", "0622222222", "1AC", "Mathématiques");
        long second = db.addTeacher("Prof B", "0644444444", "1AC", "Mathématiques");
        long student = addStudent(firstDay(0), first, 0);
        db.saveTariffTeacherAssignment(tariff, "math", second);
        assertEquals(first, db.getStudent(student).mathTeacherId);
    }

    @Test
    public void tariffChangeDoesNotAlterExistingDues() {
        long tariff = db.addTariffOption("1AC", TariffOption.packKey(true, false, true, false), 300, 120, 0, 100, 0, 80);
        long student = addStudent(firstDay(0), 0, 0);
        db.refreshMonthlyDuesAndAdvances();
        assertTrue(db.updateTariffOption(tariff, "1AC", TariffOption.packKey(true, false, true, false), 350, 140, 0, 120, 0, 90));
        assertEquals(300, balance(student, 0).totalAmount);
    }

    @Test
    public void salarySettlementAndLateComplement() { // FIN-03
        long math = db.addTeacher("Prof Math", "0622222222", "1AC", "Mathématiques");
        long a = addStudent(firstDay(0), math, 0);
        long b = addStudent(firstDay(0), math, 0);
        db.recordPayment(a, 300, firstDay(0), reference(0), "");
        assertTrue(db.recordTeacherSettlement(math, 120, firstDay(0), stored(0), "") > 0);
        assertEquals(-1, db.recordTeacherSettlement(math, 120, firstDay(0), stored(0), "")); // nothing left
        db.recordPayment(b, 300, firstDay(0), reference(0), ""); // late payment for the same month
        TeacherBalance balance = db.getTeacherBalance(math, stored(0));
        assertEquals(120, balance.remainingAmount);
        assertEquals(-1, db.recordTeacherSettlement(math, 50, firstDay(0), stored(0), "")); // partial refused
        assertTrue(db.recordTeacherSettlement(math, 120, firstDay(0), stored(0), "") > 0);
        assertEquals(0, db.getTeacherBalance(math, stored(0)).remainingAmount);
    }

    @Test
    public void paymentBeforeEnrollmentIsRefused() { // FIN-07
        long student = addStudent(firstDay(0), 0, 0);
        assertNull(db.recordPayment(student, 300, firstDay(0), reference(-1), ""));
        assertEquals(PaymentPreview.BEFORE_ENROLLMENT, db.previewPayment(student, 300, firstDay(0), reference(-1)).status);
        assertEquals(0, count("SELECT COUNT(*) FROM payments"));
    }

    @Test
    public void previewMatchesTheRecordedPaymentAndKeepsNothing() { // FIN-06
        long student = addStudent(firstDay(-1), 0, 0);
        db.recordPayment(student, 450, firstDay(-1), reference(-1), ""); // advance 150
        PaymentPreview preview = db.previewPayment(student, 300, firstDay(0), reference(0));
        int paymentsBefore = count("SELECT COUNT(*) FROM payments");
        int applicationsBefore = count("SELECT COUNT(*) FROM payment_applications");
        PaymentResult result = db.recordPayment(student, 300, firstDay(0), reference(0), "");
        assertEquals(PaymentPreview.OK, preview.status);
        assertEquals(result.allocatedFromPayment, preview.allocated);
        assertEquals(result.advanceCreated, preview.advanceCreated);
        assertEquals(result.advanceAppliedBeforePayment, preview.advanceApplied);
        assertEquals(paymentsBefore + 1, count("SELECT COUNT(*) FROM payments"));
        assertTrue(count("SELECT COUNT(*) FROM payment_applications") > applicationsBefore);
    }

    @Test
    public void failedApplicationRollsBackThePayment() { // DB-01
        long student = addStudent(firstDay(0), 0, 0);
        db.refreshMonthlyDuesAndAdvances();
        db.getWritableDatabase().execSQL("CREATE TRIGGER fail_applications BEFORE INSERT ON payment_applications BEGIN SELECT RAISE(ABORT, 'test'); END");
        assertNull(db.recordPayment(student, 300, firstDay(0), reference(0), ""));
        assertEquals(0, count("SELECT COUNT(*) FROM payments"));
        assertEquals(0, count("SELECT COUNT(*) FROM payment_applications"));
    }

    @Test
    public void invalidAmountsAreRefused() {
        long student = addStudent(firstDay(0), 0, 0);
        assertNull(db.recordPayment(student, 0, firstDay(0), reference(0), ""));
        assertNull(db.recordPayment(student, -10, firstDay(0), reference(0), ""));
        assertNull(db.recordPayment(student, 100, firstDay(0), "13/2026", ""));
    }

    @Test
    public void upgradeFromVersion13KeepsPaymentsAndRebuildsAllocations() {
        long student = addStudent(firstDay(-1), 0, 0);
        db.recordPayment(student, 300, firstDay(-1), reference(-1), "");
        db.recordPayment(student, 150, firstDay(0), reference(0), "");
        SQLiteDatabase raw = db.getWritableDatabase();
        raw.setVersion(13);
        db.close();
        db = new StudentDatabaseHelper(RuntimeEnvironment.getApplication());
        assertEquals(14, db.getReadableDatabase().getVersion());
        assertEquals(2, count("SELECT COUNT(*) FROM payments"));
        assertEquals(450, count("SELECT SUM(amount) FROM payment_applications"));
        assertEquals(1, count("SELECT COUNT(*) FROM students"));
    }

    @Test
    public void indexesAreCreatedWithoutChangingTheSchemaVersion() { // DB-05
        SQLiteDatabase raw = db.getWritableDatabase();
        assertEquals(14, raw.getVersion());
        assertEquals(5, count("SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name LIKE 'idx_%'"));
    }

    @Test
    public void dashboardTotalsStayConsistent() {
        long student = addStudent(firstDay(-1), 0, 0);
        db.recordPayment(student, 400, firstDay(-1), reference(-1), "");
        DashboardSummary summary = db.getDashboardSummary();
        Map<Long, StudentMonthBalance> current = db.getCurrentMonthBalances();
        assertEquals(400, summary.receivedAmount);
        assertEquals(100, current.get(student).paidAmount); // advance used for the current month
        assertEquals(0, summary.advanceAmount);
    }
}
