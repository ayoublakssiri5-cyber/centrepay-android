package com.centrepay.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Tableau de bord CentrePay — design teal/doré approuvé. */
public class MainActivity extends Activity {
    private static final int PRIMARY = Palette.PRIMARY;
    private static final int PRIMARY_DARK = Palette.PRIMARY_DARK;
    private static final int PRIMARY_LIGHT = Palette.PRIMARY_SOFT;
    private static final int ACCENT = Palette.ACCENT;
    private static final int ACCENT_LIGHT = Palette.ACCENT_SOFT;
    private static final int BACKGROUND = Palette.BACKGROUND;
    private static final int SURFACE = Palette.SURFACE;
    private static final int TEXT = Palette.TEXT;
    private static final int MUTED = Palette.MUTED;
    private static final int BORDER = Palette.BORDER;
    private static final int GREEN = Palette.SUCCESS;
    private static final int GREEN_BG = Palette.SUCCESS_SOFT;
    private static final int ORANGE = Palette.WARNING;
    private static final int ORANGE_BG = Palette.WARNING_SOFT;
    private static final int RED = Palette.ERROR;
    private static final int RED_BG = Palette.ERROR_SOFT;

    private StudentDatabaseHelper database;
    private DashboardSummary dashboard;
    private MonthlyReport currentReport;
    private LinearLayout body;
    private boolean firstResume = true;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(PRIMARY_DARK);
        getWindow().setNavigationBarColor(SURFACE);
        getWindow().setDecorFitsSystemWindows(true);
        database = new StudentDatabaseHelper(this);

