package com.centrepay.mobile;

public class Teacher {
    public final long id;
    public final String fullName;
    public final String phone;
    public final String level;
    public final String subjects;
    public final boolean active;

    public Teacher(long id, String fullName, String phone, String level, String subjects, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.level = level;
        this.subjects = subjects;
        this.active = active;
    }
}
