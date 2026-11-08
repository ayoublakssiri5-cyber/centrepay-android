package com.centrepay.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTeacherSettlementActivity extends Activity {
    public static final String EXTRA_TEACHER_ID = "teacher_id";
    public static final String EXTRA_DUE_MONTH = "due_month";
    private static final int NAVY=Palette.PRIMARY_DARK,TEAL=Palette.PRIMARY,TEXT=Palette.TEXT,MUTED=Palette.MUTED,SURFACE=Palette.SURFACE,BORDER=Palette.BORDER,BG=Palette.BACKGROUND,GREEN=Palette.SUCCESS,GOLD=Palette.ACCENT_TEXT;

    private StudentDatabaseHelper database;
    private long teacherId;
    private String dueMonth;
    private TeacherBalance balance;
    private EditText paymentDate;
    private EditText referenceMonth;
    private EditText note;
    private TextView saveButton;
    private boolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new StudentDatabaseHelper(this);
        teacherId = getIntent().getLongExtra(EXTRA_TEACHER_ID, -1);
        dueMonth = getIntent().getStringExtra(EXTRA_DUE_MONTH);
        if (!validStoredMonth(dueMonth)) dueMonth = String.format(Locale.US, "%04d-%02d", Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1);
        balance = database.getTeacherBalance(teacherId, dueMonth);
        if (balance == null) {
            Toast.makeText(this, "Professeur introuvable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(BG);LinearLayout root = column();root.setBackgroundColor(BG);root.addView(hero(),new LinearLayout.LayoutParams(MATCH,dp(104)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = column();
        content.setPadding(dp(16),dp(16),dp(16),dp(28));content.addView(selectionCard(),margins(MATCH,WRAP,0,0,0,dp(14)));content.addView(summaryCard(),margins(MATCH,WRAP,0,0,0,dp(14)));
        buildForm(content);
        scroll.addView(content, new ScrollView.LayoutParams(MATCH, WRAP));
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        setContentView(root);
    }

    private void buildForm(LinearLayout content) {
        LinearLayout details=card();LinearLayout title=row();title.setGravity(Gravity.CENTER_VERTICAL);title.addView(icon(R.drawable.ic_calendar_month_24,NAVY),new LinearLayout.LayoutParams(dp(26),dp(26)));title.addView(text("Détails du versement",19,TEXT,true),margins(WRAP,WRAP,dp(10),0,0,0));details.addView(title,margins(MATCH,WRAP,0,0,0,dp(8)));
        paymentDate = input("jj/mm/aaaa", InputType.TYPE_CLASS_TEXT);
        paymentDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
        makeDatePicker(paymentDate);
        addField(details, "Date du versement *", paymentDate);
        referenceMonth = input("Mois des cours", InputType.TYPE_CLASS_TEXT);
        referenceMonth.setText(monthLabel(dueMonth));
        referenceMonth.setFocusable(false);
        referenceMonth.setClickable(false);
        note = input("Ex. règlement de septembre", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        note.setSingleLine(false);
        note.setMinLines(2);
        addField(details, "Note (facultative)", note);content.addView(details,margins(MATCH,WRAP,0,0,0,dp(14)));

        LinearLayout confirmation=card();confirmation.addView(text("✓  Confirmation",18,TEXT,true));confirmation.addView(text("Vérifiez les informations avant de confirmer le versement.",13,MUTED,false),margins(MATCH,WRAP,0,dp(4),0,dp(10)));TextView recap=text("Récapitulatif du versement\n\nProfesseur          "+balance.teacher.fullName+"\nMois                     "+monthLabel(dueMonth)+"\nMontant à verser   "+balance.remainingAmount+" DH",14,TEXT,false);recap.setPadding(dp(14),dp(14),dp(14),dp(14));recap.setBackground(rounded(Color.rgb(236,253,245),dp(14),Color.TRANSPARENT,0));confirmation.addView(recap);content.addView(confirmation,margins(MATCH,WRAP,0,0,0,dp(14)));

        saveButton = text("Verser " + balance.remainingAmount + " DH", 15, SURFACE, true);
        saveButton.setGravity(Gravity.CENTER);saveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_24,0,0,0);saveButton.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));saveButton.setCompoundDrawablePadding(dp(10));saveButton.setBackground(rounded(TEAL,dp(14),TEAL,0));
        saveButton.setOnClickListener(view -> validateAndConfirm());
        content.addView(saveButton, margins(MATCH, dp(52), 0, dp(24), 0, 0));
    }

    private View selectionCard(){LinearLayout card=card();LinearLayout teacher=row();teacher.setGravity(Gravity.CENTER_VERTICAL);teacher.addView(iconCircle(R.drawable.ic_person_24,GOLD,Palette.WARNING_SOFT),new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout tc=column();tc.addView(text("Professeur",17,TEXT,true));tc.addView(text(balance.teacher.fullName,16,TEXT,false));tc.addView(text(balance.teacher.subjects+" · "+balance.teacher.level,12,MUTED,false));teacher.addView(tc,margins(0,WRAP,dp(12),0,0,0,1));card.addView(teacher);LinearLayout month=row();month.setGravity(Gravity.CENTER_VERTICAL);month.addView(iconCircle(R.drawable.ic_calendar_month_24,NAVY,Color.rgb(221,243,238)),new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout mc=column();mc.addView(text("Mois",17,TEXT,true));mc.addView(text(monthLabel(dueMonth),16,TEXT,false));month.addView(mc,margins(0,WRAP,dp(12),0,0,0,1));card.addView(month,margins(MATCH,WRAP,0,dp(16),0,0));return card;}

    private void validateAndConfirm() {
        String date = paymentDate.getText().toString().trim();
        if (!validDate(date)) {
            paymentDate.setError("Date invalide (jj/mm/aaaa)");
            return;
        }
        paymentDate.setError(null);
        TeacherBalance latest = database.getTeacherBalance(teacherId, dueMonth);
        if (latest == null || latest.remainingAmount <= 0) {
            Toast.makeText(this, "Ce salaire est déjà réglé.", Toast.LENGTH_LONG).show();
            return;
        }
        balance = latest;
        new AlertDialog.Builder(this)
                .setTitle("Confirmer le versement")
                .setMessage("Professeur : " + balance.teacher.fullName + "\nMois : " + monthLabel(dueMonth) + "\nMontant : " + balance.remainingAmount + " DH\nDate : " + date)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Confirmer", (dialog, which) -> saveSettlement())
                .show();
    }

    private void saveSettlement() {
        if (saving) return;
        TeacherBalance latest = database.getTeacherBalance(teacherId, dueMonth);
        if (latest == null || latest.remainingAmount <= 0) {
            Toast.makeText(this, "Ce salaire est déjà réglé.", Toast.LENGTH_LONG).show();
            return;
        }
        balance = latest;
        int paid = balance.remainingAmount;
        saving = true;
        saveButton.setEnabled(false);
        saveButton.setAlpha(.65f);
        saveButton.setText("Enregistrement…");
        if (database.recordTeacherSettlement(teacherId, paid, paymentDate.getText().toString().trim(), dueMonth, note.getText().toString().trim()) < 0) {
            saving = false;
            saveButton.setEnabled(true);
            saveButton.setAlpha(1f);
            saveButton.setText("Verser " + paid + " DH");
            Toast.makeText(this, "Ce mois a déjà été versé ou son solde a changé.", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Versement de " + paid + " DH enregistré.", Toast.LENGTH_LONG).show();
        finish();
    }

    private View summaryCard() {
        LinearLayout card=card();LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.addView(icon(R.drawable.ic_equalizer_24,TEAL),new LinearLayout.LayoutParams(dp(28),dp(28)));head.addView(text("Synthèse financière",20,TEXT,true),margins(WRAP,WRAP,dp(9),0,0,0));card.addView(head,margins(MATCH,WRAP,0,0,0,dp(12)));LinearLayout first=row();first.addView(metric("Salaire du mois",balance.earnedAmount,NAVY,Palette.PRIMARY_SOFT),new LinearLayout.LayoutParams(0,dp(92),1));first.addView(metric("Déjà versé",balance.paidAmount,GREEN,Color.rgb(236,253,245)),margins(0,dp(92),dp(8),0,0,0,1));card.addView(first);LinearLayout second=row();second.addView(metric("Reste à verser",balance.remainingAmount,GOLD,Color.rgb(254,247,224)),new LinearLayout.LayoutParams(0,dp(92),1));second.addView(metric("Mois concerné",0,NAVY,Palette.BLUE_SOFT),margins(0,dp(92),dp(8),0,0,0,1));card.addView(second,margins(MATCH,WRAP,0,dp(8),0,0));String components=componentText();if(!components.isEmpty()){TextView breakdown=text("Répartition du salaire  •  "+components,12,NAVY,true);breakdown.setPadding(dp(12),dp(10),dp(12),dp(10));breakdown.setBackground(rounded(Color.rgb(237,246,245),dp(12),0,0));card.addView(breakdown,margins(MATCH,WRAP,0,dp(10),0,0));}
        return card;
    }
    private View metric(String label,int amount,int color,int background){LinearLayout box=column();box.setGravity(Gravity.CENTER_VERTICAL);box.setPadding(dp(14),dp(10),dp(14),dp(10));box.setBackground(rounded(background,dp(14),0,0));box.addView(text(label,13,color,false));box.addView(text(label.startsWith("Mois")?monthLabel(dueMonth):amount+" DH",19,TEXT,true));return box;}

    private String componentText() {
        StringBuilder value = new StringBuilder();
        appendComponent(value, "Math", balance.mathAmount);
        appendComponent(value, "Français", balance.frenchAmount);
        appendComponent(value, "PC", balance.pcAmount);
        appendComponent(value, "SVT", balance.svtAmount);
        return value.toString();
    }

    private void appendComponent(StringBuilder value, String label, int amount) {
        if (amount <= 0) return;
        if (value.length() > 0) value.append(" · ");
        value.append(label).append(" ").append(amount).append(" DH");
    }

    private boolean validDate(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            format.setLenient(false);
            format.parse(value);
            return true;
        } catch (Exception error) { return false; }
    }

    private boolean validMonth(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/yyyy", Locale.US);
            format.setLenient(false);
            format.parse(value);
            return value.matches("(0[1-9]|1[0-2])/\\d{4}");
        } catch (Exception error) { return false; }
    }

    private boolean validStoredMonth(String value) { return value != null && value.matches("\\d{4}-(0[1-9]|1[0-2])"); }
    private String monthLabel(String value) { String[] parts = value.split("-"); if (parts.length != 2) return value; String[] names = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"}; try { return names[Integer.parseInt(parts[1]) - 1] + " " + parts[0]; } catch (Exception error) { return value; } }

    private void makeDatePicker(EditText field) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(view -> {
            Calendar selected = dateFromText(field.getText().toString());
            DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) -> field.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year)), selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private Calendar dateFromText(String value) {
        try { SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US); format.setLenient(false); Calendar calendar = Calendar.getInstance(); calendar.setTime(format.parse(value)); return calendar; } catch (Exception error) { return Calendar.getInstance(); }
    }

    private void addField(LinearLayout content, String title, EditText field) { content.addView(label(title), margins(MATCH, WRAP, 0, dp(18), 0, dp(6))); content.addView(field, new LinearLayout.LayoutParams(MATCH, title.startsWith("Note") ? dp(82) : dp(50))); }
    private EditText input(String hint, int inputType) { EditText field = new EditText(this); field.setHint(hint); field.setTextSize(14); field.setInputType(inputType); field.setPadding(dp(14), 0, dp(14), 0); field.setBackground(rounded(SURFACE, dp(12), BORDER, dp(1))); return field; }
    private TextView label(String value) { return text(value, 13, TEXT, true); }
    private View hero(){LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(dp(16),dp(10),dp(18),dp(10));GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{NAVY,TEAL});g.setCornerRadii(new float[]{0,0,0,0,dp(18),dp(18),dp(18),dp(18)});h.setBackground(g);ImageView back=icon(R.drawable.ic_arrow_back_24,Color.WHITE);back.setPadding(dp(10),dp(10),dp(10),dp(10));back.setOnClickListener(v->finish());h.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout title=column();title.addView(text("Nouveau règlement",21,Color.WHITE,true));title.addView(text("Verser le salaire d’un professeur",14,Color.rgb(220,238,237),false));h.addView(title,new LinearLayout.LayoutParams(0,WRAP,1));return h;}
    private LinearLayout card(){LinearLayout card=column();card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setElevation(dp(2));card.setBackground(rounded(SURFACE,dp(16),Color.TRANSPARENT,0));return card;}
    private FrameLayout iconCircle(int res,int color,int background){FrameLayout circle=new FrameLayout(this);circle.setBackground(rounded(background,dp(24),0,0));circle.addView(icon(res,color),new FrameLayout.LayoutParams(dp(24),dp(24),Gravity.CENTER));return circle;}
    private ImageView icon(int resource,int color){ImageView image=new ImageView(this);image.setImageResource(resource);image.setColorFilter(color);image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);return image;}
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setIncludeFontPadding(false); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout row() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL); return view; }
    private GradientDrawable rounded(int color, int radius, int stroke, int width) { GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color); drawable.setCornerRadius(radius); if (width > 0) drawable.setStroke(width, stroke); return drawable; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height); params.setMargins(left, top, right, bottom); return params; }
    private LinearLayout.LayoutParams margins(int width,int height,int left,int top,int right,int bottom,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height,weight);p.setMargins(left,top,right,bottom);return p;}
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
}
