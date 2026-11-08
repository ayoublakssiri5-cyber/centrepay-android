package com.centrepay.mobile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;

public class PaymentListActivity extends Activity {
    private static final int NAVY=Palette.PRIMARY_DARK,TEAL=Palette.PRIMARY,TEXT=Palette.TEXT,MUTED=Palette.MUTED,SURFACE=Palette.SURFACE,BORDER=Palette.BORDER,BG=Palette.BACKGROUND,GREEN=Palette.SUCCESS,GREEN_BG=Palette.SUCCESS_SOFT;
    private LinearLayout paymentList;
    private StudentDatabaseHelper database;
    private String selectedMonth;
    private HorizontalScrollView monthFilters;
    private EditText search;
    private String searchQuery="",selectedStatus="",selectedSort="Récent";
    private LinearLayout summaryHost;
    private Map<Long,StudentMonthBalance> monthBalances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        database = new StudentDatabaseHelper(this);
        Calendar now=Calendar.getInstance();selectedMonth=String.format(Locale.FRANCE,"%02d/%04d",now.get(Calendar.MONTH)+1,now.get(Calendar.YEAR));

        getWindow().setNavigationBarColor(SURFACE);LinearLayout root = column();root.setBackgroundColor(BG);root.addView(hero(), new LinearLayout.LayoutParams(MATCH, dp(124)));
        LinearLayout content = column();content.setPadding(dp(16),dp(16),dp(16),0);content.addView(searchBar(),new LinearLayout.LayoutParams(MATCH,dp(52)));monthFilters=(HorizontalScrollView)filters();content.addView(monthFilters,margins(MATCH,dp(48),0,dp(12),0,dp(10)));summaryHost=column();content.addView(summaryHost,margins(MATCH,WRAP,0,0,0,dp(10)));

