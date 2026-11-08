package com.centrepay.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherSettlementListActivity extends Activity {
    private static final int PRIMARY = Palette.PRIMARY, DARK = Palette.PRIMARY_DARK, TEXT = Palette.TEXT, MUTED = Palette.MUTED;
    private static final int SURFACE = Palette.SURFACE, BORDER = Palette.BORDER, SUCCESS = Palette.SUCCESS, WARNING = Palette.WARNING, ERROR = Palette.ERROR;
    private StudentDatabaseHelper database;
    private LinearLayout content;
    private EditText search;
    private LinearLayout summaryHost,resultsHost;
    private String selectedStatus="",selectedSort="Reste dû";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); getWindow().setStatusBarColor(DARK); getWindow().setNavigationBarColor(Palette.BACKGROUND); database = new StudentDatabaseHelper(this);
        LinearLayout root = column(); root.setBackgroundColor(Palette.BACKGROUND); root.addView(header(), new LinearLayout.LayoutParams(MATCH, dp(88)));
        ScrollView scroll = new ScrollView(this); content = column(); content.setPadding(dp(16), dp(12), dp(16), dp(24));summaryHost=column();content.addView(summaryHost);content.addView(searchBar(""),lp(MATCH,dp(52),0,0,0,dp(10)));content.addView(filterBar(),lp(MATCH,dp(44),0,0,0,dp(12)));resultsHost=column();content.addView(resultsHost);scroll.addView(content, new ScrollView.LayoutParams(MATCH, WRAP)); root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
        root.addView(bottomNavigation(), new LinearLayout.LayoutParams(MATCH, dp(74))); setContentView(root);
    }
    @Override protected void onResume() { super.onResume(); if (resultsHost != null) render(search == null ? "" : search.getText().toString()); }

    private void render(String query) {
        summaryHost.removeAllViews();resultsHost.removeAllViews(); List<Teacher> all = database.findTeachers("");
        int total = 0, concerned = 0;
        for (Teacher teacher : all) { int remaining = outstanding(teacher); if (remaining > 0) { total += remaining; concerned++; } }
        summaryHost.addView(summary(total, concerned, all.size()), lp(MATCH, WRAP, 0, 0, 0, dp(12)));
        List<Teacher> filtered = new ArrayList<>(database.findTeachers(query == null ? "" : query.trim()));filtered.removeIf(t -> (selectedStatus.equals("À verser") && outstanding(t) <= 0) || (selectedStatus.equals("À jour") && outstanding(t) > 0));if(selectedSort.equals("Nom"))filtered.sort((a,b)->a.fullName.compareToIgnoreCase(b.fullName));else if(selectedSort.equals("Mois ouverts"))filtered.sort((a,b)->Integer.compare(openMonths(b),openMonths(a)));else filtered.sort((a,b)->Integer.compare(outstanding(b),outstanding(a)));
        if (filtered.isEmpty()) { TextView empty = text("Aucun professeur ne correspond aux filtres.", 14, MUTED, false); empty.setGravity(Gravity.CENTER); empty.setPadding(0, dp(28), 0, dp(28)); resultsHost.addView(empty); return; }
        for (Teacher teacher : filtered) resultsHost.addView(teacherCard(teacher), lp(MATCH, WRAP, 0, 0, 0, dp(10)));
    }

    private View header() {
        LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(18), 0, dp(18), 0); bar.setBackground(gradient(DARK, PRIMARY, 0));
        LinearLayout titles = column(); TextView logo = text("CentrePay", 25, Color.WHITE, true); titles.addView(logo); titles.addView(text("Règlements enseignants", 13, 0xffd7efed, false), lp(MATCH, WRAP, 0, dp(4), 0, 0)); bar.addView(titles, new LinearLayout.LayoutParams(0, WRAP, 1));
        TextView avatar = text("CP", 14, Color.WHITE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(round(0x55ffffff, dp(24), 0, 0)); bar.addView(avatar, lp(dp(48), dp(48), dp(16), 0, 0, 0)); return bar;
    }

    private View summary(int total, int concerned, int all) {
        LinearLayout card = row(); card.setPadding(dp(14), dp(16), dp(14), dp(16)); card.setGravity(Gravity.CENTER_VERTICAL); card.setBackground(round(SURFACE, dp(16), BORDER, dp(1))); card.setElevation(dp(1));
        card.addView(summaryPart(R.drawable.ic_credit_card_24, Palette.SUCCESS_SOFT, SUCCESS, "Total à verser", total + " DH", "Montant encore dû"), new LinearLayout.LayoutParams(0, WRAP, 1));
        View line = new View(this); line.setBackgroundColor(BORDER); card.addView(line, lp(dp(1), dp(76), dp(8), 0, dp(8), 0));
        card.addView(summaryPart(R.drawable.ic_groups_24, 0xfffff3d6, 0xff926000, "Professeurs concernés", String.valueOf(concerned), "Sur " + all + " professeurs"), new LinearLayout.LayoutParams(0, WRAP, 1)); return card;
    }

    private View summaryPart(int res, int bg, int color, String title, String value, String sub) {
        LinearLayout part = row(); part.setGravity(Gravity.CENTER_VERTICAL); LinearLayout bubble = row(); bubble.setGravity(Gravity.CENTER); bubble.setBackground(round(bg, dp(25), 0, 0)); bubble.addView(icon(res, color), new LinearLayout.LayoutParams(dp(25), dp(25))); part.addView(bubble, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout words = column(); words.addView(text(title, 11, MUTED, false)); words.addView(text(value, 18, TEXT, true), lp(MATCH, WRAP, 0, dp(4), 0, 0)); words.addView(text(sub, 11, MUTED, false), lp(MATCH, WRAP, 0, dp(4), 0, 0)); part.addView(words, lp(0, WRAP, dp(9), 0, 0, 0, 1)); return part;
    }

    private View searchBar(String currentQuery) {
        LinearLayout wrap = row(); wrap.setGravity(Gravity.CENTER_VERTICAL); wrap.setPadding(dp(14), 0, dp(10), 0); wrap.setBackground(round(SURFACE, dp(15), BORDER, dp(1)));
        wrap.addView(icon(R.drawable.ic_search_24, DARK), new LinearLayout.LayoutParams(dp(25), dp(25)));
        search = new EditText(this); search.setHint("Rechercher un professeur..."); search.setHintTextColor(0xff94a3b8); search.setTextColor(TEXT); search.setTextSize(14); search.setSingleLine(); search.setBackgroundColor(Color.TRANSPARENT); search.setPadding(dp(10), 0, 0, 0); search.setText(currentQuery); wrap.addView(search, new LinearLayout.LayoutParams(0, MATCH, 1));
        ImageView tune = icon(R.drawable.ic_tune_24, DARK); tune.setPadding(dp(7), dp(7), dp(7), dp(7)); tune.setBackground(round(0xffedf6f5, dp(10), 0, 0));tune.setOnClickListener(v->showSort()); wrap.addView(tune, new LinearLayout.LayoutParams(dp(38), dp(38)));
        search.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s,int a,int b,int c){} public void onTextChanged(CharSequence s,int a,int b,int c){render(s.toString());} public void afterTextChanged(Editable e){} }); return wrap;
    }

    private View filterBar(){HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);LinearLayout row=row();for(String value:new String[]{"Tous","À verser","À jour"}){boolean active=value.equals("Tous")?selectedStatus.isEmpty():value.equals(selectedStatus);TextView chip=text(value,13,active?Color.WHITE:TEXT,true);chip.setGravity(Gravity.CENTER);chip.setPadding(dp(16),0,dp(16),0);chip.setBackground(round(active?PRIMARY:0xfff1f5f9,dp(20),active?0:BORDER,active?0:dp(1)));chip.setOnClickListener(v->{selectedStatus=value.equals("Tous")?"":value;rebuildFilterBar(scroll);render(search.getText().toString());});row.addView(chip,lp(WRAP,dp(40),0,0,dp(8),0));}scroll.addView(row);return scroll;}
    private void rebuildFilterBar(HorizontalScrollView old){ViewGroup parent=(ViewGroup)old.getParent();int index=parent.indexOfChild(old);parent.removeViewAt(index);parent.addView(filterBar(),index,lp(MATCH,dp(44),0,0,0,dp(12)));}
    private void showSort(){String[] values={"Reste dû","Nom","Mois ouverts"};new AlertDialog.Builder(this).setTitle("Trier les règlements").setItems(values,(d,w)->{selectedSort=values[w];render(search.getText().toString());}).show();}

    private View teacherCard(Teacher teacher) {
        int remaining = outstanding(teacher), months = openMonths(teacher); LinearLayout card = row(); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(12), dp(13), dp(12), dp(13)); card.setBackground(round(SURFACE, dp(16), BORDER, dp(1))); card.setElevation(dp(1));
        TextView avatar = text(initials(teacher.fullName), 16, avatarColor(teacher.id), true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(round(avatarBg(teacher.id), dp(25), 0, 0)); card.addView(avatar, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout details = column(); details.addView(text(teacher.fullName, 16, TEXT, true)); details.addView(text(teacher.subjects == null || teacher.subjects.isEmpty() ? "Enseignant" : "Cours : " + teacher.subjects, 12, MUTED, false), lp(MATCH, WRAP, 0, dp(4), 0, 0));
        LinearLayout monthLine = row(); monthLine.setGravity(Gravity.CENTER_VERTICAL); monthLine.addView(icon(R.drawable.ic_calendar_month_24, MUTED), new LinearLayout.LayoutParams(dp(18), dp(18))); monthLine.addView(text(months == 0 ? "Tous les salaires sont réglés" : months + (months == 1 ? " mois ouvert" : " mois ouverts"), 11, months == 0 ? MUTED : TEXT, months > 0), lp(WRAP, WRAP, dp(7), 0, 0, 0)); details.addView(monthLine, lp(MATCH, WRAP, 0, dp(10), 0, 0)); card.addView(details, lp(0, WRAP, dp(12), 0, dp(8), 0, 1));
        LinearLayout side = column(); side.setGravity(Gravity.END); TextView amount = text(remaining + " DH", 17, TEXT, true); amount.setGravity(Gravity.END); side.addView(amount, new LinearLayout.LayoutParams(MATCH, WRAP));
        side.addView(status(months), lp(WRAP, WRAP, 0, dp(7), 0, 0)); TextView open = text("Voir salaires  ›", 12, DARK, true); open.setGravity(Gravity.CENTER); open.setPadding(dp(12), dp(9), dp(12), dp(9)); open.setBackground(round(Palette.PRIMARY_SOFT, dp(18), 0, 0)); open.setOnClickListener(v -> { Intent i = new Intent(this, TeacherMonthlySalaryActivity.class); i.putExtra(TeacherMonthlySalaryActivity.EXTRA_TEACHER_ID, teacher.id); startActivity(i); }); side.addView(open, lp(WRAP, WRAP, 0, dp(8), 0, 0)); card.addView(side); return card;
    }

    private TextView status(int months) { int color = months == 0 ? SUCCESS : months <= 1 ? WARNING : ERROR, bg = months == 0 ? Palette.SUCCESS_SOFT : months <= 1 ? 0xfffff3d6 : Palette.ERROR_SOFT; TextView b = text(months == 0 ? "✓  À jour" : (months <= 1 ? "◷  1 mois" : "!  " + months + " mois"), 11, color, true); b.setGravity(Gravity.CENTER); b.setPadding(dp(9), dp(6), dp(9), dp(6)); b.setBackground(round(bg, dp(18), 0, 0)); return b; }
    private int outstanding(Teacher t) { int total = 0; for (TeacherBalance b : database.getTeacherMonthlyBalances(t.id)) total += b.remainingAmount; return total; }
    private int openMonths(Teacher t) { int count = 0; for (TeacherBalance b : database.getTeacherMonthlyBalances(t.id)) if (b.remainingAmount > 0) count++; return count; }

    private View bottomNavigation() { LinearLayout nav = row(); nav.setGravity(Gravity.CENTER); nav.setPadding(dp(8), dp(7), dp(8), dp(7)); nav.setBackground(round(SURFACE, dp(20), BORDER, dp(1))); nav.addView(navItem("Accueil",R.drawable.ic_home_24,false,MainActivity.class),new LinearLayout.LayoutParams(0,MATCH,1)); nav.addView(navItem("Élèves",R.drawable.ic_groups_24,false,StudentListActivity.class),new LinearLayout.LayoutParams(0,MATCH,1)); nav.addView(navItem("Paiements",R.drawable.ic_credit_card_24,true,PaymentListActivity.class),new LinearLayout.LayoutParams(0,MATCH,1)); nav.addView(navItem("Profs",R.drawable.ic_school_24,false,TeacherListActivity.class),new LinearLayout.LayoutParams(0,MATCH,1)); return nav; }
    private View navItem(String label,int res,boolean active,Class<?> target){ LinearLayout item=column();item.setGravity(Gravity.CENTER);ImageView image=icon(res,active?DARK:MUTED);item.addView(image,new LinearLayout.LayoutParams(dp(25),dp(25)));item.addView(text(label,11,active?DARK:MUTED,active),lp(WRAP,WRAP,0,dp(3),0,0));item.setOnClickListener(v->{if(!active){startActivity(new Intent(this,target));finish();}});return item; }
    private String initials(String name){ if(name==null||name.trim().isEmpty())return "PR";String[] p=name.trim().split("\\s+");return (p[0].substring(0,1)+(p.length>1?p[p.length-1].substring(0,1):"")).toUpperCase(Locale.ROOT); }
    private int avatarBg(long id){int[] c={0xffddf7f4,0xfffff3d6,0xffffe4e6,Palette.BLUE_SOFT,0xffede9fe};return c[(int)(Math.abs(id)%c.length)];} private int avatarColor(long id){int[] c={DARK,0xff92400e,0xffbe123c,0xff075985,0xff5b21b6};return c[(int)(Math.abs(id)%c.length)];}
    private TextView text(String v,int s,int c,boolean b){TextView x=new TextView(this);x.setText(v);x.setTextSize(s);x.setTextColor(c);x.setIncludeFontPadding(false);if(b)x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return x;} private ImageView icon(int r,int c){ImageView v=new ImageView(this);v.setImageResource(r);v.setImageTintList(ColorStateList.valueOf(c));v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);return v;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;} private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);return v;} private GradientDrawable round(int c,int r,int s,int w){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(r);if(w>0)d.setStroke(w,s);return d;} private GradientDrawable gradient(int a,int b,int r){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{a,b});d.setCornerRadius(r);return d;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(l,t,r,b);return p;} private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h,weight);p.setMargins(l,t,r,b);return p;} private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);} private static final int MATCH=ViewGroup.LayoutParams.MATCH_PARENT,WRAP=ViewGroup.LayoutParams.WRAP_CONTENT;
}
