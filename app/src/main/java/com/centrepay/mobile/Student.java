package com.centrepay.mobile;

public class Student {
    public final long id;
    public final String lastName;
    public final String firstName;
    public final String phone;
    public final String parentPhone;
    public final String level;
    public final String packKey;
    public final String enrollmentDate;
    public final String subjects;
    public final int negotiatedAmount;
    public final int mathAmount;
    public final int frenchAmount;
    public final int pcAmount;
    public final int svtAmount;
    public final int centerAmount;
    public final long mathTeacherId;
    public final long frenchTeacherId;
    public final long pcTeacherId;
    public final long svtTeacherId;

    public Student(long id, String lastName, String firstName, String phone, String parentPhone,
                   String level, String packKey, String enrollmentDate, String subjects, int negotiatedAmount,
                   int mathAmount, int frenchAmount, int pcAmount, int svtAmount, int centerAmount) {
        this(id, lastName, firstName, phone, parentPhone, level, packKey, enrollmentDate, subjects, negotiatedAmount,
                mathAmount, frenchAmount, pcAmount, svtAmount, centerAmount, 0, 0, 0, 0);
    }

    public Student(long id, String lastName, String firstName, String phone, String parentPhone,
                   String level, String packKey, String enrollmentDate, String subjects, int negotiatedAmount,
                   int mathAmount, int frenchAmount, int pcAmount, int svtAmount, int centerAmount,
                   long mathTeacherId, long frenchTeacherId, long pcTeacherId, long svtTeacherId) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.phone = phone;
        this.parentPhone = parentPhone;
        this.level = level;
        this.packKey = packKey;
        this.enrollmentDate = enrollmentDate;
        this.subjects = subjects;
        this.negotiatedAmount = negotiatedAmount;
        this.mathAmount = mathAmount;
        this.frenchAmount = frenchAmount;
        this.pcAmount = pcAmount;
        this.svtAmount = svtAmount;
        this.centerAmount = centerAmount;
        this.mathTeacherId = mathTeacherId;
        this.frenchTeacherId = frenchTeacherId;
        this.pcTeacherId = pcTeacherId;
        this.svtTeacherId = svtTeacherId;
    }

    public String fullName() {
        return (firstName + " " + lastName).trim();
    }
}
