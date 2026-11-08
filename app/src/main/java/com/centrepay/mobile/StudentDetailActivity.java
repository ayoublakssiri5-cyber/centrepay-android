package com.centrepay.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class StudentDetailActivity extends Activity {
    private static final int PRIMARY = Palette.PRIMARY, PRIMARY_DARK = Palette.PRIMARY_DARK;
    private static final int TEXT = Palette.TEXT, MUTED = Palette.MUTED;
    private static final int SURFACE = Palette.SURFACE, BORDER = Palette.BORDER;
    private static final int SUCCESS = Palette.SUCCESS, WARNING = Palette.WARNING;
    private static final int ERROR = Palette.ERROR, INFO = Palette.INFO;
    private StudentDatabaseHelper database;
    private long studentId;
    private LinearLayout content;
    private boolean showAllMonths;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(PRIMARY_DARK);
        studentId = getIntent().getLongExtra("student_id", -1);
        if (studentId <= 0) { finish(); return; }
        database = new StudentDatabaseHelper(this);
        LinearLayout root = column();
        root.setBackgroundColor(Palette.BACKGROUND);
        root.addView(header(), new LinearLayout.LayoutParams(MATCH, dp(88)));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = column();
        content.setPadding(dp(16), dp(16), dp(16), dp(28));
        scroll.addView(content, new ScrollView.LayoutParams(MATCH, WRAP));
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        setContentView(root);
    }

    @Override public void onResume() { super.onResume(); if (content != null) render(); }

    private void render() {
        content.removeAllViews();
        Student student = database.getStudent(studentId);
        if (student == null) { finish(); return; }
        List<StudentMonthBalance> months = database.getStudentMonthlyBalances(studentId);
        StudentFinancialSummary summary = database.getFinancialSummary(studentId);
        content.addView(identityCard(student, summary), lp(MATCH, WRAP, 0, 0, 0, dp(12)));

        LinearLayout metrics1 = row();
        metrics1.addView(metric(R.drawable.ic_description_24, "Total dû", summary.dueAmount + " DH", "Montant total", ERROR, Palette.ERROR_SOFT), lp(0, WRAP, 0, 0, dp(6), 0, 1));
        metrics1.addView(metric(R.drawable.ic_credit_card_24, "Déjà payé", summary.paidAmount + " DH", percent(summary.paidAmount, summary.dueAmount) + "% du total", SUCCESS, Palette.SUCCESS_SOFT), lp(0, WRAP, dp(6), 0, 0, 0, 1));
        content.addView(metrics1);
        LinearLayout metrics2 = row();
        metrics2.addView(metric(R.drawable.ic_schedule_24, "Reste", summary.outstandingAmount + " DH", percent(summary.outstandingAmount, summary.dueAmount) + "% à payer", WARNING, Palette.WARNING_SOFT), lp(0, WRAP, 0, dp(12), dp(6), 0, 1));
        metrics2.addView(metric(R.drawable.ic_sell_24, "Avance", summary.availableAdvance + " DH", summary.availableAdvance > 0 ? "Avoir disponible" : "Aucun avoir", INFO, Palette.BLUE_SOFT), lp(0, WRAP, dp(6), dp(12), 0, 0, 1));
        content.addView(metrics2);
        content.addView(tariffCard(student), lp(MATCH, WRAP, 0, dp(12), 0, 0));

        LinearLayout pay = row();
        pay.setGravity(Gravity.CENTER);
        pay.setBackground(gradient(PRIMARY_DARK, PRIMARY, dp(14)));
        pay.addView(icon(R.drawable.ic_credit_card_24, Color.WHITE), lp(dp(28), dp(28), 0, 0, dp(12), 0));
        pay.addView(text("Saisir un paiement", 16, Color.WHITE, true));
        pay.setOnClickListener(v -> { Intent i = new Intent(this, AddPaymentActivity.class); i.putExtra("student_id", studentId); startActivity(i); });
        content.addView(pay, lp(MATCH, dp(56), 0, dp(16), 0, 0));
        content.addView(historyCard(months), lp(MATCH, WRAP, 0, dp(16), 0, 0));
        content.addView(distributionCard(student, months.isEmpty()?null:months.get(0)),lp(MATCH,WRAP,0,dp(12),0,0));
    }

    private LinearLayout identityCard(Student s, StudentFinancialSummary summary) {
        LinearLayout card = cardRow(); card.setPadding(dp(14), dp(16), dp(12), dp(16));
        TextView avatar = text(initials(s), 20, PRIMARY_DARK, true);
        avatar.setGravity(Gravity.CENTER); avatar.setBackground(round(0xffddf7f4, dp(34), 0, 0));
        card.addView(avatar, new LinearLayout.LayoutParams(dp(68), dp(68)));
        LinearLayout info = column();
        info.addView(text(s.fullName(), 19, TEXT, true));
        info.addView(text(s.level == null ? "" : s.level, 14, MUTED, false), lp(MATCH, WRAP, 0, dp(5), 0, 0));
        LinearLayout phone = row(); phone.setGravity(Gravity.CENTER_VERTICAL);
        phone.addView(icon(R.drawable.ic_call_24, MUTED), new LinearLayout.LayoutParams(dp(18), dp(18)));
        phone.addView(text(phone(s), 14, MUTED, false), lp(WRAP, WRAP, dp(7), 0, 0, 0));
        info.addView(phone, lp(MATCH, WRAP, 0, dp(7), 0, 0));
        card.addView(info, lp(0, WRAP, dp(14), 0, dp(8), 0, 1));
        boolean noDue=summary.dueAmount<=0,paid = !noDue&&summary.outstandingAmount <= 0, partial = !noDue&&summary.paidAmount > 0;
        card.addView(status(noDue?"Sans échéance":paid ? "Réglé" : partial ? "Partiel" : "Impayé", noDue?R.drawable.ic_calendar_month_24:paid ? R.drawable.ic_check_circle_24 : partial ? R.drawable.ic_schedule_24 : R.drawable.ic_error_24, noDue?MUTED:paid ? SUCCESS : partial ? WARNING : ERROR, noDue?0xfff1f5f9:paid ? Palette.SUCCESS_SOFT : partial ? Palette.WARNING_SOFT : Palette.ERROR_SOFT));
        ImageView menu=icon(R.drawable.ic_more_vert_24,MUTED);menu.setPadding(dp(3),dp(3),dp(3),dp(3));menu.setContentDescription("Actions élève");menu.setOnClickListener(v->showStudentActions(s));card.addView(menu, lp(dp(30), dp(34), dp(7), 0, 0, 0));
        return card;
    }

    private LinearLayout metric(int res, String title, String value, String subtitle, int color, int bg) {
        LinearLayout card = card(); card.setMinimumHeight(dp(124));
        LinearLayout body = row(); body.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout bubble = row(); bubble.setGravity(Gravity.CENTER); bubble.setBackground(round(bg, dp(25), 0, 0));
        bubble.addView(icon(res, color), new LinearLayout.LayoutParams(dp(25), dp(25)));
        body.addView(bubble, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout copy = column(); copy.addView(text(title, 12, MUTED, true));
        copy.addView(text(value, 18, color, true), lp(MATCH, WRAP, 0, dp(4), 0, 0));
        copy.addView(text(subtitle, 11, MUTED, false), lp(MATCH, WRAP, 0, dp(4), 0, 0));
        body.addView(copy, lp(0, WRAP, dp(9), 0, 0, 0, 1)); card.addView(body); return card;
    }

    private LinearLayout tariffCard(Student s) {
        LinearLayout card = card();
        LinearLayout heading = row(); heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(icon(R.drawable.ic_menu_book_24, PRIMARY_DARK), new LinearLayout.LayoutParams(dp(25), dp(25)));
        heading.addView(text("Tarif & matières", 17, TEXT, true), lp(0, WRAP, dp(10), 0, 0, 0, 1));
        TextView details=text("Voir les détails  ›",12,PRIMARY,true);details.setOnClickListener(v->showTariffDetails(s));heading.addView(details); card.addView(heading);
        LinearLayout body = row();
        LinearLayout subjects = column(); subjects.addView(text("Matières sélectionnées", 12, MUTED, true));
        LinearLayout chips = row(); chips.setPadding(0, dp(8), 0, 0);
        String[] vals = s.subjects == null ? new String[0] : s.subjects.split("[,·]"); int shown = 0;
        for (String raw : vals) { String value = raw.trim(); if (value.isEmpty() || shown == 3) continue; TextView chip = text(value, 11, PRIMARY_DARK, false); chip.setPadding(dp(9), dp(7), dp(9), dp(7)); chip.setBackground(round(Palette.PRIMARY_SOFT, dp(18), 0, 0)); chips.addView(chip, lp(WRAP, WRAP, 0, 0, dp(5), 0)); shown++; }
        if (shown == 0) chips.addView(text("Aucune matière", 11, MUTED, false));
        subjects.addView(chips); body.addView(subjects, lp(0, WRAP, 0, 0, dp(10), 0, 1));
        LinearLayout tariff = column(); tariff.setPadding(dp(12), 0, 0, 0);
        tariff.addView(text("Tarif mensuel", 12, MUTED, true)); tariff.addView(text(s.negotiatedAmount + " DH", 19, TEXT, true), lp(MATCH, WRAP, 0, dp(4), 0, 0));
        tariff.addView(text("Centre : " + s.centerAmount + " DH", 11, MUTED, false), lp(MATCH, WRAP, 0, dp(4), 0, 0));
        body.addView(tariff, new LinearLayout.LayoutParams(dp(125), WRAP)); card.addView(body, lp(MATCH, WRAP, 0, dp(18), 0, 0)); return card;
    }

    private LinearLayout historyCard(List<StudentMonthBalance> months) {
        LinearLayout card = card(); card.setPadding(dp(12), dp(14), dp(12), dp(8));
        LinearLayout h = row(); h.setGravity(Gravity.CENTER_VERTICAL);
        h.addView(icon(R.drawable.ic_schedule_24, PRIMARY_DARK), new LinearLayout.LayoutParams(dp(25), dp(25)));
        h.addView(text("Historique mensuel", 17, TEXT, true), lp(0, WRAP, dp(10), 0, 0, 0, 1));TextView all=text(showAllMonths?"Réduire  ↑":"Voir tout  ›",12,PRIMARY,true);all.setOnClickListener(v->{showAllMonths=!showAllMonths;render();});h.addView(all);
        card.addView(h, lp(MATCH, WRAP, dp(2), 0, dp(2), dp(10)));
        if (months.isEmpty()) { TextView e = text("Aucune mensualité disponible.", 13, MUTED, false); e.setGravity(Gravity.CENTER); e.setPadding(0, dp(22), 0, dp(22)); card.addView(e); return card; }
        int count = 0; for (StudentMonthBalance month : months) { if (!showAllMonths&&count++ == 5) break; card.addView(monthRow(month), lp(MATCH, WRAP, 0, 0, 0, dp(6))); }
        return card;
    }

    private LinearLayout monthRow(StudentMonthBalance m) {
        LinearLayout item = cardRow(); item.setPadding(dp(9), dp(9), dp(7), dp(9)); item.setBackground(round(0xfffcfdfd, dp(13), BORDER, dp(1)));
        TextView badge = text(shortMonth(m.dueMonth), 10, PRIMARY_DARK, true); badge.setGravity(Gravity.CENTER); badge.setBackground(round(0xffddf7f4, dp(24), 0, 0)); item.addView(badge, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout amount = column(); amount.addView(text(m.totalAmount + " DH", 15, TEXT, true));
        amount.addView(text(m.remainingAmount() > 0 ? "Payé " + m.paidAmount + " · reste " + m.remainingAmount() + " DH" : "Mensualité réglée", 10, MUTED, false), lp(MATCH, WRAP, 0, dp(4), 0, 0));
        item.addView(amount, lp(0, WRAP, dp(10), 0, dp(4), 0, 1));
        if (m.paidAmount >= m.totalAmount) item.addView(status("Payé", R.drawable.ic_check_circle_24, SUCCESS, Palette.SUCCESS_SOFT));
        else if (m.paidAmount == 0) item.addView(status("Impayé", R.drawable.ic_error_24, ERROR, Palette.ERROR_SOFT));
        else item.addView(status("Partiel", R.drawable.ic_schedule_24, WARNING, Palette.WARNING_SOFT));
        TextView arrow=text("›",24,MUTED,false);arrow.setGravity(Gravity.CENTER);item.addView(arrow,lp(dp(24),dp(32),dp(4),0,0,0));item.setOnClickListener(v->{if(m.remainingAmount()>0)openPayment(m.dueMonth);else Toast.makeText(this,"Cette mensualité est déjà réglée.",Toast.LENGTH_SHORT).show();}); return item;
    }

    private View distributionCard(Student s,StudentMonthBalance selected){LinearLayout card=card();LinearLayout title=row();title.setGravity(Gravity.CENTER_VERTICAL);title.addView(icon(R.drawable.ic_analytics_24,PRIMARY_DARK),new LinearLayout.LayoutParams(dp(25),dp(25)));title.addView(text("Répartition du tarif",17,TEXT,true),lp(0,WRAP,dp(10),0,0,0,1));title.addView(text(selected==null?"Mensuelle":longMonth(selected.dueMonth),12,MUTED,false));card.addView(title);LinearLayout grid=row();addPart(grid,"Math",s.mathAmount,0xffe6f7f4,PRIMARY_DARK);addPart(grid,"Français",s.frenchAmount,0xffe8f2ff,Palette.BLUE);addPart(grid,"PC",s.pcAmount,0xfff3e8ff,0xff7e22ce);addPart(grid,"SVT",s.svtAmount,0xfffff1e6,0xffc45d08);card.addView(grid,lp(MATCH,WRAP,0,dp(14),0,0));TextView center=text("Part du centre :  "+s.centerAmount+" DH     •     Total :  "+s.negotiatedAmount+" DH",12,TEXT,true);center.setPadding(dp(12),dp(11),dp(12),dp(11));center.setBackground(round(0xfff1f5f9,dp(11),0,0));card.addView(center,lp(MATCH,WRAP,0,dp(10),0,0));return card;}
    private void addPart(LinearLayout row,String label,int amount,int bg,int color){if(amount<=0)return;LinearLayout box=column();box.setGravity(Gravity.CENTER);box.setPadding(dp(5),dp(9),dp(5),dp(9));box.setBackground(round(bg,dp(11),0,0));box.addView(text(label,11,color,false));box.addView(text(amount+" DH",13,color,true),lp(MATCH,WRAP,0,dp(4),0,0));row.addView(box,lp(0,WRAP,0,0,dp(6),0,1));}

    private void showStudentActions(Student student){java.util.ArrayList<String> choices=new java.util.ArrayList<>();choices.add("Saisir un paiement");String number=phone(student);if(!"Téléphone non renseigné".equals(number))choices.add("Appeler "+number);new AlertDialog.Builder(this).setTitle(student.fullName()).setItems(choices.toArray(new String[0]),(d,which)->{if(which==0)openPayment(null);else startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+number.replace(" ",""))));}).show();}
    private void showTariffDetails(Student s){new AlertDialog.Builder(this).setTitle("Tarif de "+s.fullName()).setMessage("Niveau : "+s.level+"\nMatières : "+s.subjects+"\n\n"+distributionText(s)+"\n\nTotal mensuel : "+s.negotiatedAmount+" DH").setPositiveButton("Fermer",null).show();}
    private String distributionText(Student s){java.util.ArrayList<String> p=new java.util.ArrayList<>();if(s.mathAmount>0)p.add("Math : "+s.mathAmount+" DH");if(s.frenchAmount>0)p.add("Français : "+s.frenchAmount+" DH");if(s.pcAmount>0)p.add("PC : "+s.pcAmount+" DH");if(s.svtAmount>0)p.add("SVT : "+s.svtAmount+" DH");p.add("Centre : "+s.centerAmount+" DH");return android.text.TextUtils.join("\n",p);}
    private void openPayment(String dueMonth){Intent i=new Intent(this,AddPaymentActivity.class);i.putExtra("student_id",studentId);if(dueMonth!=null)i.putExtra("reference_month",dueMonth);startActivity(i);}

    private LinearLayout status(String label, int res, int color, int bg) {
        LinearLayout b = row(); b.setGravity(Gravity.CENTER); b.setPadding(dp(8), dp(7), dp(8), dp(7)); b.setBackground(round(bg, dp(20), 0, 0));
        b.addView(icon(res, color), new LinearLayout.LayoutParams(dp(18), dp(18))); b.addView(text(label, 11, color, true), lp(WRAP, WRAP, dp(5), 0, 0, 0)); return b;
    }

    private View header() {
        LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(16), 0, dp(16), 0); bar.setBackground(gradient(PRIMARY_DARK, PRIMARY, 0));
        ImageView back = icon(R.drawable.ic_arrow_back_24, Color.WHITE); back.setPadding(dp(4), dp(4), dp(4), dp(4)); back.setOnClickListener(v -> finish()); bar.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));
        LinearLayout copy=column();copy.addView(text("Fiche élève",21,Color.WHITE,true));copy.addView(text("Situation financière et mensualités",12,0xffd7efed,false),lp(MATCH,WRAP,0,dp(3),0,0));bar.addView(copy,lp(0,WRAP,dp(12),0,0,0,1));bar.addView(text("CentrePay",17,Color.WHITE,true));return bar;
    }

    private String initials(Student s) { String f = s.firstName == null ? "" : s.firstName.trim(), l = s.lastName == null ? "" : s.lastName.trim(); String x = (f.isEmpty() ? "" : f.substring(0,1)) + (l.isEmpty() ? "" : l.substring(0,1)); return x.isEmpty() ? "ÉL" : x.toUpperCase(); }
    private String phone(Student s) { if (s.phone != null && !s.phone.trim().isEmpty()) return s.phone; if (s.parentPhone != null && !s.parentPhone.trim().isEmpty()) return s.parentPhone; return "Téléphone non renseigné"; }
    private int percent(int value, int total) { return total <= 0 ? 0 : Math.round(value * 100f / total); }
    private String shortMonth(String v) { if (v == null || !v.matches("\\d{4}-(0[1-9]|1[0-2])")) return v == null ? "" : v; String[] n = {"JAN","FÉV","MAR","AVR","MAI","JUN","JUL","AOÛ","SEP","OCT","NOV","DÉC"}; return n[Integer.parseInt(v.substring(5))-1] + "\n" + v.substring(0,4); }
    private String longMonth(String v){if(v==null||!v.matches("\\d{4}-(0[1-9]|1[0-2])"))return "Mensuelle";String[] n={"Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"};return n[Integer.parseInt(v.substring(5))-1]+" "+v.substring(0,4);}
    private LinearLayout card() { LinearLayout v = column(); v.setPadding(dp(14), dp(14), dp(14), dp(14)); v.setBackground(round(SURFACE, dp(16), BORDER, dp(1))); v.setElevation(dp(1)); return v; }
    private LinearLayout cardRow() { LinearLayout v = row(); v.setGravity(Gravity.CENTER_VERTICAL); v.setPadding(dp(14), dp(14), dp(14), dp(14)); v.setBackground(round(SURFACE, dp(16), BORDER, dp(1))); v.setElevation(dp(1)); return v; }
    private ImageView icon(int res, int color) { ImageView v = new ImageView(this); v.setImageResource(res); v.setImageTintList(ColorStateList.valueOf(color)); v.setScaleType(ImageView.ScaleType.CENTER_INSIDE); v.setContentDescription(""); return v; }
    private TextView text(String value, int size, int color, boolean bold) { TextView v = new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color); v.setIncludeFontPadding(false); if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); return v; }
    private GradientDrawable round(int color, int radius, int stroke, int width) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if (width > 0) d.setStroke(width, stroke); return d; }
    private GradientDrawable gradient(int start, int end, int radius) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start,end}); d.setCornerRadius(radius); return d; }
    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h); p.setMargins(l,t,r,b); return p; }
    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b, float weight) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h,weight); p.setMargins(l,t,r,b); return p; }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT, WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
}
