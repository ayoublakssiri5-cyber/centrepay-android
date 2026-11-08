package com.centrepay.mobile;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AddTeacherActivity extends Activity {
    private static final int BROWN = Palette.PRIMARY, TERRACOTTA = Palette.PRIMARY_DARK;
    private static final int CREAM = Palette.BACKGROUND, SURFACE = Palette.SURFACE;
    private static final int TEXT = Palette.TEXT, MUTED = Palette.MUTED, BORDER = Palette.BORDER;
    private EditText fullName, phone;
    private CheckBox math, french, pc, svt, level1ac, level2ac, level3ac, levelTc;
    private StudentDatabaseHelper database;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BROWN);
        database = new StudentDatabaseHelper(this);
        getWindow().setNavigationBarColor(CREAM);
        LinearLayout root = column(); root.setBackgroundColor(CREAM);
        root.addView(hero(), new LinearLayout.LayoutParams(MATCH, dp(108)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = column(); form.setPadding(dp(16), dp(18), dp(16), dp(26));
        LinearLayout intro=row();FrameLayout personCircle=iconCircle(R.drawable.ic_person_24,Color.rgb(138,101,0),Color.rgb(253,236,205));intro.addView(personCircle,new LinearLayout.LayoutParams(dp(56),dp(56)));LinearLayout introText=column();introText.addView(text("Ajouter un professeur",23,TEXT,true));introText.addView(text("Renseignez les informations du nouveau professeur pour l’ajouter à votre centre.",14,MUTED,false));intro.addView(introText,margins(0,WRAP,dp(14),0,0,0,1));form.addView(intro,margins(MATCH,WRAP,0,0,0,dp(18)));

        LinearLayout personal=section("Informations personnelles","Ces informations nous aident à identifier le professeur.");
        fullName = input("Nom complet *");fullName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_24,0,0,0);fullName.setCompoundDrawableTintList(ColorStateList.valueOf(MUTED));fullName.setCompoundDrawablePadding(dp(10)); addField(personal, "", fullName);
        phone = input("Téléphone *");phone.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_24,0,0,0);phone.setCompoundDrawableTintList(ColorStateList.valueOf(MUTED));phone.setCompoundDrawablePadding(dp(10)); phone.setInputType(android.text.InputType.TYPE_CLASS_PHONE); addField(personal, "", phone);form.addView(personal);

        LinearLayout levelCard=section("Niveaux pris en charge","Sélectionnez les niveaux que ce professeur peut enseigner.");
        LinearLayout levels = row();
        level1ac = chip("1AC"); level2ac = chip("2AC"); level3ac = chip("3AC"); levelTc=chip("TC");
        levels.addView(level1ac, margins(0, dp(48), 0, 0, dp(5), 0,1));
        levels.addView(level2ac, margins(0, dp(48), dp(5), 0, dp(5), 0,1));
        levels.addView(level3ac, margins(0,dp(48),dp(5),0,dp(5),0,1));levels.addView(levelTc,new LinearLayout.LayoutParams(0,dp(48),1)); levelCard.addView(levels);form.addView(levelCard,margins(MATCH,WRAP,0,dp(14),0,0));

        LinearLayout subjectCard=section("Matières","Sélectionnez les matières enseignées par ce professeur.");
        LinearLayout subjectRow1 = row(); math = chip("Mathématiques"); french = chip("Français");
        subjectRow1.addView(math, margins(0, dp(48), 0, 0, dp(8), 0,1)); subjectRow1.addView(french, new LinearLayout.LayoutParams(0, dp(48),1)); subjectCard.addView(subjectRow1);
        LinearLayout subjectRow2 = row(); pc = chip("Physique-Chimie / PC"); svt = chip("SVT");
        subjectRow2.addView(pc, margins(0, dp(48), 0, 0, dp(8), 0,1)); subjectRow2.addView(svt, new LinearLayout.LayoutParams(0, dp(48),1)); subjectCard.addView(subjectRow2, margins(MATCH, WRAP, 0, dp(8), 0, 0));form.addView(subjectCard,margins(MATCH,WRAP,0,dp(14),0,0));

        LinearLayout notesCard=section("Notes (optionnel)","Ajoutez des informations complémentaires si nécessaire.");EditText notes=input("Notes");notes.setSingleLine(false);notes.setMinLines(2);notesCard.addView(notes,new LinearLayout.LayoutParams(MATCH,dp(82)));form.addView(notesCard,margins(MATCH,WRAP,0,dp(14),0,0));

        TextView save = text("Enregistrer le professeur", 17, Color.WHITE, true);save.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_24,0,0,0);save.setCompoundDrawableTintList(ColorStateList.valueOf(Color.WHITE));save.setCompoundDrawablePadding(dp(12));
        save.setGravity(Gravity.CENTER); save.setBackground(rounded(BROWN, dp(16), Color.TRANSPARENT, 0)); save.setOnClickListener(v -> saveTeacher());
        form.addView(save, margins(MATCH, dp(58), 0, dp(8), 0, 0));TextView cancel=text("Annuler",16,TERRACOTTA,true);cancel.setGravity(Gravity.CENTER);cancel.setBackground(rounded(Palette.PRIMARY_SOFT,dp(16),0,0));cancel.setOnClickListener(v->finish());form.addView(cancel,margins(MATCH,dp(52),0,dp(10),0,0));
        scroll.addView(form, new ScrollView.LayoutParams(MATCH, WRAP)); root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f)); setContentView(root);
    }

    private View hero() {
        LinearLayout hero=row();hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(dp(16),dp(10),dp(18),dp(10));GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.rgb(15,94,97),Color.rgb(24,127,132)});g.setCornerRadii(new float[]{0,0,0,0,dp(22),dp(22),dp(22),dp(22)});hero.setBackground(g);ImageView back=icon(R.drawable.ic_arrow_back_24,Color.WHITE);back.setPadding(dp(10),dp(10),dp(10),dp(10));back.setOnClickListener(v->finish());hero.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));hero.addView(text("Nouveau professeur",20,Color.WHITE,true),new LinearLayout.LayoutParams(0,WRAP,1));hero.addView(icon(R.drawable.ic_notifications_24,Color.WHITE),new LinearLayout.LayoutParams(dp(40),dp(40)));TextView profile=text("MA",14,Color.WHITE,true);profile.setGravity(Gravity.CENTER);profile.setBackground(rounded(Color.argb(70,255,255,255),dp(20),0,0));hero.addView(profile,new LinearLayout.LayoutParams(dp(40),dp(40)));return hero;
    }

    private LinearLayout section(String title,String subtitle){int res=R.drawable.ic_person_24,fg=TERRACOTTA,bg=Color.rgb(221,243,238);if(title.startsWith("Niveaux"))res=R.drawable.ic_equalizer_24;else if(title.equals("Matières")){res=R.drawable.ic_menu_book_24;fg=Color.rgb(138,101,0);bg=Color.rgb(253,236,205);}else if(title.startsWith("Notes")){res=R.drawable.ic_description_24;fg=Color.rgb(30,90,135);bg=Palette.BLUE_SOFT;}LinearLayout card=column();card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setElevation(dp(2));card.setBackground(rounded(SURFACE,dp(12),Color.TRANSPARENT,0));LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.addView(iconCircle(res,fg,bg),new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout copy=column();copy.addView(text(title,18,TEXT,true));copy.addView(text(subtitle,13,MUTED,false));head.addView(copy,margins(0,WRAP,dp(12),0,0,0,1));card.addView(head,margins(MATCH,WRAP,0,0,0,dp(12)));return card;}
    private FrameLayout iconCircle(int res,int color,int background){FrameLayout circle=new FrameLayout(this);circle.setBackground(rounded(background,dp(24),0,0));circle.addView(icon(res,color),new FrameLayout.LayoutParams(dp(24),dp(24),Gravity.CENTER));return circle;}
    private ImageView icon(int resource,int color){ImageView image=new ImageView(this);image.setImageResource(resource);image.setColorFilter(color);image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);return image;}

    private void saveTeacher() {
        String name = fullName.getText().toString().trim(); ArrayList<String> levels = new ArrayList<>(), subjects = new ArrayList<>();
        if (level1ac.isChecked()) levels.add("1AC"); if (level2ac.isChecked()) levels.add("2AC"); if (level3ac.isChecked()) levels.add("3AC"); if(levelTc.isChecked())levels.add("TC");
        if (math.isChecked()) subjects.add("Mathématiques"); if (french.isChecked()) subjects.add("Français"); if (pc.isChecked()) subjects.add("Physique-Chimie"); if (svt.isChecked()) subjects.add("SVT");
        if (name.isEmpty()) { message("Le nom du professeur est obligatoire."); return; }
        if (levels.isEmpty()) { message("Choisissez au moins un niveau."); return; }
        if (subjects.isEmpty()) { message("Choisissez au moins une matière."); return; }
        if (database.addTeacher(name, phone.getText().toString(), TextUtils.join(", ", levels), TextUtils.join(", ", subjects)) > 0) { database.refreshAutomaticTeacherAssignments(); Toast.makeText(this, "Professeur enregistré. Les tarifs compatibles sont détectés automatiquement.", Toast.LENGTH_LONG).show(); finish(); } else message("Impossible d'enregistrer ce professeur.");
    }

    private CheckBox chip(String value) { CheckBox box = new CheckBox(this); box.setText(value); box.setTextSize(14); box.setTypeface(Typeface.DEFAULT, Typeface.BOLD); box.setTextColor(TERRACOTTA); box.setGravity(Gravity.CENTER); box.setPadding(dp(7), 0, dp(9), 0); box.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}}, new int[]{BROWN, MUTED})); box.setBackground(rounded(Palette.PRIMARY_SOFT, dp(18), Color.TRANSPARENT, 0)); return box; }
    private EditText input(String hint) { EditText field = new EditText(this); field.setHint(hint); field.setHintTextColor(Color.rgb(178, 165, 154)); field.setTextColor(TEXT); field.setTextSize(16); field.setSingleLine(true); field.setPadding(dp(15), 0, dp(15), 0); field.setBackground(rounded(SURFACE, dp(16), BORDER, dp(1))); return field; }
    private void addField(LinearLayout form, String title, EditText field) { if(!title.isEmpty())form.addView(label(title), margins(MATCH, WRAP, 0, dp(12), 0, dp(8))); form.addView(field, margins(MATCH, dp(58),0,dp(8),0,0)); }
    private TextView label(String value) { return text(value, 16, TEXT, true); }
    private void message(String value) { Toast.makeText(this, value, Toast.LENGTH_SHORT).show(); }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setIncludeFontPadding(false); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); return v; }
    private GradientDrawable rounded(int color, int radius, int stroke, int width) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if (width > 0) d.setStroke(width, stroke); return d; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height); p.setMargins(left, top, right, bottom); return p; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom, float weight) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height, weight); p.setMargins(left, top, right, bottom); return p; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT, WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
}