        ScrollView scroll = new ScrollView(this);
        paymentList = column();
        scroll.addView(paymentList, new ScrollView.LayoutParams(MATCH, WRAP));
        content.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        root.addView(content, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        root.addView(bottomNav());
        FrameLayout frame=new FrameLayout(this);frame.addView(root);LinearLayout fab=column();fab.setGravity(Gravity.CENTER);TextView plus=text("+",32,Color.WHITE,false);plus.setGravity(Gravity.CENTER);plus.setBackground(rounded(TEAL,dp(28),0,0));fab.addView(plus,new LinearLayout.LayoutParams(dp(56),dp(56)));TextView label=text("Nouveau\npaiement",11,NAVY,true);label.setGravity(Gravity.CENTER);fab.addView(label);fab.setOnClickListener(v->startActivity(new Intent(this,AddPaymentActivity.class)));FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(82),dp(88),Gravity.RIGHT|Gravity.BOTTOM);fp.setMargins(0,0,dp(8),dp(76));frame.addView(fab,fp);setContentView(frame);
    }

    @Override protected void onResume() { super.onResume();monthBalances=null; if (paymentList != null) renderPayments(); }

    private View searchBar(){LinearLayout outer=row();outer.setGravity(Gravity.CENTER_VERTICAL);LinearLayout bar=row();bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(12),0,dp(10),0);bar.setBackground(rounded(SURFACE,dp(12),BORDER,dp(1)));bar.addView(icon(R.drawable.ic_search_24,NAVY),new LinearLayout.LayoutParams(dp(34),MATCH));search=new EditText(this);search.setHint("Rechercher un élève...");search.setHintTextColor(MUTED);search.setTextColor(TEXT);search.setTextSize(14);search.setSingleLine();search.setBackgroundColor(Color.TRANSPARENT);search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){searchQuery=s.toString().trim();if(paymentList!=null)renderPayments();}public void afterTextChanged(Editable e){}});bar.addView(search,new LinearLayout.LayoutParams(0,MATCH,1));outer.addView(bar,new LinearLayout.LayoutParams(0,MATCH,1));FrameLayout button=new FrameLayout(this);button.setBackground(rounded(SURFACE,dp(12),BORDER,dp(1)));button.addView(icon(R.drawable.ic_tune_24,NAVY),new FrameLayout.LayoutParams(dp(24),dp(24),Gravity.CENTER));button.setOnClickListener(v->showSortMenu());outer.addView(button,margins(dp(48),MATCH,dp(10),0,0,0));return outer;}
    private View filters(){HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);LinearLayout line=row();String current=currentMonth();String previous=previousMonth();TextView currentChip=filter("Ce mois",selectedMonth.equals(current),R.drawable.ic_calendar_month_24);currentChip.setOnClickListener(v->selectMonth(current));line.addView(currentChip,margins(WRAP,dp(40),0,0,dp(8),0));TextView previousChip=filter("Mois précédent",selectedMonth.equals(previous),0);previousChip.setOnClickListener(v->selectMonth(previous));line.addView(previousChip,margins(WRAP,dp(40),0,0,dp(8),0));TextView choose=filter("Choisir un mois",!selectedMonth.equals(current)&&!selectedMonth.equals(previous),R.drawable.ic_calendar_month_24);choose.setOnClickListener(v->showMonthPicker());line.addView(choose,margins(WRAP,dp(40),0,0,dp(12),0));addStatusFilter(line,"Payé","paid",R.drawable.ic_check_circle_24);addStatusFilter(line,"Partiel","partial",R.drawable.ic_schedule_24);addStatusFilter(line,"Impayé","unpaid",R.drawable.ic_error_24);scroll.addView(line);return scroll;}
    private void addStatusFilter(LinearLayout line,String label,String value,int icon){TextView chip=filter(label,selectedStatus.equals(value),icon);chip.setOnClickListener(v->{selectedStatus=selectedStatus.equals(value)?"":value;refreshMonthFilters();renderPayments();});line.addView(chip,margins(WRAP,dp(40),0,0,dp(8),0));}
    private String currentMonth(){Calendar c=Calendar.getInstance();return String.format(Locale.FRANCE,"%02d/%04d",c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR));}
    private String previousMonth(){Calendar c=Calendar.getInstance();c.add(Calendar.MONTH,-1);return String.format(Locale.FRANCE,"%02d/%04d",c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR));}
    private void selectMonth(String month){selectedMonth=month;monthBalances=null;refreshMonthFilters();renderPayments();}
    private void refreshMonthFilters(){if(monthFilters==null)return;ViewGroup parent=(ViewGroup)monthFilters.getParent();int index=parent.indexOfChild(monthFilters);parent.removeViewAt(index);monthFilters=(HorizontalScrollView)filters();parent.addView(monthFilters,index,margins(MATCH,dp(48),0,dp(12),0,dp(10)));}
    private void showMonthPicker(){String[] parts=selectedMonth.split("/");int month=Integer.parseInt(parts[0])-1,year=Integer.parseInt(parts[1]);DatePickerDialog dialog=new DatePickerDialog(this,(view,y,m,d)->selectMonth(String.format(Locale.FRANCE,"%02d/%04d",m+1,y)),year,month,1);dialog.getDatePicker().setCalendarViewShown(false);dialog.setTitle("Choisir le mois payé");dialog.show();}
    private TextView filter(String value,boolean active,int drawable){int color=active?Color.WHITE:(value.contains("Impayé")?Palette.ERROR:value.contains("Partiel")?Color.rgb(217,119,6):value.contains("Payé")?GREEN:TEXT);TextView v=text(value,14,color,true);v.setGravity(Gravity.CENTER);v.setPadding(dp(15),0,dp(15),0);if(drawable!=0){v.setCompoundDrawablesWithIntrinsicBounds(drawable,0,0,0);v.setCompoundDrawableTintList(ColorStateList.valueOf(color));v.setCompoundDrawablePadding(dp(7));}int bg=active?TEAL:value.contains("Impayé")?Palette.ERROR_SOFT:value.contains("Partiel")?Palette.WARNING_SOFT:value.contains("Payé")?GREEN_BG:SURFACE;v.setBackground(rounded(bg,dp(20),active?Color.TRANSPARENT:BORDER,active?0:dp(1)));return v;}

    private void renderPayments() {
        paymentList.removeAllViews();if(monthBalances==null)monthBalances=database.getMonthBalances(selectedMonth);
        List<PaymentRecord> allPayments=database.getRecentPayments();List<PaymentRecord> payments=new ArrayList<>();for(PaymentRecord payment:allPayments){StudentMonthBalance balance=monthBalances.get(payment.studentId);boolean month=selectedMonth.equals(payment.referenceMonth),query=searchQuery.isEmpty()||payment.studentName.toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))||payment.paymentDate.contains(searchQuery);if(month&&query&&matchesStatus(balance))payments.add(payment);}
        if("unpaid".equals(selectedStatus)){for(Student student:database.findStudents(searchQuery)){StudentMonthBalance b=monthBalances.get(student.id);if(b!=null&&b.paidAmount<=0)payments.add(new PaymentRecord(-student.id,student.id,student.fullName(),student.level,0,0,0,0,0,0,0,0,0,"Aucun règlement",selectedMonth,""));}}
        if("Nom".equals(selectedSort))Collections.sort(payments,Comparator.comparing(p->p.studentName,String.CASE_INSENSITIVE_ORDER));else if("Montant".equals(selectedSort))Collections.sort(payments,(a,b)->Integer.compare(b.amount,a.amount));
        renderSummary(payments);
        if (payments.isEmpty()) {
            TextView empty = text("Aucun paiement enregistré.\nUtilisez « Saisir un paiement » pour commencer.", 14, MUTED, false);
            empty.setText("Aucun paiement pour le mois "+selectedMonth+".");empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(38), dp(16), dp(38));
            empty.setBackground(rounded(SURFACE, dp(14), BORDER, dp(1)));
            paymentList.addView(empty);
            return;
        }
        for (PaymentRecord payment : payments) paymentList.addView(paymentCard(payment,monthBalances.get(payment.studentId)), margins(MATCH, WRAP, 0, 0, 0, dp(10)));
    }

    private View paymentCard(PaymentRecord payment,StudentMonthBalance balance) {
        LinearLayout card = column();card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setElevation(dp(2));card.setBackground(rounded(SURFACE,dp(12),Color.TRANSPARENT,0));
        LinearLayout top = row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView avatar=text(initials(payment.studentName),16,NAVY,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(rounded(Color.rgb(223,247,236),dp(25),0,0));top.addView(avatar,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout identity = column();
        identity.addView(text(payment.studentName,17,TEXT,true));identity.addView(text("▣  "+payment.referenceMonth,13,MUTED,false));identity.addView(text(payment.id<0?"◷  Aucun règlement":"◷  Reçu le "+payment.paymentDate,12,MUTED,false));top.addView(identity,margins(0,WRAP,dp(10),0,dp(6),0,1));LinearLayout amount=column();amount.setGravity(Gravity.RIGHT);amount.addView(text(payment.amount+" DH",20,TEXT,true));amount.addView(statusView(balance),margins(WRAP,WRAP,0,dp(5),0,0));top.addView(amount);ImageView menu=icon(R.drawable.ic_more_vert_24,MUTED);menu.setPadding(dp(3),dp(3),dp(3),dp(3));menu.setOnClickListener(v->showPaymentActions(payment));top.addView(menu,margins(dp(30),dp(38),dp(4),0,0,0));
        card.addView(top);
        if (payment.mathAmount + payment.frenchAmount + payment.pcAmount + payment.svtAmount + payment.centerAmount > 0) {
            LinearLayout parts=row();if(payment.mathAmount>0)parts.addView(part("Maths",payment.mathAmount,Palette.PRIMARY_SOFT,NAVY),new LinearLayout.LayoutParams(0,dp(42),1));if(payment.frenchAmount>0)parts.addView(part("Français",payment.frenchAmount,Palette.BLUE_SOFT,Color.rgb(30,64,175)),margins(0,dp(42),dp(7),0,0,0,1));if(payment.pcAmount>0)parts.addView(part("PC",payment.pcAmount,0xfff3e8ff,0xff7e22ce),margins(0,dp(42),dp(7),0,0,0,1));if(payment.svtAmount>0)parts.addView(part("SVT",payment.svtAmount,0xfffff1e6,0xffc45d08),margins(0,dp(42),dp(7),0,0,0,1));if(payment.centerAmount>0)parts.addView(part("Centre",payment.centerAmount,Color.rgb(241,245,249),MUTED),margins(0,dp(42),dp(7),0,0,0,1));card.addView(parts,margins(MATCH,WRAP,0,dp(12),0,0));
        }
        if (payment.advanceAmount > 0) {
            String advance = "Avance créée : " + payment.advanceAmount + " DH";
            if (payment.advanceBalance > 0) advance += " · disponible : " + payment.advanceBalance + " DH";
            card.addView(text(advance, 11, Color.rgb(208, 141, 23), true), margins(MATCH, WRAP, 0, dp(7), 0, 0));
        }
        if (!payment.note.isEmpty()) card.addView(text("Note : " + payment.note, 11, MUTED, false), margins(MATCH, WRAP, 0, dp(7), 0, 0));
        card.setOnClickListener(v->openStudent(payment.studentId));return card;
    }
    private boolean matchesStatus(StudentMonthBalance balance){if(selectedStatus.isEmpty())return true;if(balance==null)return false;if("paid".equals(selectedStatus))return balance.paidAmount>=balance.totalAmount;if("partial".equals(selectedStatus))return balance.paidAmount>0&&balance.paidAmount<balance.totalAmount;return "unpaid".equals(selectedStatus)&&balance.paidAmount<=0;}
    private TextView statusView(StudentMonthBalance b){String label;int fg,bg,icon;if(b==null){label="Sans échéance";fg=MUTED;bg=0xfff1f5f9;icon=R.drawable.ic_calendar_month_24;}else if(b.paidAmount>=b.totalAmount){label="Payé";fg=GREEN;bg=GREEN_BG;icon=R.drawable.ic_check_circle_24;}else if(b.paidAmount>0){label="Partiel";fg=0xffd97706;bg=Palette.WARNING_SOFT;icon=R.drawable.ic_schedule_24;}else{label="Impayé";fg=0xffdc2626;bg=Palette.ERROR_SOFT;icon=R.drawable.ic_error_24;}TextView status=text(label,12,fg,true);status.setGravity(Gravity.CENTER);status.setPadding(dp(10),dp(5),dp(10),dp(5));status.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);status.setCompoundDrawableTintList(ColorStateList.valueOf(fg));status.setCompoundDrawablePadding(dp(5));status.setBackground(rounded(bg,dp(16),0,0));return status;}
    private void renderSummary(List<PaymentRecord> payments){if(summaryHost==null)return;summaryHost.removeAllViews();int total=0,advance=0;for(PaymentRecord p:payments){if(p.id>0){total+=p.amount;advance+=p.advanceAmount;}}LinearLayout card=row();card.setPadding(dp(14),dp(11),dp(14),dp(11));card.setGravity(Gravity.CENTER_VERTICAL);card.setBackground(rounded(SURFACE,dp(14),BORDER,dp(1)));LinearLayout received=column();received.addView(text("Encaissé en "+selectedMonth,11,MUTED,false));received.addView(text(total+" DH",19,NAVY,true),margins(MATCH,WRAP,0,dp(3),0,0));card.addView(received,new LinearLayout.LayoutParams(0,WRAP,1));LinearLayout count=column();count.setGravity(Gravity.RIGHT);count.addView(text(payments.size()+" élément"+(payments.size()>1?"s":""),13,TEXT,true));count.addView(text(advance>0?"Avances : "+advance+" DH":"Tri : "+selectedSort,11,MUTED,false),margins(WRAP,WRAP,0,dp(4),0,0));card.addView(count);summaryHost.addView(card);}
    private void showSortMenu(){String[] options={"Plus récents","Nom de l’élève","Montant décroissant"};new AlertDialog.Builder(this).setTitle("Trier les paiements").setItems(options,(d,which)->{selectedSort=which==1?"Nom":which==2?"Montant":"Récent";renderPayments();}).show();}
    private void showPaymentActions(PaymentRecord payment){String first=payment.id<0?"Saisir un paiement":"Voir la fiche élève";String second=payment.id<0?"Voir la fiche élève":"Saisir un autre paiement";new AlertDialog.Builder(this).setTitle(payment.studentName).setItems(new String[]{first,second},(d,which)->{boolean pay=payment.id<0?which==0:which==1;if(pay){Intent i=new Intent(this,AddPaymentActivity.class);i.putExtra("student_id",payment.studentId);i.putExtra("reference_month",selectedMonth.substring(3)+"-"+selectedMonth.substring(0,2));startActivity(i);}else openStudent(payment.studentId);}).show();}
    private void openStudent(long id){Intent i=new Intent(this,StudentDetailActivity.class);i.putExtra("student_id",id);startActivity(i);}
    private TextView part(String name,int value,int bg,int fg){TextView v=text(name+"  "+value+" DH",11,fg,true);v.setGravity(Gravity.CENTER);v.setBackground(rounded(bg,dp(14),0,0));return v;}
    private String initials(String name){String[] p=name.trim().split("\\s+");return(p[0].substring(0,1)+(p.length>1?p[p.length-1].substring(0,1):"")).toUpperCase();}

    private View hero(){LinearLayout h=column();h.setPadding(dp(20),dp(14),dp(20),dp(12));GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{NAVY,TEAL});g.setCornerRadii(new float[]{0,0,0,0,dp(16),dp(16),dp(16),dp(16)});h.setBackground(g);LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.addView(text("Centre",25,Color.WHITE,true));top.addView(text("Pay",25,Color.rgb(198,165,93),true));top.addView(icon(R.drawable.ic_notifications_24,Color.WHITE),new LinearLayout.LayoutParams(0,dp(34),1));TextView p=text("MA",14,Color.WHITE,true);p.setGravity(Gravity.CENTER);p.setBackground(rounded(Color.argb(70,255,255,255),dp(20),0,0));top.addView(p,new LinearLayout.LayoutParams(dp(40),dp(40)));h.addView(top);h.addView(text("Paiements",15,Color.WHITE,true));h.addView(text("Historique des paiements",13,Color.rgb(236,239,224),false));return h;}
    private View bottomNav(){return BottomTabBar.create(this,PaymentListActivity.class);}
    private void addTab(LinearLayout bar,int res,String label,boolean selected,View.OnClickListener click){LinearLayout tab=column();tab.setGravity(Gravity.CENTER);if(click!=null)tab.setOnClickListener(click);FrameLayout box=new FrameLayout(this);box.addView(icon(res,selected?TEAL:MUTED),new FrameLayout.LayoutParams(dp(28),dp(28),Gravity.CENTER));if(selected)box.setBackground(rounded(Palette.PRIMARY_SOFT,dp(18),0,0));tab.addView(box,new LinearLayout.LayoutParams(dp(72),dp(34)));TextView name=text(label,12,selected?NAVY:MUTED,selected);name.setGravity(Gravity.CENTER);tab.addView(name);bar.addView(tab,new LinearLayout.LayoutParams(0,MATCH,1));}
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
