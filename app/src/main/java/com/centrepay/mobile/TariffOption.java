package com.centrepay.mobile;

public class TariffOption {
    public final long id;
    public final String level;
    public final String packKey;
    public final int studentAmount;
    public final int mathAmount;
    public final int frenchAmount;
    public final int pcAmount;
    public final int svtAmount;
    public final int centerAmount;

    public TariffOption(long id, String level, String packKey, int studentAmount, int mathAmount, int frenchAmount,
                        int pcAmount, int svtAmount, int centerAmount) {
        this.id = id;
        this.level = level;
        this.packKey = packKey;
        this.studentAmount = studentAmount;
        this.mathAmount = mathAmount;
        this.frenchAmount = frenchAmount;
        this.pcAmount = pcAmount;
        this.svtAmount = svtAmount;
        this.centerAmount = centerAmount;
    }

    public String displayLabel() {
        return studentAmount + " DH — " + packLabel(packKey);
    }

    public static String packKey(boolean math, boolean french, boolean pc, boolean svt) {
        StringBuilder key = new StringBuilder();
        if (math) key.append("math|");
        if (french) key.append("french|");
        if (pc) key.append("pc|");
        if (svt) key.append("svt|");
        if (key.length() > 0) key.setLength(key.length() - 1);
        return key.toString();
    }

    public static String packLabel(String packKey) {
        if (packKey == null || packKey.isEmpty()) return "Pack non défini";
        return packKey.replace("math", "Math").replace("french", "Français").replace("pc", "PC").replace("svt", "SVT").replace("|", " + ");
    }
}
