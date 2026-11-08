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
import java.util.ArrayList;
import java.util.List;

public class TeacherMonthlySalaryActivity extends Activity {
    public static final String EXTRA_TEACHER_ID="teacher_id";
    private static final int TEAL=Palette.PRIMARY,DARK=Palette.PRIMARY_DARK,BG=Palette.BACKGROUND,TEXT=Palette.TEXT,MUTED=Palette.MUTED,BORDER=Palette.BORDER,GREEN=Palette.SUCCESS,ORANGE=Palette.WARNING;
    private StudentDatabaseHelper database;
    private Teacher teacher;
    private LinearLayout content;
    private List<TeacherBalance> balances=new ArrayList<>();
    private String selectedMonth;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);getWindow().setStatusBarColor(DARK);
        database=new StudentDatabaseHelper(this);
        teacher=database.getTeacher(getIntent().getLongExtra(EXTRA_TEACHER_ID,-1));
        if(teacher==null){Toast.makeText(this,"Professeur introuvable.",Toast.LENGTH_SHORT).show();finish();return;}
        LinearLayout root=column();root.setBackgroundColor(BG);
        root.addView(header(),new LinearLayout.LayoutParams(MATCH,dp(88)));
        ScrollView scroll=new ScrollView(this);content=column();content.setPadding(dp(12),dp(12),dp(12),dp(28));
        scroll.addView(content,new ScrollView.LayoutParams(MATCH,WRAP));
        root.addView(scroll,new LinearLayout.LayoutParams(MATCH,0,1));setContentView(root);
    }
    @Override protected void onResume(){super.onResume();if(content!=null)render();}

    private void render(){
        teacher=database.getTeacher(teacher.id);if(teacher==null){finish();return;}
        balances=database.getTeacherMonthlyBalances(teacher.id);
        if(selectedMonth==null&&!balances.isEmpty())selectedMonth=balances.get(0).dueMonth;
        content.removeAllViews();
        content.addView(profile());
        content.addView(kpis(),margin(MATCH,WRAP,0,dp(12),0,0));
        content.addView(monthlyCard(),margin(MATCH,WRAP,0,dp(12),0,0));
        content.addView(distributionCard(),margin(MATCH,WRAP,0,dp(12),0,0));
        TextView add=text("⊕  Nouveau règlement",17,Color.WHITE,true);
        add.setGravity(Gravity.CENTER);add.setBackground(gradient(TEAL,DARK,dp(13)));boolean canPay=selectedMonth!=null&&selectedBalance()!=null&&selectedBalance().remainingAmount>0;add.setAlpha(canPay?1f:.55f);add.setEnabled(canPay);
        add.setOnClickListener(v->openSettlement(selectedMonth));
        content.addView(add,margin(MATCH,dp(58),0,dp(14),0,0));
    }

    private View header(){
        LinearLayout bar=row();bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(14),0,dp(16),0);
        bar.setBackground(gradient(TEAL,DARK,0));
        ImageView back=icon(R.drawable.ic_arrow_back_24,Color.WHITE);back.setOnClickListener(v->finish());
        bar.addView(back,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout title=column();title.addView(text("Fiche professeur",20,Color.WHITE,true));
        title.addView(text("Informations et salaires mensuels",13,0xffd7efed,false),margin(MATCH,WRAP,0,dp(3),0,0));
        bar.addView(title,new LinearLayout.LayoutParams(0,WRAP,1));bar.addView(text("CentrePay",17,Color.WHITE,true));
        return bar;
    }

    private View profile(){
        LinearLayout card=card();LinearLayout line=row();line.setGravity(Gravity.CENTER_VERTICAL);
        TextView avatar=text(initials(teacher.fullName),20,DARK,true);avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(0xffd9f2ef,dp(38),Color.TRANSPARENT,0));
        line.addView(avatar,new LinearLayout.LayoutParams(dp(64),dp(64)));
        LinearLayout copy=column();copy.addView(text(teacher.fullName,18,TEXT,true));
        copy.addView(text(teacher.subjects+" · "+teacher.level.replace(",",", "),13,MUTED,false),margin(MATCH,WRAP,0,dp(4),0,0));
        copy.addView(text((teacher.phone==null||teacher.phone.trim().isEmpty()?"Téléphone non renseigné":teacher.phone)+" · "+database.getTeacherStudentCount(teacher.id)+" élève(s) affecté(s)",13,MUTED,false),margin(MATCH,WRAP,0,dp(5),0,0));
        line.addView(copy,margin(0,WRAP,dp(12),0,0,0,1));
        TextView status=!teacher.active?pill("Inactif",MUTED,0xfff1f5f9):allRemaining()==0?pill("✓  À jour",GREEN,0xffe7f8ef):pill("À verser",ORANGE,0xfffff0e4);line.addView(status);ImageView more=icon(R.drawable.ic_more_vert_24,MUTED);more.setPadding(dp(3),dp(3),dp(3),dp(3));more.setOnClickListener(v->showActions());line.addView(more,margin(dp(30),dp(34),dp(7),0,0,0));
        card.addView(line);return card;
    }

    private View kpis(){
        LinearLayout card=card();LinearLayout first=row(),second=row();
        int earned=0,paid=0,open=0;
        for(TeacherBalance b:balances){earned+=b.earnedAmount;paid+=b.paidAmount;if(b.remainingAmount>0)open++;}
        first.addView(metric(R.drawable.ic_analytics_24,"Total cumulé",earned,TEAL,0xffeaf7ff),weight(0,WRAP,0,0,dp(5),0));
        first.addView(metric(R.drawable.ic_check_circle_24,"Déjà versé",paid,GREEN,0xffeaf9f3),weight(0,WRAP,dp(5),0,0,0));
        second.addView(metric(R.drawable.ic_schedule_24,"Reste",Math.max(0,earned-paid),ORANGE,0xfffff3e9),weight(0,WRAP,0,dp(10),dp(5),0));
        second.addView(metric(R.drawable.ic_groups_24,"Mois ouverts",open,0xff5b21b6,0xfff1efff),weight(0,WRAP,dp(5),dp(10),0,0));
        card.addView(first);card.addView(second);return card;
    }
    private View metric(int drawable,String label,int amount,int color,int bg){
        LinearLayout box=column();box.setPadding(dp(11),dp(11),dp(8),dp(11));box.setBackground(round(bg,dp(12),Color.TRANSPARENT,0));
        box.addView(icon(drawable,color),new LinearLayout.LayoutParams(dp(25),dp(25)));
        box.addView(text(label,12,color,false),margin(MATCH,WRAP,0,dp(7),0,0));
        box.addView(text(amount+(label.equals("Mois ouverts")?"":" DH"),19,color,true),margin(MATCH,WRAP,0,dp(4),0,0));
        return box;
    }

    private View monthlyCard(){
        LinearLayout card=card();
        LinearLayout title=row();title.setGravity(Gravity.CENTER_VERTICAL);
        title.addView(icon(R.drawable.ic_calendar_month_24,TEAL),new LinearLayout.LayoutParams(dp(25),dp(25)));
        title.addView(text("Détail mensuel",17,TEXT,true),margin(0,WRAP,dp(9),0,0,0,1));
        title.addView(text("Année scolaire",12,MUTED,false));card.addView(title);
        if(balances.isEmpty()){
            card.addView(text("Aucun mois disponible. Les salaires apparaîtront lorsque des paiements d’élèves seront enregistrés.",14,MUTED,false),margin(MATCH,WRAP,0,dp(16),0,0));return card;
        }
        LinearLayout months=row();
        int count=Math.min(4,balances.size());
        for(int i=count-1;i>=0;i--){TeacherBalance b=balances.get(i);boolean active=b.dueMonth.equals(selectedMonth);
            TextView chip=text(shortMonth(b.dueMonth),13,active?Color.WHITE:TEXT,true);chip.setGravity(Gravity.CENTER);
            chip.setBackground(round(active?TEAL:0xffeef3f7,dp(11),Color.TRANSPARENT,0));
            chip.setOnClickListener(v->{selectedMonth=b.dueMonth;render();});
            months.addView(chip,weight(0,dp(40),0,dp(12),dp(6),0));
        }
        card.addView(months);
        for(TeacherBalance b:balances)card.addView(monthRow(b),margin(MATCH,WRAP,0,dp(10),0,0));
        return card;
    }
    private View monthRow(TeacherBalance balance){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(10),dp(10),dp(10));
        row.setBackground(round(balance.dueMonth.equals(selectedMonth)?0xfff0fdfa:Color.WHITE,dp(11),0xffd8e2ec,dp(1)));
        ImageView calendar=icon(R.drawable.ic_calendar_month_24,TEAL);row.addView(calendar,new LinearLayout.LayoutParams(dp(23),dp(23)));
        LinearLayout month=column();month.addView(text(monthLabel(balance.dueMonth),13,TEXT,true));
        row.addView(month,margin(0,WRAP,dp(8),0,dp(8),0,1));
        LinearLayout amounts=column();amounts.addView(text("Dû "+balance.earnedAmount+" DH",12,TEXT,true));
        amounts.addView(text("Versé "+balance.paidAmount+" · Reste "+balance.remainingAmount+" DH",11,MUTED,false),margin(MATCH,WRAP,0,dp(3),0,0));
        row.addView(amounts,new LinearLayout.LayoutParams(0,WRAP,2));
        int color=balance.remainingAmount==0?GREEN:balance.paidAmount>0?ORANGE:Palette.WARNING;
        String status=balance.remainingAmount==0?"Payé":balance.paidAmount>0?"Partiel":"À verser";
        row.addView(pill(status,color,balance.remainingAmount==0?0xffe7f8ef:0xfffff0e4));
        row.setOnClickListener(v->{selectedMonth=balance.dueMonth;render();});
        return row;
    }

    private View distributionCard(){
        LinearLayout card=card();LinearLayout title=row();title.setGravity(Gravity.CENTER_VERTICAL);
        title.addView(icon(R.drawable.ic_analytics_24,TEAL),new LinearLayout.LayoutParams(dp(25),dp(25)));
        title.addView(text("Répartition du mois sélectionné",16,TEXT,true),margin(0,WRAP,dp(9),0,0,0,1));
        title.addView(text(selectedMonth==null?"":monthLabel(selectedMonth),12,MUTED,false));card.addView(title);
        if(selectedMonth==null)return card;
        List<TeacherLevelBalance> levels=database.getTeacherLevelBalances(teacher.id,selectedMonth);
        if(levels.isEmpty()){card.addView(text("Aucune part encaissée pour ce mois.",13,MUTED,false),margin(MATCH,WRAP,0,dp(14),0,0));return card;}
        LinearLayout row=row();int total=0;
        for(TeacherLevelBalance level:levels){total+=level.totalAmount;LinearLayout box=column();box.setPadding(dp(8),dp(10),dp(8),dp(10));box.setBackground(round(0xffeaf7f6,dp(10),Color.TRANSPARENT,0));box.addView(text(level.level,12,DARK,false));box.addView(text(level.totalAmount+" DH",15,DARK,true),margin(MATCH,WRAP,0,dp(4),0,0));row.addView(box,weight(0,WRAP,0,dp(12),dp(6),0));}
        card.addView(row);
        TeacherBalance selected=selectedBalance();if(selected!=null){LinearLayout subjects=row();addSubject(subjects,"Maths",selected.mathAmount,TEAL,0xffe6f7f4);addSubject(subjects,"Français",selected.frenchAmount,Palette.BLUE,Palette.BLUE_SOFT);addSubject(subjects,"PC",selected.pcAmount,0xff7e22ce,0xffede9fe);addSubject(subjects,"SVT",selected.svtAmount,0xffc45d08,0xfffff1e6);card.addView(subjects,margin(MATCH,WRAP,0,dp(10),0,0));}
        TextView sum=text("Total du mois : "+total+" DH",14,TEXT,true);sum.setPadding(dp(12),dp(12),dp(12),dp(12));sum.setBackground(round(0xffeaf3ff,dp(10),Color.TRANSPARENT,0));card.addView(sum,margin(MATCH,WRAP,0,dp(10),0,0));return card;
    }
    private void addSubject(LinearLayout line,String label,int amount,int color,int bg){if(amount<=0)return;LinearLayout box=column();box.setGravity(Gravity.CENTER);box.setPadding(dp(5),dp(8),dp(5),dp(8));box.setBackground(round(bg,dp(10),0,0));box.addView(text(label,11,color,false));box.addView(text(amount+" DH",13,color,true),margin(MATCH,WRAP,0,dp(3),0,0));line.addView(box,weight(0,WRAP,0,0,dp(6),0));}
    private TeacherBalance selectedBalance(){if(selectedMonth==null)return null;for(TeacherBalance b:balances)if(selectedMonth.equals(b.dueMonth))return b;return null;}
    private void showActions(){ArrayList<String> actions=new ArrayList<>();actions.add("Nouveau règlement");if(teacher.phone!=null&&!teacher.phone.trim().isEmpty())actions.add("Appeler "+teacher.phone);new AlertDialog.Builder(this).setTitle(teacher.fullName).setItems(actions.toArray(new String[0]),(d,w)->{if(w==0)openSettlement(selectedMonth);else startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+teacher.phone.replace(" ",""))));}).show();}
    private void openSettlement(String month){
        if(month==null){Toast.makeText(this,"Aucun mois disponible.",Toast.LENGTH_SHORT).show();return;}
        TeacherBalance selected=database.getTeacherBalance(teacher.id,month);
        if(selected==null||selected.remainingAmount<=0){Toast.makeText(this,"Aucun montant à verser pour ce mois.",Toast.LENGTH_SHORT).show();return;}
        Intent intent=new Intent(this,AddTeacherSettlementActivity.class);
        intent.putExtra(AddTeacherSettlementActivity.EXTRA_TEACHER_ID,teacher.id);
        intent.putExtra(AddTeacherSettlementActivity.EXTRA_DUE_MONTH,month);startActivity(intent);
    }
    private int allRemaining(){int result=0;for(TeacherBalance b:balances)result+=b.remainingAmount;return result;}
    private String initials(String name){String[] parts=name.trim().split("\\s+");return parts.length<2?name.substring(0,Math.min(2,name.length())).toUpperCase():(parts[0].substring(0,1)+parts[parts.length-1].substring(0,1)).toUpperCase();}
    private String shortMonth(String value){String label=monthLabel(value);return label.substring(0,Math.min(3,label.length()));}
    private String monthLabel(String value){String[] p=value.split("-");if(p.length!=2)return value;String[] names={"Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"};try{int month=Integer.parseInt(p[0].length()==4?p[1]:p[0]);String year=p[0].length()==4?p[0]:p[1];return names[month-1]+" "+year;}catch(Exception e){return value;}}
    private TextView pill(String value,int color,int bg){TextView p=text(value,11,color,true);p.setPadding(dp(8),dp(7),dp(8),dp(7));p.setBackground(round(bg,dp(20),Color.TRANSPARENT,0));return p;}
    private LinearLayout card(){LinearLayout c=column();c.setPadding(dp(13),dp(14),dp(13),dp(14));c.setBackground(round(Color.WHITE,dp(16),BORDER,dp(1)));c.setElevation(dp(2));return c;}
    private ImageView icon(int res,int color){ImageView v=new ImageView(this);v.setImageResource(res);v.setImageTintList(ColorStateList.valueOf(color));v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);return v;}
    private TextView text(String value,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setIncludeFontPadding(false);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);return v;}
    private GradientDrawable round(int color,int radius,int stroke,int width){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(width>0)d.setStroke(width,stroke);return d;}
    private GradientDrawable gradient(int a,int b,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{a,b});d.setCornerRadius(radius);return d;}
    private LinearLayout.LayoutParams margin(int width,int height,int left,int top,int right,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height);p.setMargins(left,top,right,bottom);return p;}
    private LinearLayout.LayoutParams margin(int width,int height,int left,int top,int right,int bottom,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height,weight);p.setMargins(left,top,right,bottom);return p;}
    private LinearLayout.LayoutParams weight(int width,int height,int left,int top,int right,int bottom){return margin(width,height,left,top,right,bottom,1);}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private static final int MATCH=ViewGroup.LayoutParams.MATCH_PARENT,WRAP=ViewGroup.LayoutParams.WRAP_CONTENT;
}
