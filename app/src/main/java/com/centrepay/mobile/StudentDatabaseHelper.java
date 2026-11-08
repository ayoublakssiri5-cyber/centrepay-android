package com.centrepay.mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "centrepay.db";
    private static final int DATABASE_VERSION = 14;

    public StudentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "last_name TEXT NOT NULL, first_name TEXT NOT NULL, phone TEXT, " +
                "parent_phone TEXT, level TEXT NOT NULL, pack_key TEXT NOT NULL DEFAULT '', enrollment_date TEXT NOT NULL, " +
                "subjects TEXT NOT NULL, negotiated_amount INTEGER NOT NULL DEFAULT 0, " +
                "math_amount INTEGER NOT NULL DEFAULT 0, french_amount INTEGER NOT NULL DEFAULT 0, " +
                "pc_amount INTEGER NOT NULL DEFAULT 0, svt_amount INTEGER NOT NULL DEFAULT 0, " +
                "center_amount INTEGER NOT NULL DEFAULT 0, math_teacher_id INTEGER NOT NULL DEFAULT 0, " +
                "french_teacher_id INTEGER NOT NULL DEFAULT 0, pc_teacher_id INTEGER NOT NULL DEFAULT 0, " +
                "svt_teacher_id INTEGER NOT NULL DEFAULT 0)");
        createTariffOptionsTable(db);
        createTeachersTable(db);
        createTariffTeacherAssignmentsTable(db);
        createPaymentTables(db);
        createTeacherSettlementsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createTariffRatesTable(db);
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE students ADD COLUMN negotiated_amount INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN center_amount INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion < 4) {
            createTariffOptionsTable(db);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE students ADD COLUMN math_amount INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN french_amount INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN pc_amount INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN svt_amount INTEGER NOT NULL DEFAULT 0");
            upgradeTariffOptionsToDistributedRates(db);
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE students ADD COLUMN pack_key TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE tariff_options ADD COLUMN pack_key TEXT NOT NULL DEFAULT ''");
            populateTariffPackKeys(db);
        }
        if (oldVersion < 7) {
            createTeachersTable(db);
        }
        if (oldVersion >= 7 && oldVersion < 8) {
            db.execSQL("ALTER TABLE teachers ADD COLUMN level TEXT NOT NULL DEFAULT ''");
        }
        if (oldVersion < 9) {
            createTariffTeacherAssignmentsTable(db);
        }
        if (oldVersion < 10) {
            createPaymentTables(db);
        }
        if (oldVersion < 11) {
            db.execSQL("ALTER TABLE students ADD COLUMN math_teacher_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN french_teacher_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN pc_teacher_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE students ADD COLUMN svt_teacher_id INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion >= 10 && oldVersion < 11) {
            db.execSQL("ALTER TABLE monthly_dues ADD COLUMN math_teacher_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE monthly_dues ADD COLUMN french_teacher_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE monthly_dues ADD COLUMN pc_teacher_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE monthly_dues ADD COLUMN svt_teacher_id INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion >= 10 && oldVersion < 12) {
            db.execSQL("ALTER TABLE payments ADD COLUMN reference_month TEXT NOT NULL DEFAULT ''");
        }
        if (oldVersion < 13) {
            createTeacherSettlementsTable(db);
        }
        if (oldVersion < 14) {
            rebuildPaymentApplicationsByReferenceMonth(db);
        }
    }

    /**
     * Indexes for the joins used by every balance and report. CREATE INDEX IF NOT EXISTS changes no data and keeps
     * the schema version, so an older APK can still open the database.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (db.isReadOnly()) return;
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_payment_applications_due ON payment_applications(due_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_payment_applications_payment ON payment_applications(payment_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_payments_student ON payments(student_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_monthly_dues_month ON monthly_dues(due_month)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_teacher_settlements_teacher ON teacher_settlements(teacher_id, reference_month)");
    }

    private void createTariffRatesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tariff_rates (" +
                "level TEXT NOT NULL, subject TEXT NOT NULL, monthly_amount INTEGER NOT NULL, " +
                "updated_at TEXT NOT NULL, PRIMARY KEY (level, subject))");
    }

    private void createTariffOptionsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tariff_options (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, level TEXT NOT NULL, " +
                "pack_key TEXT NOT NULL, " +
                "student_amount INTEGER NOT NULL, math_amount INTEGER NOT NULL DEFAULT 0, " +
                "french_amount INTEGER NOT NULL DEFAULT 0, pc_amount INTEGER NOT NULL DEFAULT 0, " +
                "svt_amount INTEGER NOT NULL DEFAULT 0, center_amount INTEGER NOT NULL, " +
                "created_at TEXT NOT NULL)");
    }

    private void createTeachersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS teachers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, full_name TEXT NOT NULL, " +
                "phone TEXT, level TEXT NOT NULL, subjects TEXT NOT NULL, " +
                "active INTEGER NOT NULL DEFAULT 1)");
    }

    private void createTariffTeacherAssignmentsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tariff_teacher_assignments (" +
                "tariff_id INTEGER NOT NULL, subject_key TEXT NOT NULL, teacher_id INTEGER NOT NULL, " +
                "PRIMARY KEY (tariff_id, subject_key))");
    }

    private void createPaymentTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS monthly_dues (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, due_month TEXT NOT NULL, " +
                "total_amount INTEGER NOT NULL, math_amount INTEGER NOT NULL DEFAULT 0, " +
                "french_amount INTEGER NOT NULL DEFAULT 0, pc_amount INTEGER NOT NULL DEFAULT 0, " +
                "svt_amount INTEGER NOT NULL DEFAULT 0, center_amount INTEGER NOT NULL DEFAULT 0, " +
                "math_teacher_id INTEGER NOT NULL DEFAULT 0, french_teacher_id INTEGER NOT NULL DEFAULT 0, " +
                "pc_teacher_id INTEGER NOT NULL DEFAULT 0, svt_teacher_id INTEGER NOT NULL DEFAULT 0, " +
                "created_at TEXT NOT NULL, UNIQUE(student_id, due_month))");
        db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, amount INTEGER NOT NULL, " +
                "payment_date TEXT NOT NULL, reference_month TEXT NOT NULL, note TEXT, allocated_amount INTEGER NOT NULL DEFAULT 0, " +
                "advance_amount INTEGER NOT NULL DEFAULT 0, advance_balance INTEGER NOT NULL DEFAULT 0, " +
                "created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS payment_applications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, payment_id INTEGER NOT NULL, due_id INTEGER NOT NULL, " +
                "amount INTEGER NOT NULL, math_amount INTEGER NOT NULL DEFAULT 0, " +
                "french_amount INTEGER NOT NULL DEFAULT 0, pc_amount INTEGER NOT NULL DEFAULT 0, " +
                "svt_amount INTEGER NOT NULL DEFAULT 0, center_amount INTEGER NOT NULL DEFAULT 0, " +
                "source_kind TEXT NOT NULL, applied_at TEXT NOT NULL)");
    }

    private void createTeacherSettlementsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS teacher_settlements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, teacher_id INTEGER NOT NULL, amount INTEGER NOT NULL, " +
                "payment_date TEXT NOT NULL, reference_month TEXT NOT NULL, note TEXT, created_at TEXT NOT NULL)");
    }

    private void upgradeTariffOptionsToDistributedRates(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tariff_options_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, level TEXT NOT NULL, " +
                "student_amount INTEGER NOT NULL, math_amount INTEGER NOT NULL DEFAULT 0, " +
                "french_amount INTEGER NOT NULL DEFAULT 0, pc_amount INTEGER NOT NULL DEFAULT 0, " +
                "svt_amount INTEGER NOT NULL DEFAULT 0, center_amount INTEGER NOT NULL, " +
                "created_at TEXT NOT NULL)");
        db.execSQL("INSERT INTO tariff_options_new (id, level, student_amount, math_amount, french_amount, pc_amount, svt_amount, center_amount, created_at) " +
                "SELECT id, level, student_amount, 0, 0, 0, 0, center_amount, created_at FROM tariff_options");
        db.execSQL("DROP TABLE tariff_options");
        db.execSQL("ALTER TABLE tariff_options_new RENAME TO tariff_options");
    }

    private void populateTariffPackKeys(SQLiteDatabase db) {
        Cursor cursor = db.query("tariff_options", new String[]{"id", "math_amount", "french_amount", "pc_amount", "svt_amount"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String packKey = TariffOption.packKey(
                    cursor.getInt(cursor.getColumnIndexOrThrow("math_amount")) > 0,
                    cursor.getInt(cursor.getColumnIndexOrThrow("french_amount")) > 0,
                    cursor.getInt(cursor.getColumnIndexOrThrow("pc_amount")) > 0,
                    cursor.getInt(cursor.getColumnIndexOrThrow("svt_amount")) > 0);
            ContentValues values = new ContentValues();
            values.put("pack_key", packKey);
            db.update("tariff_options", values, "id = ?", new String[]{String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("id")))});
        }
        cursor.close();
    }

    public long addStudent(String lastName, String firstName, String phone, String parentPhone,
                           String level, String packKey, String enrollmentDate, String subjects,
                           int negotiatedAmount, int mathAmount, int frenchAmount, int pcAmount,
                           int svtAmount, int centerAmount, long mathTeacherId, long frenchTeacherId,
                           long pcTeacherId, long svtTeacherId) {
        ContentValues values = new ContentValues();
        values.put("last_name", lastName.trim());
        values.put("first_name", firstName.trim());
        values.put("phone", phone.trim());
        values.put("parent_phone", parentPhone.trim());
        values.put("level", level);
        values.put("pack_key", packKey);
        values.put("enrollment_date", enrollmentDate.trim());
        values.put("subjects", subjects);
        values.put("negotiated_amount", negotiatedAmount);
        values.put("math_amount", mathAmount);
        values.put("french_amount", frenchAmount);
        values.put("pc_amount", pcAmount);
        values.put("svt_amount", svtAmount);
        values.put("center_amount", centerAmount);
        values.put("math_teacher_id", mathTeacherId);
        values.put("french_teacher_id", frenchTeacherId);
        values.put("pc_teacher_id", pcTeacherId);
        values.put("svt_teacher_id", svtTeacherId);
        return getWritableDatabase().insert("students", null, values);
    }

    public List<Student> findStudents(String query) {
        return findStudents(query, "");
    }

    /** Returns the students matching the search text, optionally limited to one group/level. */
    public List<Student> findStudents(String query, String level) {
        List<Student> students = new ArrayList<>();
        String normalized = "%" + query.trim() + "%";
        boolean hasLevel = level != null && !level.trim().isEmpty();
        String selection = "(last_name LIKE ? OR first_name LIKE ? OR phone LIKE ?)" +
                (hasLevel ? " AND level = ?" : "");
        String[] selectionArgs = hasLevel
                ? new String[]{normalized, normalized, normalized, level.trim()}
                : new String[]{normalized, normalized, normalized};
        Cursor cursor = getReadableDatabase().query(
                "students",
                null,
                selection,
                selectionArgs,
                null, null, "last_name COLLATE NOCASE, first_name COLLATE NOCASE");

        while (cursor.moveToNext()) {
            students.add(new Student(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("parent_phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    cursor.getString(cursor.getColumnIndexOrThrow("pack_key")),
                    cursor.getString(cursor.getColumnIndexOrThrow("enrollment_date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("subjects")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("negotiated_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("math_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("french_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("pc_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("svt_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("center_amount")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("math_teacher_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("french_teacher_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("pc_teacher_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("svt_teacher_id"))));
        }
        cursor.close();
        return students;
    }

    /** Balances of the current month, loaded once for the student directory badges. */
    public Map<Long, StudentMonthBalance> getCurrentMonthBalances() {
        Calendar current = Calendar.getInstance();
        current.set(Calendar.DAY_OF_MONTH, 1);
        return getMonthBalances(monthKey(current));
    }

    /** Balances for a requested month (YYYY-MM or MM/YYYY). */
    public Map<Long, StudentMonthBalance> getMonthBalances(String requestedMonth) {
        Map<Long, StudentMonthBalance> balances = new HashMap<>();
        refreshMonthlyDuesAndAdvances();
        String month=requestedMonth;
        if(month!=null&&month.matches("(0[1-9]|1[0-2])/\\d{4}"))month=month.substring(3)+"-"+month.substring(0,2);
        if(month==null||!month.matches("\\d{4}-(0[1-9]|1[0-2])"))return balances;
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT d.student_id, d.due_month, d.total_amount, COALESCE(SUM(a.amount), 0) AS paid_amount " +
                        "FROM monthly_dues d LEFT JOIN payment_applications a ON a.due_id = d.id " +
                        "WHERE d.due_month = ? GROUP BY d.id",
                new String[]{month});
        while (cursor.moveToNext()) {
            balances.put(cursor.getLong(cursor.getColumnIndexOrThrow("student_id")), new StudentMonthBalance(
                    cursor.getString(cursor.getColumnIndexOrThrow("due_month")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_amount"))));
        }
        cursor.close();
        return balances;
    }

    public List<TariffOption> getTariffOptions(String level) {
        return getTariffOptions(level, null);
    }

    public List<TariffOption> getTariffOptions(String level, String packKey) {
        List<TariffOption> options = new ArrayList<>();
        String selection = packKey == null ? "level = ?" : "level = ? AND pack_key = ?";
        String[] selectionArgs = packKey == null ? new String[]{level} : new String[]{level, packKey};
        Cursor cursor = getReadableDatabase().query(
                "tariff_options",
                null,
                selection,
                selectionArgs,
                null, null, "student_amount DESC, id ASC");

        while (cursor.moveToNext()) {
            options.add(new TariffOption(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    cursor.getString(cursor.getColumnIndexOrThrow("pack_key")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("student_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("math_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("french_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("pc_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("svt_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("center_amount"))));
        }
        cursor.close();
        return options;
    }

    public long addTariffOption(String level, String packKey, int studentAmount, int mathAmount, int frenchAmount,
                                int pcAmount, int svtAmount, int centerAmount) {
        ContentValues values = new ContentValues();
        values.put("level", level);
        values.put("pack_key", packKey);
        values.put("student_amount", studentAmount);
        values.put("math_amount", mathAmount);
        values.put("french_amount", frenchAmount);
        values.put("pc_amount", pcAmount);
        values.put("svt_amount", svtAmount);
        values.put("center_amount", centerAmount);
        values.put("created_at", String.valueOf(System.currentTimeMillis()));
        return getWritableDatabase().insert("tariff_options", null, values);
    }

    public boolean updateTariffOption(long id, String level, String packKey, int studentAmount, int mathAmount,
                                      int frenchAmount, int pcAmount, int svtAmount, int centerAmount) {
        ContentValues values = new ContentValues();
        values.put("level", level);
        values.put("pack_key", packKey);
        values.put("student_amount", studentAmount);
        values.put("math_amount", mathAmount);
        values.put("french_amount", frenchAmount);
        values.put("pc_amount", pcAmount);
        values.put("svt_amount", svtAmount);
        values.put("center_amount", centerAmount);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            boolean updated = db.update("tariff_options", values, "id = ?", new String[]{String.valueOf(id)}) > 0;
            if (updated) {
                if (mathAmount <= 0) db.delete("tariff_teacher_assignments", "tariff_id = ? AND subject_key = 'math'", new String[]{String.valueOf(id)});
                if (frenchAmount <= 0) db.delete("tariff_teacher_assignments", "tariff_id = ? AND subject_key = 'french'", new String[]{String.valueOf(id)});
                if (pcAmount <= 0) db.delete("tariff_teacher_assignments", "tariff_id = ? AND subject_key = 'pc'", new String[]{String.valueOf(id)});
                if (svtAmount <= 0) db.delete("tariff_teacher_assignments", "tariff_id = ? AND subject_key = 'svt'", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
            return updated;
        } finally {
            db.endTransaction();
        }
    }

    public boolean tariffOptionExists(String level, String packKey, long excludedId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM tariff_options WHERE level = ? AND pack_key = ? AND id <> ?",
                new String[]{level, packKey, String.valueOf(excludedId)});
        boolean exists = cursor.moveToFirst() && cursor.getInt(0) > 0;
        cursor.close();
        return exists;
    }

    public boolean deleteTariffOption(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("tariff_teacher_assignments", "tariff_id = ?", new String[]{String.valueOf(id)});
            boolean deleted = db.delete("tariff_options", "id = ?", new String[]{String.valueOf(id)}) > 0;
            db.setTransactionSuccessful();
            return deleted;
        } finally {
            db.endTransaction();
        }
    }

    public long addTeacher(String fullName, String phone, String level, String subjects) {
        ContentValues values = new ContentValues();
        values.put("full_name", fullName.trim());
        values.put("phone", phone.trim());
        values.put("level", level);
        values.put("subjects", subjects);
        values.put("active", 1);
        return getWritableDatabase().insert("teachers", null, values);
    }

    public List<Teacher> findTeachers(String query) {
        List<Teacher> teachers = new ArrayList<>();
        String normalized = "%" + query.trim() + "%";
        Cursor cursor = getReadableDatabase().query(
                "teachers",
                null,
                "full_name LIKE ? OR phone LIKE ? OR level LIKE ? OR subjects LIKE ?",
                new String[]{normalized, normalized, normalized, normalized},
                null, null, "full_name COLLATE NOCASE");
        while (cursor.moveToNext()) {
            teachers.add(new Teacher(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    cursor.getString(cursor.getColumnIndexOrThrow("subjects")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1));
        }
        cursor.close();
        return teachers;
    }

    public Teacher getTeacher(long id) {
        Cursor cursor = getReadableDatabase().query("teachers", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Teacher teacher = null;
        if (cursor.moveToFirst()) teacher = readTeacher(cursor);
        cursor.close();
        return teacher;
    }

    public int getTeacherStudentCount(long teacherId) {
        return queryInt(getReadableDatabase(),"SELECT COUNT(*) FROM students WHERE math_teacher_id = ? OR french_teacher_id = ? OR pc_teacher_id = ? OR svt_teacher_id = ?",new String[]{String.valueOf(teacherId),String.valueOf(teacherId),String.valueOf(teacherId),String.valueOf(teacherId)});
    }


    public List<TeacherBalance> getTeacherBalances(String dueMonth) {
        List<TeacherBalance> balances = new ArrayList<>();
        for (Teacher teacher : findTeachers("")) balances.add(getTeacherBalance(teacher, dueMonth));
        return balances;
    }

    public List<TeacherBalance> getTeacherMonthlyBalances(long teacherId) {
        List<TeacherBalance> balances = new ArrayList<>();
        Teacher teacher = getTeacher(teacherId);
        if (teacher == null) return balances;
        refreshMonthlyDuesAndAdvances();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT DISTINCT due_month FROM monthly_dues WHERE math_teacher_id = ? OR french_teacher_id = ? OR pc_teacher_id = ? OR svt_teacher_id = ? " +
                        "ORDER BY due_month DESC",
                new String[]{String.valueOf(teacherId), String.valueOf(teacherId), String.valueOf(teacherId), String.valueOf(teacherId)});
        while (cursor.moveToNext()) {
            balances.add(getTeacherBalance(teacher, cursor.getString(cursor.getColumnIndexOrThrow("due_month"))));
        }
        cursor.close();
        return balances;
    }


    public List<TeacherLevelBalance> getTeacherLevelBalances(long teacherId, String dueMonth) {
        List<TeacherLevelBalance> balances = new ArrayList<>();
        String storedMonth = storedMonthKey(dueMonth);
        if (storedMonth == null) return balances;
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT s.level, " +
                "COALESCE(SUM(CASE WHEN d.math_teacher_id = ? THEN a.math_amount ELSE 0 END), 0) AS math_total, " +
                "COALESCE(SUM(CASE WHEN d.french_teacher_id = ? THEN a.french_amount ELSE 0 END), 0) AS french_total, " +
                "COALESCE(SUM(CASE WHEN d.pc_teacher_id = ? THEN a.pc_amount ELSE 0 END), 0) AS pc_total, " +
                "COALESCE(SUM(CASE WHEN d.svt_teacher_id = ? THEN a.svt_amount ELSE 0 END), 0) AS svt_total " +
                "FROM payment_applications a INNER JOIN monthly_dues d ON d.id = a.due_id " +
                "INNER JOIN students s ON s.id = d.student_id " +
                "WHERE d.due_month = ? AND (d.math_teacher_id = ? OR d.french_teacher_id = ? OR d.pc_teacher_id = ? OR d.svt_teacher_id = ?) " +
                "GROUP BY s.level",
                new String[]{String.valueOf(teacherId),String.valueOf(teacherId),String.valueOf(teacherId),String.valueOf(teacherId),
                        storedMonth,String.valueOf(teacherId),String.valueOf(teacherId),String.valueOf(teacherId),String.valueOf(teacherId)});
        while (cursor.moveToNext()) {
            balances.add(new TeacherLevelBalance(cursor.getString(0),cursor.getInt(1),cursor.getInt(2),cursor.getInt(3),cursor.getInt(4)));
        }
        cursor.close();
        return balances;
    }


    public TeacherBalance getTeacherBalance(long teacherId, String dueMonth) {
        Teacher teacher = getTeacher(teacherId);
        return teacher == null ? null : getTeacherBalance(teacher, dueMonth);
    }


    private TeacherBalance getTeacherBalance(Teacher teacher, String dueMonth) {
        String storedDueMonth = storedMonthKey(dueMonth);
        boolean hasMonth = storedDueMonth != null;
        String dueFilter = hasMonth ? " WHERE d.due_month = ?" : "";
        String[] allocationArguments = hasMonth
                ? new String[]{String.valueOf(teacher.id), String.valueOf(teacher.id), String.valueOf(teacher.id), String.valueOf(teacher.id), storedDueMonth}
                : new String[]{String.valueOf(teacher.id), String.valueOf(teacher.id), String.valueOf(teacher.id), String.valueOf(teacher.id)};
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT " +
                        "COALESCE(SUM(CASE WHEN d.math_teacher_id = ? THEN a.math_amount ELSE 0 END), 0) AS math_total, " +
                        "COALESCE(SUM(CASE WHEN d.french_teacher_id = ? THEN a.french_amount ELSE 0 END), 0) AS french_total, " +
                        "COALESCE(SUM(CASE WHEN d.pc_teacher_id = ? THEN a.pc_amount ELSE 0 END), 0) AS pc_total, " +
                        "COALESCE(SUM(CASE WHEN d.svt_teacher_id = ? THEN a.svt_amount ELSE 0 END), 0) AS svt_total " +
                        "FROM payment_applications a INNER JOIN monthly_dues d ON d.id = a.due_id" + dueFilter,
                allocationArguments);
        int math = 0, french = 0, pc = 0, svt = 0;
        if (cursor.moveToFirst()) {
            math = cursor.getInt(cursor.getColumnIndexOrThrow("math_total"));
            french = cursor.getInt(cursor.getColumnIndexOrThrow("french_total"));
            pc = cursor.getInt(cursor.getColumnIndexOrThrow("pc_total"));
            svt = cursor.getInt(cursor.getColumnIndexOrThrow("svt_total"));
        }
        cursor.close();
        int paid = queryInt(getReadableDatabase(),
                "SELECT COALESCE(SUM(amount), 0) FROM teacher_settlements WHERE teacher_id = ?" + (hasMonth ? " AND (reference_month = ? OR reference_month = ?)" : ""),
                hasMonth ? new String[]{String.valueOf(teacher.id), storedDueMonth, legacyMonthKey(storedDueMonth)} : new String[]{String.valueOf(teacher.id)});
        return new TeacherBalance(teacher, math, french, pc, svt, paid, hasMonth ? storedDueMonth : "");
    }

    public long recordTeacherSettlement(long teacherId, int amount, String paymentDate, String referenceMonth, String note) {
        String storedMonth = storedMonthKey(referenceMonth);
        if (storedMonth == null) return -1;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            TeacherBalance balance = getTeacherBalance(teacherId, storedMonth);
            // The whole remaining salary of the month is paid at once; a later complement is allowed
            // when student payments for that month arrive after the first settlement.
            if (balance == null || !FinanceRules.canRecordSettlement(amount, balance.earnedAmount, balance.paidAmount)) return -1;
            ContentValues values = new ContentValues();
            values.put("teacher_id", teacherId);
            values.put("amount", amount);
            values.put("payment_date", paymentDate.trim());
            values.put("reference_month", storedMonth);
            values.put("note", note == null ? "" : note.trim());
            values.put("created_at", String.valueOf(System.currentTimeMillis()));
            long id = db.insert("teacher_settlements", null, values);
            if (id > 0) db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }



    public TariffOption getTariffOption(long id) {
        Cursor cursor = getReadableDatabase().query("tariff_options", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        TariffOption option = null;
        if (cursor.moveToFirst()) option = readTariffOption(cursor);
        cursor.close();
        return option;
    }

    public List<Teacher> getCompatibleTeachers(String level, String subjectKey) {
        List<Teacher> teachers = new ArrayList<>();
        String subject = subjectLabelForStorage(subjectKey);
        if (subject.isEmpty()) return teachers;
        Cursor cursor = getReadableDatabase().query(
                "teachers", null,
                "active = 1 AND level LIKE ? AND subjects LIKE ?",
                new String[]{"%" + level + "%", "%" + subject + "%"},
                null, null, "full_name COLLATE NOCASE");
        while (cursor.moveToNext()) {
            teachers.add(readTeacher(cursor));
        }
        cursor.close();
        return teachers;
    }

    public Map<String, Teacher> getTariffTeacherAssignments(long tariffId) {
        Map<String, Teacher> assignments = new HashMap<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT a.subject_key, t.id, t.full_name, t.phone, t.level, t.subjects, t.active " +
                        "FROM tariff_teacher_assignments a INNER JOIN teachers t ON t.id = a.teacher_id " +
                        "WHERE a.tariff_id = ?", new String[]{String.valueOf(tariffId)});
        while (cursor.moveToNext()) {
            assignments.put(cursor.getString(cursor.getColumnIndexOrThrow("subject_key")), readTeacher(cursor));
        }
        cursor.close();
        return assignments;
    }

    public void saveTariffTeacherAssignment(long tariffId, String subjectKey, long teacherId) {
        ContentValues values = new ContentValues();
        values.put("tariff_id", tariffId);
        values.put("subject_key", subjectKey);
        values.put("teacher_id", teacherId);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.insertWithOnConflict("tariff_teacher_assignments", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            fillMissingTeacher(db, tariffId, subjectKey, teacherId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Students keep the teachers known when they enrolled. When a formula receives a teacher later, the students of
     * that formula (same level and pack) and their monthly dues that still have no teacher (id 0) for this subject
     * are linked to it. Existing non-zero teachers are never replaced, and no amount changes.
     */
    private void fillMissingTeacher(SQLiteDatabase db, long tariffId, String subjectKey, long teacherId) {
        String teacherColumn = teacherColumn(subjectKey), amountColumn = amountColumn(subjectKey);
        if (teacherColumn == null || teacherId <= 0) return;
        Cursor cursor = db.query("tariff_options", new String[]{"level", "pack_key"}, "id = ?", new String[]{String.valueOf(tariffId)}, null, null, null);
        String level = null, packKey = null;
        if (cursor.moveToFirst()) {
            level = cursor.getString(0);
            packKey = cursor.getString(1);
        }
        cursor.close();
        if (level == null || packKey == null || packKey.isEmpty()) return;
        db.execSQL("UPDATE students SET " + teacherColumn + " = ? WHERE " + teacherColumn + " = 0 AND " + amountColumn + " > 0 AND level = ? AND pack_key = ?",
                new Object[]{teacherId, level, packKey});
        db.execSQL("UPDATE monthly_dues SET " + teacherColumn + " = ? WHERE " + teacherColumn + " = 0 AND " + amountColumn + " > 0 " +
                        "AND student_id IN (SELECT id FROM students WHERE level = ? AND pack_key = ?)",
                new Object[]{teacherId, level, packKey});
    }

    /** Applies fillMissingTeacher for every saved assignment (repairs records created before the teacher existed). */
    private void fillMissingTeachersForAllAssignments(SQLiteDatabase db) {
        Cursor cursor = db.query("tariff_teacher_assignments", new String[]{"tariff_id", "subject_key", "teacher_id"}, null, null, null, null, null);
        List<Object[]> assignments = new ArrayList<>();
        while (cursor.moveToNext()) {
            assignments.add(new Object[]{cursor.getLong(0), cursor.getString(1), cursor.getLong(2)});
        }
        cursor.close();
        for (Object[] assignment : assignments) {
            fillMissingTeacher(db, (Long) assignment[0], (String) assignment[1], (Long) assignment[2]);
        }
    }

    private static String teacherColumn(String subjectKey) {
        if ("math".equals(subjectKey)) return "math_teacher_id";
        if ("french".equals(subjectKey)) return "french_teacher_id";
        if ("pc".equals(subjectKey)) return "pc_teacher_id";
        if ("svt".equals(subjectKey)) return "svt_teacher_id";
        return null;
    }

    private static String amountColumn(String subjectKey) {
        if ("math".equals(subjectKey)) return "math_amount";
        if ("french".equals(subjectKey)) return "french_amount";
        if ("pc".equals(subjectKey)) return "pc_amount";
        if ("svt".equals(subjectKey)) return "svt_amount";
        return null;
    }

    public void removeTariffTeacherAssignment(long tariffId, String subjectKey) {
        getWritableDatabase().delete("tariff_teacher_assignments", "tariff_id = ? AND subject_key = ?", new String[]{String.valueOf(tariffId), subjectKey});
    }

    public int autoAssignUniqueTeachers(long tariffId) {
        TariffOption option = getTariffOption(tariffId);
        if (option == null) return 0;
        Map<String, Teacher> assignments = getTariffTeacherAssignments(tariffId);
        int assigned = 0;
        if (autoAssignUniqueTeacher(option, assignments, "math", option.mathAmount)) assigned++;
        if (autoAssignUniqueTeacher(option, assignments, "french", option.frenchAmount)) assigned++;
        if (autoAssignUniqueTeacher(option, assignments, "pc", option.pcAmount)) assigned++;
        if (autoAssignUniqueTeacher(option, assignments, "svt", option.svtAmount)) assigned++;
        return assigned;
    }

    public void refreshAutomaticTeacherAssignments() {
        List<Long> tariffIds = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query("tariff_options", new String[]{"id"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            tariffIds.add(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        }
        cursor.close();
        for (Long tariffId : tariffIds) autoAssignUniqueTeachers(tariffId);
    }

    private boolean autoAssignUniqueTeacher(TariffOption option, Map<String, Teacher> assignments, String subjectKey, int amount) {
        if (amount == 0 || assignments.containsKey(subjectKey)) return false;
        List<Teacher> compatible = getCompatibleTeachers(option.level, subjectKey);
        if (compatible.size() != 1) return false;
        saveTariffTeacherAssignment(option.id, subjectKey, compatible.get(0).id);
        return true;
    }

    public Student getStudent(long id) {
        Cursor cursor = getReadableDatabase().query("students", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Student student = null;
        if (cursor.moveToFirst()) student = readStudent(cursor);
        cursor.close();
        return student;
    }

    public StudentFinancialSummary getFinancialSummary(long studentId) {
        SQLiteDatabase db = getReadableDatabase();
        int due = queryInt(db, "SELECT COALESCE(SUM(total_amount), 0) FROM monthly_dues WHERE student_id = ?", new String[]{String.valueOf(studentId)});
        int paid = queryInt(db,
                "SELECT COALESCE(SUM(a.amount), 0) FROM payment_applications a " +
                        "INNER JOIN monthly_dues d ON d.id = a.due_id WHERE d.student_id = ?",
                new String[]{String.valueOf(studentId)});
        int advance = queryInt(db, "SELECT COALESCE(SUM(advance_balance), 0) FROM payments WHERE student_id = ?", new String[]{String.valueOf(studentId)});
        return new StudentFinancialSummary(due, paid, Math.max(0, due - paid), advance);
    }

    /** Monthly history shown on a student's financial sheet. */
    public List<StudentMonthBalance> getStudentMonthlyBalances(long studentId) {
        List<StudentMonthBalance> balances = new ArrayList<>();
        Student student = getStudent(studentId);
        if (student == null) return balances;
        refreshMonthlyDuesAndAdvances();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT d.due_month, d.total_amount, COALESCE(SUM(a.amount), 0) AS paid_amount " +
                        "FROM monthly_dues d LEFT JOIN payment_applications a ON a.due_id = d.id " +
                        "WHERE d.student_id = ? GROUP BY d.id ORDER BY d.due_month DESC",
                new String[]{String.valueOf(studentId)});
        while (cursor.moveToNext()) {
            balances.add(new StudentMonthBalance(
                    cursor.getString(cursor.getColumnIndexOrThrow("due_month")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_amount"))));
        }
        cursor.close();
        return balances;
    }

    public PaymentResult recordPayment(long studentId, int amount, String paymentDate, String referenceMonth, String note) {
        if (amount <= 0) return null;
        Student student = getStudent(studentId);
        if (student == null) return null;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            PaymentPreview outcome = performPayment(db, student, amount, paymentDate, referenceMonth, note);
            if (outcome.status != PaymentPreview.OK || outcome.result == null) return null;
            db.setTransactionSuccessful();
            return outcome.result;
        } catch (android.database.SQLException error) {
            return null;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Runs exactly the recordPayment computation inside a transaction that is always rolled back, so the screen can
     * show what the payment will really do (advance used first, allocation, split, new advance). amount may be 0.
     */
    public PaymentPreview previewPayment(long studentId, int amount, String paymentDate, String referenceMonth) {
        Student student = getStudent(studentId);
        if (student == null) return PaymentPreview.error(PaymentPreview.INVALID);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            return performPayment(db, student, Math.max(0, amount), paymentDate, referenceMonth, "");
        } catch (android.database.SQLException error) {
            return PaymentPreview.error(PaymentPreview.INVALID);
        } finally {
            db.endTransaction(); // never marked successful: nothing is kept
        }
    }

    private PaymentPreview performPayment(SQLiteDatabase db, Student student, int amount, String paymentDate,
                                          String referenceMonth, String note) {
        Calendar paymentMonth = monthForReference(referenceMonth);
        String enrollmentMonth = MonthKeys.monthOfDate(student.enrollmentDate);
        if (paymentMonth == null || enrollmentMonth == null || student.negotiatedAmount <= 0) {
            return PaymentPreview.error(PaymentPreview.INVALID);
        }
        String dueMonth = monthKey(paymentMonth);
        // No due exists before enrollment: such a payment used to become an advance silently.
        if (dueMonth.compareTo(enrollmentMonth) < 0) return PaymentPreview.error(PaymentPreview.BEFORE_ENROLLMENT);

        ensureMonthlyDuesThrough(db, student, paymentMonth);
        int[] monthBefore = monthDueAndPaid(db, student.id, dueMonth);
        int advanceApplied = applyExistingAdvancesToDueMonth(db, student.id, dueMonth, paymentDate);
        if (amount == 0) {
            return new PaymentPreview(PaymentPreview.OK, monthBefore[0], monthBefore[1], advanceApplied, 0, 0,
                    new int[FinanceRules.COMPONENTS], null);
        }

        ContentValues payment = new ContentValues();
        payment.put("student_id", student.id);
        payment.put("amount", amount);
        payment.put("payment_date", paymentDate.trim());
        payment.put("reference_month", referenceMonth.trim());
        payment.put("note", note == null ? "" : note.trim());
        payment.put("allocated_amount", 0);
        payment.put("advance_amount", 0);
        payment.put("advance_balance", 0);
        payment.put("created_at", String.valueOf(System.currentTimeMillis()));
        long paymentId = db.insert("payments", null, payment);
        if (paymentId <= 0) return PaymentPreview.error(PaymentPreview.INVALID);

        List<FinanceRules.Application> applications = applyPaymentToDueMonth(db, paymentId, student.id, dueMonth, amount, "paiement", paymentDate);
        int allocated = FinanceRules.totalAmount(applications);
        int advance = amount - allocated;
        ContentValues totals = new ContentValues();
        totals.put("allocated_amount", allocated);
        totals.put("advance_amount", advance);
        totals.put("advance_balance", advance);
        db.update("payments", totals, "id = ?", new String[]{String.valueOf(paymentId)});

        StudentFinancialSummary summary = getFinancialSummaryInDatabase(db, student.id);
        PaymentResult result = new PaymentResult(paymentId, allocated, advance, advanceApplied, summary.outstandingAmount);
        return new PaymentPreview(PaymentPreview.OK, monthBefore[0], monthBefore[1], advanceApplied, allocated, advance,
                FinanceRules.totalSplit(applications), result);
    }

    /** {due amount, amount already applied} for one student and month; {0, 0} when the due does not exist. */
    private int[] monthDueAndPaid(SQLiteDatabase db, long studentId, String dueMonth) {
        Cursor cursor = db.rawQuery(
                "SELECT d.total_amount, COALESCE(SUM(a.amount), 0) FROM monthly_dues d " +
                        "LEFT JOIN payment_applications a ON a.due_id = d.id WHERE d.student_id = ? AND d.due_month = ? GROUP BY d.id",
                new String[]{String.valueOf(studentId), dueMonth});
        int[] result = {0, 0};
        if (cursor.moveToFirst()) {
            result[0] = cursor.getInt(0);
            result[1] = cursor.getInt(1);
        }
        cursor.close();
        return result;
    }

    public List<PaymentRecord> getRecentPayments() {
        List<PaymentRecord> payments = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT p.*, s.first_name, s.last_name, s.level, " +
                        "COALESCE(SUM(a.math_amount), 0) AS paid_math, " +
                        "COALESCE(SUM(a.french_amount), 0) AS paid_french, " +
                        "COALESCE(SUM(a.pc_amount), 0) AS paid_pc, " +
                        "COALESCE(SUM(a.svt_amount), 0) AS paid_svt, " +
                        "COALESCE(SUM(a.center_amount), 0) AS paid_center " +
                        "FROM payments p INNER JOIN students s ON s.id = p.student_id " +
                        "LEFT JOIN payment_applications a ON a.payment_id = p.id " +
                        "GROUP BY p.id ORDER BY p.id DESC", null);
        while (cursor.moveToNext()) {
            String name = (cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                    cursor.getString(cursor.getColumnIndexOrThrow("last_name"))).trim();
            payments.add(new PaymentRecord(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("student_id")),
                    name,
                    cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("allocated_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("advance_amount")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("advance_balance")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_math")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_french")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_pc")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_svt")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_center")),
                    cursor.getString(cursor.getColumnIndexOrThrow("payment_date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("reference_month")),
                    cursor.getString(cursor.getColumnIndexOrThrow("note"))));
        }
        cursor.close();
        return payments;
    }

    public DashboardSummary getDashboardSummary() {
        refreshMonthlyDuesAndAdvances();
        SQLiteDatabase db = getReadableDatabase();
        int activeStudents = queryInt(db, "SELECT COUNT(*) FROM students", null);
        int received = queryInt(db, "SELECT COALESCE(SUM(amount), 0) FROM payments", null);
        int due = queryInt(db, "SELECT COALESCE(SUM(total_amount), 0) FROM monthly_dues", null);
        int allocated = queryInt(db, "SELECT COALESCE(SUM(amount), 0) FROM payment_applications", null);
        int center = queryInt(db, "SELECT COALESCE(SUM(center_amount), 0) FROM payment_applications", null);
        int teachersEarned = queryInt(db, "SELECT COALESCE(SUM(math_amount + french_amount + pc_amount + svt_amount), 0) FROM payment_applications", null);
        int teacherSettlements = queryInt(db, "SELECT COALESCE(SUM(amount), 0) FROM teacher_settlements", null);
        int teachers = Math.max(0, teachersEarned - teacherSettlements);
        int advances = queryInt(db, "SELECT COALESCE(SUM(advance_balance), 0) FROM payments", null);
        return new DashboardSummary(activeStudents, received, Math.max(0, due - allocated), center, teachers, advances);
    }


    public MonthlyReport getMonthlyReport(String month) {
        String storedMonth = storedMonthKey(month);
        if (storedMonth == null) return new MonthlyReport("", 0, 0, 0, 0, 0, 0, 0, new ArrayList<>());
        refreshMonthlyDuesAndAdvances();
        Calendar targetMonth = calendarForStoredMonth(storedMonth);
        if (targetMonth != null) {
            List<Student> students = findStudents("");
            SQLiteDatabase writable = getWritableDatabase();
            writable.beginTransaction();
            try {
                for (Student student : students) {
                    if (student.negotiatedAmount > 0) ensureMonthlyDuesThrough(writable, student, targetMonth);
                }
                writable.setTransactionSuccessful();
            } finally {
                writable.endTransaction();
            }
        }

        SQLiteDatabase db = getReadableDatabase();
        int received = queryInt(db, "SELECT COALESCE(SUM(a.amount), 0) FROM payment_applications a INNER JOIN monthly_dues d ON d.id = a.due_id WHERE d.due_month = ?", new String[]{storedMonth});
        int center = queryInt(db, "SELECT COALESCE(SUM(a.center_amount), 0) FROM payment_applications a INNER JOIN monthly_dues d ON d.id = a.due_id WHERE d.due_month = ?", new String[]{storedMonth});
        int teachersEarned = queryInt(db, "SELECT COALESCE(SUM(a.math_amount + a.french_amount + a.pc_amount + a.svt_amount), 0) FROM payment_applications a INNER JOIN monthly_dues d ON d.id = a.due_id WHERE d.due_month = ?", new String[]{storedMonth});
        int teachersPaid = queryInt(db, "SELECT COALESCE(SUM(amount), 0) FROM teacher_settlements WHERE reference_month = ? OR reference_month = ?", new String[]{storedMonth, legacyMonthKey(storedMonth)});
        int paidCount = 0, partialCount = 0, unpaidCount = 0;
        List<MonthlyStudentBalance> unpaid = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT s.id, s.first_name, s.last_name, s.level, d.total_amount, COALESCE(SUM(a.amount), 0) AS paid_amount " +
                        "FROM monthly_dues d INNER JOIN students s ON s.id = d.student_id LEFT JOIN payment_applications a ON a.due_id = d.id " +
                        "WHERE d.due_month = ? GROUP BY d.id ORDER BY s.last_name COLLATE NOCASE, s.first_name COLLATE NOCASE",
                new String[]{storedMonth});
        while (cursor.moveToNext()) {
            String name = (cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " + cursor.getString(cursor.getColumnIndexOrThrow("last_name"))).trim();
            int total = cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")), paid = cursor.getInt(cursor.getColumnIndexOrThrow("paid_amount"));
            if (paid >= total) paidCount++; else if (paid > 0) partialCount++; else unpaidCount++;
            if (paid < total) unpaid.add(new MonthlyStudentBalance(cursor.getLong(cursor.getColumnIndexOrThrow("id")), name, cursor.getString(cursor.getColumnIndexOrThrow("level")), total, paid));
        }
        cursor.close();
        return new MonthlyReport(storedMonth, received, center, teachersEarned, teachersPaid, paidCount, partialCount, unpaidCount, unpaid);
    }

    private void rebuildPaymentApplicationsByReferenceMonth(SQLiteDatabase db) {
        // The payment's declared month is authoritative. Applications are derived data,
        // so rebuilding them fixes records made by the former oldest-debt rule.
        db.delete("payment_applications", null, null);
        ContentValues reset = new ContentValues();
        reset.put("allocated_amount", 0);
        reset.put("advance_amount", 0);
        reset.put("advance_balance", 0);
        db.update("payments", reset, null, null);

        Cursor payments = db.query("payments", new String[]{"id", "student_id", "amount", "payment_date", "reference_month"}, null, null, null, null, "id ASC");
        while (payments.moveToNext()) {
            long paymentId = payments.getLong(payments.getColumnIndexOrThrow("id"));
            long studentId = payments.getLong(payments.getColumnIndexOrThrow("student_id"));
            int amount = payments.getInt(payments.getColumnIndexOrThrow("amount"));
            String paymentDate = payments.getString(payments.getColumnIndexOrThrow("payment_date"));
            Calendar paymentMonth = monthForReference(payments.getString(payments.getColumnIndexOrThrow("reference_month")));
            if (paymentMonth == null) paymentMonth = monthForDate(paymentDate);
            Student student = getStudentInDatabase(db, studentId);
            if (student == null || paymentMonth == null || amount <= 0) continue;

            ensureMonthlyDuesThrough(db, student, paymentMonth);
            String dueMonth = monthKey(paymentMonth);
            applyExistingAdvancesToDueMonth(db, studentId, dueMonth, paymentDate);
            int allocated = FinanceRules.totalAmount(applyPaymentToDueMonth(db, paymentId, studentId, dueMonth, amount, "paiement", paymentDate));
            int advance = amount - allocated;
            ContentValues totals = new ContentValues();
            totals.put("allocated_amount", allocated);
            totals.put("advance_amount", advance);
            totals.put("advance_balance", advance);
            db.update("payments", totals, "id = ?", new String[]{String.valueOf(paymentId)});
        }
        payments.close();
    }

    private Student getStudentInDatabase(SQLiteDatabase db, long studentId) {
        Cursor cursor = db.query("students", null, "id = ?", new String[]{String.valueOf(studentId)}, null, null, null);
        Student student = cursor.moveToFirst() ? readStudent(cursor) : null;
        cursor.close();
        return student;
    }

    /**
     * Brings every student's monthly dues up to the current month and uses the available advances on the months
     * that follow them. Also links students to teachers assigned to their formula after they enrolled.
     */
    public void refreshMonthlyDuesAndAdvances() {
        Calendar todayMonth = Calendar.getInstance();
        todayMonth.set(Calendar.DAY_OF_MONTH, 1);
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Calendar.getInstance().getTime());
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            fillMissingTeachersForAllAssignments(db);
            for (Student student : findStudents("")) {
                if (student.negotiatedAmount <= 0) continue;
                ensureMonthlyDuesThrough(db, student, todayMonth);
            }
            applyAdvancesToFollowingMonths(db, monthKey(todayMonth), today);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void ensureMonthlyDuesThrough(SQLiteDatabase db, Student student, Calendar targetMonth) {
        Calendar month = monthForDate(student.enrollmentDate);
        if (month == null || month.after(targetMonth)) return;
        while (!month.after(targetMonth)) {
            ContentValues due = new ContentValues();
            due.put("student_id", student.id);
            due.put("due_month", monthKey(month));
            due.put("total_amount", student.negotiatedAmount);
            due.put("math_amount", student.mathAmount);
            due.put("french_amount", student.frenchAmount);
            due.put("pc_amount", student.pcAmount);
            due.put("svt_amount", student.svtAmount);
            due.put("center_amount", student.centerAmount);
            due.put("math_teacher_id", student.mathTeacherId);
            due.put("french_teacher_id", student.frenchTeacherId);
            due.put("pc_teacher_id", student.pcTeacherId);
            due.put("svt_teacher_id", student.svtTeacherId);
            due.put("created_at", String.valueOf(System.currentTimeMillis()));
            db.insertWithOnConflict("monthly_dues", null, due, SQLiteDatabase.CONFLICT_IGNORE);
            month.add(Calendar.MONTH, 1);
        }
    }

    /** Rule used when a payment is recorded: existing advances first cover the declared month. */
    private int applyExistingAdvancesToDueMonth(SQLiteDatabase db, long studentId, String dueMonth, String appliedAt) {
        List<FinanceRules.Due> dues = getUnpaidDues(db, studentId, dueMonth);
        if (dues.isEmpty()) return 0;
        List<FinanceRules.Advance> advances = getAdvanceSources(db, studentId);
        List<FinanceRules.Application> applications = FinanceRules.applyAdvances(advances, dues);
        insertApplications(db, applications, "avance", appliedAt);
        saveAdvanceBalances(db, advances);
        return FinanceRules.totalAmount(applications);
    }

    /** Advances cover the unpaid months that follow the month they come from, up to the current month. */
    private void applyAdvancesToFollowingMonths(SQLiteDatabase db, String currentMonth, String appliedAt) {
        List<Long> studentIds = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT student_id FROM payments WHERE advance_balance > 0", null);
        while (cursor.moveToNext()) studentIds.add(cursor.getLong(0));
        cursor.close();
        for (Long studentId : studentIds) {
            List<FinanceRules.Advance> advances = getAdvanceSources(db, studentId);
            List<FinanceRules.Due> dues = getUnpaidDues(db, studentId, null);
            List<FinanceRules.Application> applications = FinanceRules.applyAdvancesToFollowingMonths(advances, dues, currentMonth);
            if (applications.isEmpty()) continue;
            insertApplications(db, applications, "avance", appliedAt);
            saveAdvanceBalances(db, advances);
        }
    }

    private List<FinanceRules.Application> applyPaymentToDueMonth(SQLiteDatabase db, long paymentId, long studentId, String dueMonth, int amount, String sourceKind, String appliedAt) {
        List<FinanceRules.Application> applications = FinanceRules.allocate(paymentId, amount, getUnpaidDues(db, studentId, dueMonth));
        insertApplications(db, applications, sourceKind, appliedAt);
        return applications;
    }

    /** Throws when a row cannot be written so that the surrounding transaction is rolled back. */
    private void insertApplications(SQLiteDatabase db, List<FinanceRules.Application> applications, String sourceKind, String appliedAt) {
        for (FinanceRules.Application item : applications) {
            ContentValues application = new ContentValues();
            application.put("payment_id", item.paymentId);
            application.put("due_id", item.dueId);
            application.put("amount", item.amount);
            application.put("math_amount", item.split[0]);
            application.put("french_amount", item.split[1]);
            application.put("pc_amount", item.split[2]);
            application.put("svt_amount", item.split[3]);
            application.put("center_amount", item.split[4]);
            application.put("source_kind", sourceKind);
            application.put("applied_at", appliedAt);
            if (db.insert("payment_applications", null, application) == -1) {
                throw new android.database.SQLException("payment_applications insert failed");
            }
        }
    }

    private void saveAdvanceBalances(SQLiteDatabase db, List<FinanceRules.Advance> advances) {
        for (FinanceRules.Advance advance : advances) {
            ContentValues values = new ContentValues();
            values.put("advance_balance", advance.balance);
            db.update("payments", values, "id = ?", new String[]{String.valueOf(advance.paymentId)});
        }
    }

    /** Unpaid dues of a student (optionally one month), oldest first. */
    private List<FinanceRules.Due> getUnpaidDues(SQLiteDatabase db, long studentId, String dueMonth) {
        List<FinanceRules.Due> dues = new ArrayList<>();
        boolean hasDueMonth = dueMonth != null && dueMonth.matches("\\d{4}-(0[1-9]|1[0-2])");
        Cursor cursor = db.rawQuery(
                "SELECT d.id, d.due_month, d.total_amount, d.math_amount, d.french_amount, d.pc_amount, d.svt_amount, d.center_amount, " +
                        "COALESCE(SUM(a.amount), 0) AS paid_amount, COALESCE(SUM(a.math_amount), 0) AS paid_math, " +
                        "COALESCE(SUM(a.french_amount), 0) AS paid_french, COALESCE(SUM(a.pc_amount), 0) AS paid_pc, " +
                        "COALESCE(SUM(a.svt_amount), 0) AS paid_svt, COALESCE(SUM(a.center_amount), 0) AS paid_center " +
                        "FROM monthly_dues d LEFT JOIN payment_applications a ON a.due_id = d.id " +
                        "WHERE d.student_id = ?" + (hasDueMonth ? " AND d.due_month = ?" : "") + " GROUP BY d.id " +
                        "HAVING d.total_amount > COALESCE(SUM(a.amount), 0) ORDER BY d.due_month ASC, d.id ASC",
                hasDueMonth ? new String[]{String.valueOf(studentId), dueMonth} : new String[]{String.valueOf(studentId)});
        while (cursor.moveToNext()) {
            dues.add(new FinanceRules.Due(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("due_month")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")),
                    new int[]{
                            cursor.getInt(cursor.getColumnIndexOrThrow("math_amount")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("french_amount")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("pc_amount")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("svt_amount")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("center_amount"))},
                    cursor.getInt(cursor.getColumnIndexOrThrow("paid_amount")),
                    new int[]{
                            cursor.getInt(cursor.getColumnIndexOrThrow("paid_math")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("paid_french")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("paid_pc")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("paid_svt")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("paid_center"))}));
        }
        cursor.close();
        return dues;
    }

    /** Payments with an advance still available, oldest first, with the month they were declared for. */
    private List<FinanceRules.Advance> getAdvanceSources(SQLiteDatabase db, long studentId) {
        List<FinanceRules.Advance> advances = new ArrayList<>();
        Cursor cursor = db.query("payments", new String[]{"id", "advance_balance", "reference_month", "payment_date"},
                "student_id = ? AND advance_balance > 0", new String[]{String.valueOf(studentId)}, null, null, "id ASC");
        while (cursor.moveToNext()) {
            String reference = cursor.getString(cursor.getColumnIndexOrThrow("reference_month"));
            String sourceMonth = MonthKeys.stored(reference);
            if (sourceMonth == null) sourceMonth = MonthKeys.monthOfReference(reference);
            if (sourceMonth == null) sourceMonth = MonthKeys.monthOfDate(cursor.getString(cursor.getColumnIndexOrThrow("payment_date")));
            advances.add(new FinanceRules.Advance(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    sourceMonth,
                    cursor.getInt(cursor.getColumnIndexOrThrow("advance_balance"))));
        }
        cursor.close();
        return advances;
    }

    private StudentFinancialSummary getFinancialSummaryInDatabase(SQLiteDatabase db, long studentId) {
        int due = queryInt(db, "SELECT COALESCE(SUM(total_amount), 0) FROM monthly_dues WHERE student_id = ?", new String[]{String.valueOf(studentId)});
        int paid = queryInt(db,
                "SELECT COALESCE(SUM(a.amount), 0) FROM payment_applications a " +
                        "INNER JOIN monthly_dues d ON d.id = a.due_id WHERE d.student_id = ?",
                new String[]{String.valueOf(studentId)});
        int advance = queryInt(db, "SELECT COALESCE(SUM(advance_balance), 0) FROM payments WHERE student_id = ?", new String[]{String.valueOf(studentId)});
        return new StudentFinancialSummary(due, paid, Math.max(0, due - paid), advance);
    }

    private int queryInt(SQLiteDatabase db, String sql, String[] args) {
        Cursor cursor = db.rawQuery(sql, args);
        int value = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return value;
    }

    /** Month of a dd/MM/yyyy date, independent of the phone language (see MonthKeys). */
    private Calendar monthForDate(String date) {
        return calendarForMonth(MonthKeys.monthOfDate(date));
    }

    private Calendar monthForReference(String value) {
        return calendarForMonth(MonthKeys.monthOfReference(value));
    }

    private Calendar calendarForMonth(String storedMonth) {
        if (storedMonth == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(MonthKeys.year(storedMonth), MonthKeys.month(storedMonth) - 1, 1, 0, 0, 0);
        return calendar;
    }

    private String monthKey(Calendar month) {
        return MonthKeys.key(month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1);
    }

    private String storedMonthKey(String value) {
        return MonthKeys.stored(value);
    }

    private Calendar calendarForStoredMonth(String storedMonth) {
        return calendarForMonth(MonthKeys.stored(storedMonth));
    }

    private String legacyMonthKey(String storedMonth) {
        return MonthKeys.legacy(storedMonth);
    }

    private Student readStudent(Cursor cursor) {
        return new Student(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("parent_phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("level")),
                cursor.getString(cursor.getColumnIndexOrThrow("pack_key")),
                cursor.getString(cursor.getColumnIndexOrThrow("enrollment_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("subjects")),
                cursor.getInt(cursor.getColumnIndexOrThrow("negotiated_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("math_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("french_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("pc_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("svt_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("center_amount")),
                cursor.getLong(cursor.getColumnIndexOrThrow("math_teacher_id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("french_teacher_id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("pc_teacher_id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("svt_teacher_id")));
    }

    private TariffOption readTariffOption(Cursor cursor) {
        return new TariffOption(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("level")),
                cursor.getString(cursor.getColumnIndexOrThrow("pack_key")),
                cursor.getInt(cursor.getColumnIndexOrThrow("student_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("math_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("french_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("pc_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("svt_amount")),
                cursor.getInt(cursor.getColumnIndexOrThrow("center_amount")));
    }

    private Teacher readTeacher(Cursor cursor) {
        return new Teacher(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("level")),
                cursor.getString(cursor.getColumnIndexOrThrow("subjects")),
                cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1);
    }

    private String subjectLabelForStorage(String subjectKey) {
        if ("math".equals(subjectKey)) return "Mathématiques";
        if ("french".equals(subjectKey)) return "Français";
        if ("pc".equals(subjectKey)) return "Physique-Chimie";
        if ("svt".equals(subjectKey)) return "SVT";
        return "";
    }
}