        LinearLayout root = column(); root.setBackgroundColor(BACKGROUND);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page = column(); page.addView(header());
        body = column(); body.setPadding(dp(16), dp(18), dp(16), dp(24));
        refreshDashboard();
        renderBody();
        page.addView(body); scroll.addView(page, new ScrollView.LayoutParams(MATCH, WRAP));
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f)); root.addView(bottomNavigation()); setContentView(root);
    }

    private void refreshDashboard() {
        dashboard = database.getDashboardSummary();
        String month = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        currentReport = database.getMonthlyReport(month);
    }

    private void renderBody() {
        body.removeAllViews();
        body.addView(welcome());
        addStatistics(body);
        body.addView(financialCard(), margins(MATCH, WRAP, 0, dp(16), 0, 0));
        body.addView(sectionTitle(R.drawable.ic_analytics_24, "Actions rapides", ""), margins(MATCH, WRAP, 0, dp(20), 0, dp(10)));
        body.addView(quickActions());
        body.addView(sectionTitle(R.drawable.ic_credit_card_24, "Derniers paiements", "Voir tout  ›"), margins(MATCH, WRAP, 0, dp(20), 0, dp(10)));
        addRecentPayments(body);
    }

    @Override protected void onResume() { super.onResume(); if (firstResume) firstResume = false; else { refreshDashboard(); renderBody(); } }

    private View header() {
        LinearLayout header = column(); header.setPadding(dp(18), dp(14), dp(18), dp(16)); header.setBackground(headerGradient());
        LinearLayout top = row(); top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout brand = column(); LinearLayout logo = row(); logo.addView(text("Centre", 25, Color.WHITE, true)); logo.addView(text("Pay", 25, Color.rgb(240, 194, 89), true)); brand.addView(logo); brand.addView(text("Tableau de bord", 13, Color.rgb(214, 235, 237), false), margins(WRAP, WRAP, 0, dp(3), 0, 0)); top.addView(brand, new LinearLayout.LayoutParams(0, WRAP, 1f));
        TextView profile = text("CP", 15, Color.WHITE, true); profile.setGravity(Gravity.CENTER); profile.setBackground(round(Color.rgb(78, 139, 146), dp(25), Color.TRANSPARENT, 0)); top.addView(profile, new LinearLayout.LayoutParams(dp(50), dp(50))); header.addView(top); return header;
    }

    private View welcome() {
        LinearLayout welcome = column();
        welcome.addView(text("Bonjour !", 24, TEXT, true));
        welcome.addView(text("Voici la situation de votre centre.", 15, MUTED, false), margins(MATCH, WRAP, 0, dp(4), 0, 0));
        welcome.addView(text("▣  " + new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE).format(new Date()), 13, PRIMARY, true), margins(MATCH, WRAP, 0, dp(9), 0, 0));
        return welcome;
    }

    private void addStatistics(LinearLayout body) {
        LinearLayout first = row();
        first.addView(statCard(R.drawable.ic_groups_24, "Élèves inscrits", String.valueOf(dashboard.activeStudents), "Tous les élèves", PRIMARY, PRIMARY_LIGHT, StudentListActivity.class), weighted(1,0,dp(12),dp(8),0));
        first.addView(statCard(R.drawable.ic_credit_card_24, "Total encaissé", money(dashboard.receivedAmount), "Depuis le début", GREEN, GREEN_BG, PaymentListActivity.class), weighted(1,dp(8),dp(12),0,0));
        body.addView(first, margins(MATCH,WRAP,0,dp(18),0,0));
        LinearLayout second = row();
        second.addView(statCard(R.drawable.ic_error_24, "Reste à encaisser", money(dashboard.outstandingAmount), "Toutes échéances", RED, RED_BG, PaymentListActivity.class), weighted(1,0,0,dp(8),0));
        second.addView(statCard(R.drawable.ic_school_24, "Salaires à verser", money(dashboard.teacherAmount), "Tous les professeurs", Color.rgb(146,102,0), ACCENT_LIGHT, TeacherSettlementListActivity.class), weighted(1,dp(8),0,0,0));
        body.addView(second, margins(MATCH,WRAP,0,dp(12),0,0));
    }

    private View statCard(int iconRes,String title,String value,String note,int color,int bg,Class<?> destination) {
        LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(12),dp(15),dp(10),dp(15));card.setMinimumHeight(dp(112));card.setElevation(dp(2));card.setBackground(round(SURFACE,dp(18),Color.TRANSPARENT,0));
        card.setOnClickListener(v->startActivity(new Intent(this,destination)));
        card.setContentDescription(title+" : "+value+". Ouvrir.");
        ImageView symbol=icon(iconRes,color,24);symbol.setBackground(round(bg,dp(26),Color.TRANSPARENT,0));symbol.setPadding(dp(13),dp(13),dp(13),dp(13));card.addView(symbol,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout info=column();info.addView(text(title,13,MUTED,false));info.addView(text(value,19,color==RED?RED:TEXT,true),margins(WRAP,WRAP,0,dp(4),0,0));info.addView(text(note+"  ›",11,color,false),margins(WRAP,WRAP,0,dp(4),0,0));card.addView(info,margins(0,WRAP,dp(10),0,0,0,1));return card;
    }

    private View financialCard() {
        LinearLayout card=column();card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setElevation(dp(2));card.setBackground(round(SURFACE,dp(18),Color.TRANSPARENT,0));
        card.setOnClickListener(v->startActivity(new Intent(this,MonthlyReportActivity.class)));
        card.setContentDescription("Situation financière du mois. Ouvrir le rapport mensuel.");
        card.addView(sectionTitle(R.drawable.ic_analytics_24,"Situation financière",""));
        String month=new SimpleDateFormat("MMMM yyyy",Locale.FRANCE).format(new Date());
        card.addView(text("Paiements affectés à "+month+"  ·  Voir le rapport ›",12,MUTED,false),margins(MATCH,WRAP,0,dp(7),0,0));
        int received=currentReport.receivedAmount,center=currentReport.centerAmount,teachers=currentReport.teachersEarnedAmount;
        LinearLayout metrics=row();
        metrics.addView(financeMetric(R.drawable.ic_credit_card_24,"Affecté au mois",money(received),GREEN),new LinearLayout.LayoutParams(0,WRAP,1));
        metrics.addView(financeMetric(R.drawable.ic_analytics_24,"Part centre",money(center),PRIMARY),new LinearLayout.LayoutParams(0,WRAP,1));
        metrics.addView(financeMetric(R.drawable.ic_groups_24,"Part profs",money(teachers),TEXT),new LinearLayout.LayoutParams(0,WRAP,1));
        card.addView(metrics,margins(MATCH,WRAP,0,dp(18),0,0));
        if(center+teachers>0){
            LinearLayout progress=row();progress.addView(colorBlock(PRIMARY),new LinearLayout.LayoutParams(0,dp(12),Math.max(0,center)));progress.addView(colorBlock(ACCENT),new LinearLayout.LayoutParams(0,dp(12),Math.max(0,teachers)));card.addView(progress,margins(MATCH,dp(12),0,dp(13),0,0));
            int total=center+teachers;LinearLayout legends=row();legends.addView(text(Math.round(center*100f/total)+"% Centre",13,PRIMARY,true),new LinearLayout.LayoutParams(0,WRAP,1));
            TextView right=text(Math.round(teachers*100f/total)+"% Professeurs",13,Color.rgb(146,102,0),true);right.setGravity(Gravity.END);legends.addView(right,new LinearLayout.LayoutParams(0,WRAP,1));card.addView(legends,margins(MATCH,WRAP,0,dp(8),0,0));
        }else card.addView(text("Aucun paiement affecté à ce mois.",13,MUTED,false),margins(MATCH,WRAP,0,dp(14),0,0));
        return card;
    }

    private View financeMetric(int iconRes,String title,String value,int color){LinearLayout box=column();box.addView(icon(iconRes,color,20),new LinearLayout.LayoutParams(dp(28),dp(28)));box.addView(text(title,12,MUTED,false),margins(WRAP,WRAP,0,dp(7),0,0));box.addView(text(value,17,TEXT,true));return box;}
    private View colorBlock(int color){View v=new View(this);v.setBackground(round(color,dp(6),Color.TRANSPARENT,0));return v;}

    private View quickActions(){
        LinearLayout grid=column();
        LinearLayout first=row();
        first.addView(action(R.drawable.ic_credit_card_24,"Nouveau paiement",PRIMARY,Color.WHITE,v->startActivity(new Intent(this,AddPaymentActivity.class))),weighted(1,0,0,dp(6),0));
        first.addView(action(R.drawable.ic_person_add_24,"Nouvel élève",Color.rgb(226,245,239),TEXT,v->startActivity(new Intent(this,AddStudentActivity.class))),weighted(1,dp(6),0,0,0));
        grid.addView(first);
        LinearLayout second=row();
        second.addView(action(R.drawable.ic_sell_24,"Gestion des tarifs",ACCENT_LIGHT,Color.rgb(116,80,0),v->startActivity(new Intent(this,TariffActivity.class))),weighted(1,0,dp(12),dp(6),0));
        second.addView(action(R.drawable.ic_analytics_24,"Rapport mensuel",Color.rgb(229,245,251),PRIMARY,v->startActivity(new Intent(this,MonthlyReportActivity.class))),weighted(1,dp(6),dp(12),0,0));
        grid.addView(second);return grid;
    }
    private View action(int iconRes,String title,int bg,int color,View.OnClickListener click){LinearLayout card=column();card.setGravity(Gravity.CENTER);card.setPadding(dp(8),dp(12),dp(8),dp(12));card.setMinimumHeight(dp(88));card.setBackground(round(bg,dp(18),Color.WHITE,dp(1)));card.setOnClickListener(click);card.setContentDescription(title);card.addView(icon(iconRes,color,25),new LinearLayout.LayoutParams(dp(31),dp(31)));TextView name=text(title,14,color,true);name.setGravity(Gravity.CENTER);card.addView(name,margins(MATCH,WRAP,0,dp(8),0,0));return card;}

    private View sectionTitle(int iconRes,String title,String action){LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.addView(icon(iconRes,PRIMARY,23),new LinearLayout.LayoutParams(dp(28),dp(28)));row.addView(text(title,18,TEXT,true),margins(0,WRAP,dp(10),0,0,0,1));TextView link=text(action,13,MUTED,true);if(action.contains("Voir"))link.setOnClickListener(v->startActivity(new Intent(this,PaymentListActivity.class)));row.addView(link);return row;}
    private void addRecentPayments(LinearLayout body){List<PaymentRecord> list=database.getRecentPayments();if(list.isEmpty()){TextView empty=text("Aucun paiement enregistré.",14,MUTED,false);empty.setPadding(dp(16),dp(22),dp(16),dp(22));empty.setBackground(round(SURFACE,dp(18),Color.TRANSPARENT,0));body.addView(empty);return;}LinearLayout group=column();group.setPadding(dp(12),dp(5),dp(12),dp(5));group.setBackground(round(SURFACE,dp(18),Color.TRANSPARENT,0));for(int i=0;i<Math.min(3,list.size());i++)group.addView(paymentRow(list.get(i)),margins(MATCH,WRAP,0,dp(5),0,dp(5)));body.addView(group);}
    private View paymentRow(PaymentRecord p){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(6),dp(9),dp(6),dp(9));
        row.setOnClickListener(v->{Intent i=new Intent(this,StudentDetailActivity.class);i.putExtra("student_id",p.studentId);startActivity(i);});
        row.setContentDescription("Paiement de "+p.studentName+" : "+money(p.amount)+". Ouvrir la fiche élève.");
        TextView avatar=text(initials(p.studentName),12,PRIMARY,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(round(PRIMARY_LIGHT,dp(23),Color.TRANSPARENT,0));row.addView(avatar,new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout info=column();info.addView(text(p.studentName,14,TEXT,true));info.addView(text(p.referenceMonth+" · "+p.studentLevel,12,MUTED,false));row.addView(info,margins(0,WRAP,dp(10),0,0,0,1));
        LinearLayout amount=column();amount.addView(text(money(p.amount),14,TEXT,true));amount.addView(text(p.paymentDate,11,MUTED,false));row.addView(amount);
        TextView badge=text(p.advanceAmount>0?"Avance":"Reçu",11,p.advanceAmount>0?ORANGE:GREEN,true);badge.setPadding(dp(8),dp(6),dp(8),dp(6));badge.setBackground(round(p.advanceAmount>0?ORANGE_BG:GREEN_BG,dp(14),Color.TRANSPARENT,0));row.addView(badge,margins(WRAP,WRAP,dp(9),0,0,0));
        return row;
    }

    private View bottomNavigation(){return BottomTabBar.create(this,MainActivity.class);}

    private GradientDrawable headerGradient(){return new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{PRIMARY_DARK,Color.rgb(24,116,122)});}
    private ImageView icon(int drawable,int color,int size){ImageView view=new ImageView(this);view.setImageResource(drawable);view.setColorFilter(color);view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);view.setContentDescription("");return view;}
    private String initials(String name){String[] p=name.trim().split("\\s+");return p.length>1?(""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase():name.substring(0,Math.min(2,name.length())).toUpperCase();}
    private String money(int amount){return String.format(Locale.FRANCE,"%,d DH",amount).replace(',', ' ');}
    private TextView text(String value,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setIncludeFontPadding(false);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);return v;}
    private GradientDrawable round(int color,int radius,int stroke,int width){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(width>0)d.setStroke(width,stroke);return d;}
    private LinearLayout.LayoutParams margins(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(l,t,r,b);return p;}private LinearLayout.LayoutParams margins(int w,int h,int l,int t,int r,int b,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h,weight);p.setMargins(l,t,r,b);return p;}private LinearLayout.LayoutParams weighted(float weight,int l,int t,int r,int b){return margins(0,WRAP,l,t,r,b,weight);}private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}private static final int MATCH=ViewGroup.LayoutParams.MATCH_PARENT,WRAP=ViewGroup.LayoutParams.WRAP_CONTENT;
}
