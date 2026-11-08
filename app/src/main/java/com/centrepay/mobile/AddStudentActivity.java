package com.centrepay.mobile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** New-student form matching the approved premium mockup, with real tariff selection logic. */
public class AddStudentActivity extends Activity {
    private static final int BROWN = Palette.PRIMARY_DARK, BROWN_LIGHT = Palette.PRIMARY, TERRACOTTA = Palette.PRIMARY, TERRACOTTA_SOFT = Palette.PRIMARY_SOFT, CREAM = Palette.BACKGROUND, SURFACE = Palette.SURFACE, TEXT = Palette.TEXT, MUTED = Palette.MUTED, BORDER = Palette.BORDER;
    private StudentDatabaseHelper database;
    private EditText lastName, firstName, phone, parentPhone, enrollmentDate;
    private LinearLayout levelChips, subjectChips, tariffArea;
    private String selectedLevel = "1AC";
    private boolean mathSelected, frenchSelected, pcSelected, svtSelected;
    private List<TariffOption> availableTariffs = new ArrayList<>();
    private int selectedTariffIndex = -1;
    private TextView saveButton;
    private boolean saving;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); getWindow().setStatusBarColor(BROWN); getWindow().setNavigationBarColor(CREAM); database = new StudentDatabaseHelper(this);
        LinearLayout root = column(); root.setBackgroundColor(CREAM); root.addView(createHero(), new LinearLayout.LayoutParams(MATCH, dp(88)));
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); LinearLayout form = column(); form.setPadding(dp(16), dp(16), dp(16), dp(28));
        TextView required=text("*  Les champs marqués sont obligatoires.",12,MUTED,false);form.addView(required,margins(MATCH,WRAP,dp(4),0,0,dp(12)));

        LinearLayout identity = section(R.drawable.ic_person_24, "Identité", "Informations personnelles de l’élève", 0xffddf7f4, BROWN);
        LinearLayout names = row();
        firstName = input("Prénom *", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS); names.addView(firstName, margins(0, dp(58), 0, dp(16), dp(6), 0, 1));
        lastName = input("Nom *", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS); names.addView(lastName, margins(0, dp(58), dp(6), dp(16), 0, 0, 1)); identity.addView(names); form.addView(identity);

        LinearLayout contact = section(R.drawable.ic_call_24, "Contact", "Coordonnées pour la communication", 0xffddf7f4, BROWN);
        LinearLayout phones = row();
        phone = input("Téléphone élève", InputType.TYPE_CLASS_PHONE); phones.addView(phone, margins(0, dp(58), 0, dp(16), dp(6), 0, 1));
        parentPhone = input("Téléphone parent *", InputType.TYPE_CLASS_PHONE); phones.addView(parentPhone, margins(0, dp(58), dp(6), dp(16), 0, 0, 1)); contact.addView(phones); form.addView(contact, margins(MATCH, WRAP, 0, dp(12), 0, 0));

        LinearLayout academics = section(R.drawable.ic_school_24, "Niveau & matières", "Sélectionnez le niveau et les matières suivies", 0xfffff3d6, Color.rgb(146, 94, 0));
        academics.addView(label("Niveau *"), margins(MATCH, WRAP, 0, dp(16), 0, dp(8))); levelChips = row(); academics.addView(levelChips, new LinearLayout.LayoutParams(MATCH, dp(42))); renderLevelChips();
        academics.addView(label("Matières *"), margins(MATCH, WRAP, 0, dp(16), 0, dp(8))); subjectChips = column(); academics.addView(subjectChips); renderSubjectChips(); form.addView(academics, margins(MATCH, WRAP, 0, dp(12), 0, 0));

        LinearLayout tariff = section(R.drawable.ic_sell_24, "Tarif", "Choisissez le pack et la date d’inscription", 0xfffff3d6, Color.rgb(146, 94, 0));
        tariffArea = column(); tariff.addView(tariffArea, margins(MATCH, WRAP, 0, dp(16), 0, 0)); loadTariffOptions();
        enrollmentDate = input("Date d’inscription *", InputType.TYPE_CLASS_TEXT); enrollmentDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date())); makeDatePicker(enrollmentDate); tariff.addView(enrollmentDate, margins(MATCH, dp(58), 0, dp(12), 0, 0)); form.addView(tariff, margins(MATCH, WRAP, 0, dp(12), 0, 0));

        saveButton = text("▣     Enregistrer l’élève", 16, SURFACE, true); saveButton.setGravity(Gravity.CENTER); saveButton.setBackground(gradient(BROWN, BROWN_LIGHT, dp(12))); saveButton.setOnClickListener(view -> saveStudent()); form.addView(saveButton, margins(MATCH, dp(56), 0, dp(16), 0, 0));
        TextView cancel = text("Annuler", 15, BROWN, true); cancel.setGravity(Gravity.CENTER); cancel.setBackground(rounded(Color.TRANSPARENT, dp(12), BROWN, dp(1))); cancel.setOnClickListener(v -> finish()); form.addView(cancel, margins(MATCH, dp(52), 0, dp(8), 0, 0));
        scroll.addView(form, new ScrollView.LayoutParams(MATCH, WRAP)); root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f)); setContentView(root);
    }

    @Override protected void onResume(){super.onResume();if(tariffArea!=null)loadTariffOptions();}

    private View createHero() {
        FrameLayout hero = new FrameLayout(this); hero.setBackground(gradient(BROWN, BROWN_LIGHT, 0));
        LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(16), 0, dp(16), 0); hero.addView(bar, new FrameLayout.LayoutParams(MATCH, MATCH));
        ImageView back = icon(R.drawable.ic_arrow_back_24, Color.WHITE); back.setPadding(dp(4), dp(4), dp(4), dp(4)); back.setOnClickListener(v -> finish()); bar.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));
        LinearLayout titles = column(); titles.addView(text("Nouvel élève", 20, Color.WHITE, true)); titles.addView(text("Ajouter un élève à votre centre", 12, 0xffd7efed, false), margins(MATCH, WRAP, 0, dp(3), 0, 0)); bar.addView(titles, margins(0, WRAP, dp(12), 0, 0, 0, 1));bar.addView(text("CentrePay",17,Color.WHITE,true)); return hero;
    }

    private void renderLevelChips() { levelChips.removeAllViews(); addLevelChip("1AC"); addLevelChip("2AC"); addLevelChip("3AC"); }
    private void addLevelChip(String level) { boolean selected = selectedLevel.equals(level); TextView chip = text(level, 16, selected ? SURFACE : MUTED, true); chip.setGravity(Gravity.CENTER); chip.setBackground(rounded(selected ? BROWN : Color.rgb(239, 233, 225), dp(20), Color.TRANSPARENT, 0)); chip.setOnClickListener(view -> { selectedLevel = level; renderLevelChips(); loadTariffOptions(); }); levelChips.addView(chip, margins(0, dp(44), 0, 0, dp(10), 0, 1f)); }

    private void renderSubjectChips() {
        subjectChips.removeAllViews(); LinearLayout first = row(); first.addView(subjectChip("✓  Mathématiques", mathSelected, view -> { mathSelected = !mathSelected; refreshSubjectsAndTariffs(); }), margins(0, dp(46), 0, 0, dp(9), 0, 1.25f)); first.addView(subjectChip("✓  Français", frenchSelected, view -> { frenchSelected = !frenchSelected; refreshSubjectsAndTariffs(); }), margins(0, dp(46), dp(9), 0, 0, 0, 1f)); subjectChips.addView(first, new LinearLayout.LayoutParams(MATCH, WRAP));
        LinearLayout second = row(); second.addView(subjectChip("✓  PC", pcSelected, view -> { pcSelected = !pcSelected; refreshSubjectsAndTariffs(); }), margins(0, dp(46), 0, dp(10), dp(9), 0, .65f)); second.addView(subjectChip("✓  SVT", svtSelected, view -> { svtSelected = !svtSelected; refreshSubjectsAndTariffs(); }), margins(0, dp(46), dp(9), dp(10), 0, 0, .8f)); View spacer = new View(this); second.addView(spacer, new LinearLayout.LayoutParams(0, 1, .55f)); subjectChips.addView(second, new LinearLayout.LayoutParams(MATCH, WRAP));
    }

    private TextView subjectChip(String label, boolean selected, View.OnClickListener click) { TextView chip = text((selected ? "✓" : "○") + label.substring(1), 15, selected ? TERRACOTTA : MUTED, true); chip.setGravity(Gravity.CENTER); chip.setBackground(rounded(selected ? TERRACOTTA_SOFT : Color.rgb(239, 236, 232), dp(16), Color.TRANSPARENT, 0)); chip.setOnClickListener(click); return chip; }
    private void refreshSubjectsAndTariffs() { renderSubjectChips(); loadTariffOptions(); }

    private void loadTariffOptions() { String packKey = selectedPackKey(); availableTariffs = packKey.isEmpty() ? new ArrayList<>() : database.getTariffOptions(selectedLevel, packKey); selectedTariffIndex = availableTariffs.isEmpty() ? -1 : 0; renderTariffArea(packKey); }
    private void renderTariffArea(String packKey) {
        tariffArea.removeAllViews();
        if (packKey.isEmpty()) { tariffArea.addView(messageCard("Choisissez au moins une matière pour afficher les formules compatibles.")); return; }
        if (availableTariffs.isEmpty()) { tariffArea.addView(messageCard("Aucun tarif ne correspond à "+selectedLevel+" — "+TariffOption.packLabel(packKey)+"."));TextView manage=text("Gérer les tarifs  →",14,BROWN_LIGHT,true);manage.setGravity(Gravity.CENTER);manage.setBackground(rounded(TERRACOTTA_SOFT,dp(12),Color.TRANSPARENT,0));manage.setOnClickListener(v->startActivity(new Intent(this,TariffActivity.class)));tariffArea.addView(manage,margins(MATCH,dp(48),0,dp(9),0,0)); return; }
        for (int index = 0; index < availableTariffs.size(); index++) tariffArea.addView(tariffCard(availableTariffs.get(index), index), margins(MATCH, WRAP, 0, index == 0 ? 0 : dp(10), 0, 0));
    }

    private View tariffCard(TariffOption option, int index) {
        boolean selected = index == selectedTariffIndex; LinearLayout card = column(); card.setPadding(dp(16), dp(16), dp(16), dp(16)); card.setClickable(true); card.setOnClickListener(view -> { selectedTariffIndex = index; renderTariffArea(option.packKey); }); card.setBackground(rounded(SURFACE, dp(20), selected ? TERRACOTTA : BORDER, selected ? dp(2) : dp(1))); card.setElevation(dp(3));
        LinearLayout top = row(); top.setGravity(Gravity.CENTER_VERTICAL); top.addView(text(index == 0 ? "Formule recommandée" : "Autre formule", 14, TERRACOTTA, true), new LinearLayout.LayoutParams(0, WRAP, 1f)); if (selected) { TextView badge = text("✓  Sélectionnée", 12, TERRACOTTA, true); badge.setPadding(dp(10), dp(5), dp(10), dp(5)); badge.setBackground(rounded(TERRACOTTA_SOFT, dp(14), Color.TRANSPARENT, 0)); top.addView(badge); } card.addView(top); card.addView(text(option.studentAmount + " DH / mois", 27, TEXT, true), margins(MATCH, WRAP, 0, dp(9), 0, 0));card.addView(text(TariffOption.packLabel(option.packKey)+" · "+option.level,13,TEXT,true),margins(MATCH,WRAP,0,dp(5),0,0)); card.addView(text(distributionText(option), 13, MUTED, false), margins(MATCH, WRAP, 0, dp(5), 0, 0)); return card;
    }

    private String distributionText(TariffOption option) { ArrayList<String> parts = new ArrayList<>(); if (option.mathAmount > 0) parts.add("Math " + option.mathAmount); if (option.frenchAmount > 0) parts.add("Français " + option.frenchAmount); if (option.pcAmount > 0) parts.add("PC " + option.pcAmount); if (option.svtAmount > 0) parts.add("SVT " + option.svtAmount); parts.add("Centre " + option.centerAmount); return android.text.TextUtils.join(" · ", parts) + " DH"; }
    private View messageCard(String message) { TextView view = text(message, 14, MUTED, false); view.setPadding(dp(16), dp(20), dp(16), dp(20)); view.setBackground(rounded(SURFACE, dp(18), BORDER, dp(1))); return view; }

    private LinearLayout section(int iconResource, String title, String subtitle, int iconBackground, int iconColor) {
        LinearLayout card = column(); card.setPadding(dp(12), dp(14), dp(12), dp(14)); card.setBackground(rounded(SURFACE, dp(16), BORDER, dp(1))); card.setElevation(dp(1));
        LinearLayout heading = row(); heading.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout bubble = row(); bubble.setGravity(Gravity.CENTER); bubble.setBackground(rounded(iconBackground, dp(23), Color.TRANSPARENT, 0)); bubble.addView(icon(iconResource, iconColor), new LinearLayout.LayoutParams(dp(24), dp(24))); heading.addView(bubble, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout copy = column(); copy.addView(text(title, 17, TEXT, true)); copy.addView(text(subtitle, 12, MUTED, false), margins(MATCH, WRAP, 0, dp(4), 0, 0)); heading.addView(copy, margins(0, WRAP, dp(12), 0, 0, 0, 1)); card.addView(heading); return card;
    }

    private void saveStudent() {
        if(saving)return;clearErrors();
        String surname = lastName.getText().toString().trim(), givenName = firstName.getText().toString().trim(); ArrayList<String> subjects = new ArrayList<>(); if (mathSelected) subjects.add("Math"); if (frenchSelected) subjects.add("Français"); if (pcSelected) subjects.add("PC"); if (svtSelected) subjects.add("SVT");
        if (givenName.isEmpty()) { firstName.setError("Prénom obligatoire");firstName.requestFocus(); return; }
        if (surname.isEmpty()) { lastName.setError("Nom obligatoire");lastName.requestFocus(); return; }
        String parentNumber=parentPhone.getText().toString().trim();if (parentNumber.isEmpty()) { parentPhone.setError("Téléphone du parent obligatoire");parentPhone.requestFocus(); return; }
        if (digits(parentNumber).length()<9) { parentPhone.setError("Numéro de téléphone incomplet");parentPhone.requestFocus(); return; }
        String studentNumber=phone.getText().toString().trim();if(!studentNumber.isEmpty()&&digits(studentNumber).length()<9){phone.setError("Numéro de téléphone incomplet");phone.requestFocus();return;}
        if (subjects.isEmpty()) { Toast.makeText(this, "Choisissez au moins une matière.", Toast.LENGTH_SHORT).show(); return; }
        TariffOption option = selectedTariff(); if (option == null) { Toast.makeText(this, "Choisissez un tarif disponible pour ce pack.", Toast.LENGTH_LONG).show(); return; }
        if(enrollmentDate.getText().toString().trim().isEmpty()){enrollmentDate.setError("Date d’inscription obligatoire");return;}
        saving=true;saveButton.setEnabled(false);saveButton.setAlpha(.65f);saveButton.setText("Enregistrement…");
        Map<String, Teacher> teachers = database.getTariffTeacherAssignments(option.id); long id = database.addStudent(surname, givenName, phone.getText().toString(), parentPhone.getText().toString(), selectedLevel, selectedPackKey(), enrollmentDate.getText().toString(), android.text.TextUtils.join(", ", subjects), option.studentAmount, option.mathAmount, option.frenchAmount, option.pcAmount, option.svtAmount, option.centerAmount, teacherId(teachers, "math"), teacherId(teachers, "french"), teacherId(teachers, "pc"), teacherId(teachers, "svt"));
        if (id > 0) { Toast.makeText(this, "Élève enregistré avec succès.", Toast.LENGTH_SHORT).show(); finish(); } else {saving=false;saveButton.setEnabled(true);saveButton.setAlpha(1f);saveButton.setText("▣     Enregistrer l’élève");Toast.makeText(this, "Impossible d’enregistrer l’élève.", Toast.LENGTH_SHORT).show();}
    }

    private void clearErrors(){firstName.setError(null);lastName.setError(null);phone.setError(null);parentPhone.setError(null);enrollmentDate.setError(null);}
    private String digits(String value){return value.replaceAll("[^0-9]","");}

    private TariffOption selectedTariff() { return selectedTariffIndex >= 0 && selectedTariffIndex < availableTariffs.size() ? availableTariffs.get(selectedTariffIndex) : null; }
    private long teacherId(Map<String, Teacher> teachers, String subjectKey) { Teacher teacher = teachers.get(subjectKey); return teacher == null ? 0 : teacher.id; }
    private String selectedPackKey() { return TariffOption.packKey(mathSelected, frenchSelected, pcSelected, svtSelected); }
    private void makeDatePicker(EditText field) { field.setFocusable(false); field.setClickable(true); field.setOnClickListener(view -> { Calendar current = Calendar.getInstance(); new DatePickerDialog(this, (picker, year, month, day) -> field.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year)), current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show(); }); }
    private void addField(LinearLayout form, String title, EditText field) { form.addView(label(title), margins(MATCH, WRAP, 0, dp(19), 0, dp(8))); form.addView(field, new LinearLayout.LayoutParams(MATCH, dp(64))); }
    private EditText input(String hint, int type) { EditText field = new EditText(this); field.setHint(hint); field.setHintTextColor(MUTED); field.setTextColor(TEXT); field.setTextSize(15); field.setSingleLine(true); field.setInputType(type); field.setPadding(dp(14), 0, dp(14), 0); field.setBackground(rounded(SURFACE, dp(12), BORDER, dp(1))); return field; }
    private TextView label(String value) { return text(value, 16, TEXT, true); }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setIncludeFontPadding(false); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout row() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL); return view; }
    private ImageView icon(int resource, int color) { ImageView view = new ImageView(this); view.setImageResource(resource); view.setImageTintList(ColorStateList.valueOf(color)); view.setScaleType(ImageView.ScaleType.CENTER_INSIDE); view.setContentDescription(""); return view; }
    private GradientDrawable rounded(int color, int radius, int stroke, int width) { GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color); drawable.setCornerRadius(radius); if (width > 0) drawable.setStroke(width, stroke); return drawable; }
    private GradientDrawable gradient(int start, int end, int radius) { GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end}); drawable.setCornerRadius(radius); return drawable; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height); params.setMargins(left, top, right, bottom); return params; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom, float weight) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight); params.setMargins(left, top, right, bottom); return params; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT, WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
}
