package com.centrepay.mobile;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Student group directory matching the approved premium visual. */
public class StudentListActivity extends Activity {
    private static final int BROWN = Palette.PRIMARY, BROWN_LIGHT = Palette.PRIMARY_DARK, TERRACOTTA = Palette.PRIMARY, TERRACOTTA_SOFT = Palette.PRIMARY_SOFT, CREAM = Palette.BACKGROUND, SURFACE = Palette.SURFACE, TEXT = Palette.TEXT, MUTED = Palette.MUTED, GREEN = Palette.SUCCESS, GREEN_SOFT = Palette.SUCCESS_SOFT, AMBER = Palette.WARNING, AMBER_SOFT = Palette.WARNING_SOFT, BORDER = Palette.BORDER;
    private StudentDatabaseHelper database;
    private LinearLayout list, groupChips;
    private EditText search;
    private String selectedGroup = "";
    private String selectedStatus = "";
    private String selectedSort = "Nom";
    private Map<Long, StudentMonthBalance> monthBalances;
    private TextView resultTitle, sortButton;
    private LinearLayout overviewHost;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); getWindow().setStatusBarColor(BROWN); getWindow().setNavigationBarColor(CREAM); getWindow().setDecorFitsSystemWindows(true); database = new StudentDatabaseHelper(this);
        LinearLayout app = column(); app.setBackgroundColor(CREAM); app.addView(createHero());
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); LinearLayout content = column();
        LinearLayout body = column(); body.setPadding(dp(16), dp(18), dp(16), dp(20));
        body.addView(searchBar());
        HorizontalScrollView groupsScroll = new HorizontalScrollView(this); groupsScroll.setHorizontalScrollBarEnabled(false); groupChips = row(); groupsScroll.addView(groupChips, new HorizontalScrollView.LayoutParams(WRAP, WRAP)); body.addView(groupsScroll, margins(MATCH, dp(48), 0, dp(8), 0, dp(16))); renderGroupChips();
        overviewHost = column(); body.addView(overviewHost,margins(MATCH,WRAP,0,0,0,dp(14))); renderOverview();
        LinearLayout resultHeader = row(); resultHeader.setGravity(Gravity.CENTER_VERTICAL); resultTitle = text("", 21, TEXT, true); resultHeader.addView(resultTitle, new LinearLayout.LayoutParams(0, WRAP, 1f)); sortButton = text("Trier : Nom  ⌄", 14, BROWN, true);sortButton.setPadding(dp(10),dp(8),dp(10),dp(8));sortButton.setBackground(rounded(SURFACE,dp(12),BORDER,dp(1)));sortButton.setOnClickListener(v->showSortMenu());resultHeader.addView(sortButton); body.addView(resultHeader, margins(MATCH, WRAP, 0, dp(4), 0, dp(12)));
        list = column(); body.addView(list);
        content.addView(body); scroll.addView(content, new ScrollView.LayoutParams(MATCH, WRAP)); app.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f)); app.addView(createBottomNavigation()); FrameLayout frame=new FrameLayout(this);frame.addView(app);TextView fab=text("+",32,Color.WHITE,false);fab.setGravity(Gravity.CENTER);fab.setElevation(dp(8));fab.setBackground(rounded(BROWN,dp(28),Color.TRANSPARENT,0));fab.setOnClickListener(v->startActivity(new Intent(this,AddStudentActivity.class)));FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(56),dp(56),Gravity.RIGHT|Gravity.BOTTOM);fp.setMargins(0,0,dp(18),dp(88));frame.addView(fab,fp);setContentView(frame);
        search.addTextChangedListener(new TextWatcher() { @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { } @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderStudents(s.toString()); } @Override public void afterTextChanged(Editable s) { } });
    }

    @Override protected void onResume() { super.onResume(); monthBalances = null; if (overviewHost != null) renderOverview(); if (list != null) renderStudents(search.getText().toString()); }

    private View createHero() {
        LinearLayout hero=column();hero.setPadding(dp(20),dp(14),dp(20),dp(14));GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{BROWN,BROWN_LIGHT});gradient.setCornerRadii(new float[]{0,0,0,0,dp(22),dp(22),dp(22),dp(22)});hero.setBackground(gradient);LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.addView(text("Centre",25,Color.WHITE,true));top.addView(text("Pay",25,Color.rgb(244,194,78),true));top.addView(new View(this),new LinearLayout.LayoutParams(0,dp(32),1));TextView profile=text("CP",14,Color.WHITE,true);profile.setGravity(Gravity.CENTER);profile.setBackground(rounded(Color.argb(70,255,255,255),dp(20),0,0));top.addView(profile,new LinearLayout.LayoutParams(dp(40),dp(40)));hero.addView(top);hero.addView(text("Élèves",16,Color.WHITE,true));hero.addView(text("Répertoire et situation du mois",13,Color.rgb(220,238,237),false));return hero;
    }

    private View searchBar() {
        LinearLayout shell = row(); shell.setGravity(Gravity.CENTER_VERTICAL); shell.setPadding(dp(14), 0, dp(12), 0); shell.setBackground(rounded(SURFACE, dp(16), BORDER, dp(1)));
        shell.addView(icon(R.drawable.ic_search_24,BROWN_LIGHT), new LinearLayout.LayoutParams(dp(34), dp(48)));
        search = new EditText(this); search.setSingleLine(true); search.setTextSize(16); search.setHint("Rechercher un élève"); search.setHintTextColor(Color.rgb(174, 164, 156)); search.setTextColor(TEXT); search.setPadding(0, 0, 0, 0); search.setBackgroundColor(Color.TRANSPARENT); shell.addView(search, new LinearLayout.LayoutParams(0, dp(48), 1f));
        return shell;
    }

    private void renderGroupChips() { groupChips.removeAllViews(); addGroupChip("Tous", ""); addGroupChip("1AC", "1AC"); addGroupChip("2AC", "2AC"); addGroupChip("3AC", "3AC"); String label=selectedStatus.isEmpty()?"Statut":statusLabel(selectedStatus); TextView status=text("  "+label+"  ⌄",14,selectedStatus.isEmpty()?TEXT:Color.WHITE,true);status.setGravity(Gravity.CENTER);status.setPadding(dp(14),0,dp(14),0);status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tune_24,0,0,0);status.setCompoundDrawableTintList(ColorStateList.valueOf(selectedStatus.isEmpty()?BROWN_LIGHT:Color.WHITE));status.setCompoundDrawablePadding(dp(7));status.setBackground(rounded(selectedStatus.isEmpty()?SURFACE:BROWN,dp(18),selectedStatus.isEmpty()?Color.rgb(203,213,225):Color.TRANSPARENT,selectedStatus.isEmpty()?dp(1):0));status.setOnClickListener(v->showStatusMenu());groupChips.addView(status,margins(WRAP,dp(42),dp(8),0,0,0)); }
    private void addGroupChip(String label, String value) { boolean selected = selectedGroup.equals(value); TextView chip = text(label, 15, selected ? SURFACE : TEXT, true); chip.setGravity(Gravity.CENTER); chip.setPadding(dp(18), 0, dp(18), 0); chip.setBackground(rounded(selected ? BROWN : SURFACE, dp(18), selected?Color.TRANSPARENT:Color.rgb(203,213,225), selected?0:dp(1))); chip.setOnClickListener(view -> { selectedGroup = value; renderGroupChips(); renderStudents(search.getText().toString()); }); groupChips.addView(chip, margins(WRAP, dp(42), 0, 0, dp(10), 0)); }

    private void renderStudents(String query) {
        list.removeAllViews(); if (monthBalances == null) monthBalances = database.getCurrentMonthBalances(); List<Student> students = new ArrayList<>(database.findStudents(query, selectedGroup));
        if (!selectedStatus.isEmpty()) students.removeIf(student -> !matchesStatus(monthBalances.get(student.id), selectedStatus));
        Comparator<Student> comparator;
        if ("Niveau".equals(selectedSort)) comparator=Comparator.comparing((Student s)->s.level, String.CASE_INSENSITIVE_ORDER).thenComparing(Student::fullName,String.CASE_INSENSITIVE_ORDER);
        else if ("Reste dû".equals(selectedSort)) comparator=(a,b)->Integer.compare(remaining(monthBalances.get(b.id)),remaining(monthBalances.get(a.id)));
        else comparator=Comparator.comparing(Student::fullName,String.CASE_INSENSITIVE_ORDER);
        Collections.sort(students,comparator);
        resultTitle.setText(students.size() + " élève" + (students.size() > 1 ? "s" : ""));
        if (students.isEmpty()) { TextView empty = text("Aucun élève ne correspond aux filtres sélectionnés.", 14, MUTED, false); empty.setGravity(Gravity.CENTER); empty.setPadding(dp(16), dp(35), dp(16), dp(35)); empty.setBackground(rounded(SURFACE, dp(20), BORDER, dp(1))); list.addView(empty); return; }
        for (Student student : students) list.addView(studentCard(student, monthBalances.get(student.id)), margins(MATCH, WRAP, 0, 0, 0, dp(12)));
    }

    private View studentCard(Student student, StudentMonthBalance balance) {
        LinearLayout card = row(); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14), dp(14), dp(12), dp(14)); card.setMinimumHeight(dp(102));card.setClickable(true); card.setOnClickListener(view -> openStudent(student)); card.setBackground(rounded(SURFACE, dp(18), Color.TRANSPARENT, 0)); card.setElevation(dp(3));
        TextView avatar = text(initials(student.fullName()), 16, BROWN_LIGHT, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(rounded(Color.rgb(223,247,236), dp(24), Color.TRANSPARENT, 0)); card.addView(avatar, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout details = column(); details.addView(oneLine(text(student.fullName(), 17, TEXT, true))); details.addView(text(student.level + "  •  " + student.subjects, 13, MUTED, false), margins(MATCH, WRAP, 0, dp(4), 0, 0));LinearLayout phone=row();phone.setGravity(Gravity.CENTER_VERTICAL);phone.addView(icon(R.drawable.ic_call_24,MUTED),new LinearLayout.LayoutParams(dp(19),dp(19)));phone.addView(oneLine(text(student.phone==null||student.phone.isEmpty()?"Téléphone non renseigné":student.phone,12,MUTED,false)),margins(0,WRAP,dp(6),0,0,0,1f));details.addView(phone,margins(MATCH,WRAP,0,dp(5),0,0));card.addView(details, margins(0, WRAP, dp(13), 0, dp(5), 0, 1f));
        LinearLayout right = row();right.setGravity(Gravity.CENTER_VERTICAL); TextView status = paymentStatus(balance); status.setGravity(Gravity.CENTER); right.addView(status);ImageView menu=icon(R.drawable.ic_more_vert_24,MUTED);menu.setContentDescription("Actions pour "+student.fullName());menu.setPadding(dp(4),dp(4),dp(4),dp(4));menu.setOnClickListener(v->{v.getParent().requestDisallowInterceptTouchEvent(true);showStudentMenu(student,balance);});right.addView(menu,margins(dp(32),dp(40),dp(4),0,0,0)); card.addView(right); return card;
    }

    /** One line with an ellipsis, so the status pill never squeezes the text onto several lines. */
    private TextView oneLine(TextView view) { view.setSingleLine(true); view.setEllipsize(android.text.TextUtils.TruncateAt.END); return view; }

    private TextView paymentStatus(StudentMonthBalance balance) {
        String label; int color; int tint;
        int statusIcon;
        if (balance == null) { label = "Sans échéance"; color = MUTED; tint = Color.rgb(241,245,249); statusIcon=R.drawable.ic_calendar_month_24; }
        else if (balance.paidAmount <= 0) { label = "À payer"; color = Palette.ERROR; tint = Palette.ERROR_SOFT; statusIcon=R.drawable.ic_error_24; }
        else if (balance.paidAmount >= balance.totalAmount) { label = "Réglé"; color = GREEN; tint = GREEN_SOFT; statusIcon=R.drawable.ic_check_circle_24; }
        else { label = "Partiel"; color = AMBER; tint = AMBER_SOFT; statusIcon=R.drawable.ic_schedule_24; }
        TextView view = text(label, 12, color, true); view.setGravity(Gravity.CENTER);view.setCompoundDrawablesWithIntrinsicBounds(statusIcon,0,0,0);view.setCompoundDrawableTintList(ColorStateList.valueOf(color));view.setCompoundDrawablePadding(dp(7));view.setPadding(dp(10), dp(5), dp(12), dp(5)); view.setBackground(rounded(tint, dp(16), Color.TRANSPARENT, 0)); return view;
    }

    private void openStudent(Student student) { Intent intent = new Intent(this, StudentDetailActivity.class); intent.putExtra("student_id", student.id); startActivity(intent); }

    private void showStudentMenu(Student student, StudentMonthBalance balance) {
        String[] actions={"Voir la fiche","Saisir un paiement"};
        new AlertDialog.Builder(this).setTitle(student.fullName()).setItems(actions,(dialog,which)->{
            if(which==0) openStudent(student); else { Intent intent=new Intent(this,AddPaymentActivity.class);intent.putExtra("student_id",student.id);startActivity(intent); }
        }).show();
    }

    private void showSortMenu(){String[] values={"Nom","Niveau","Reste dû"};new AlertDialog.Builder(this).setTitle("Trier les élèves par").setSingleChoiceItems(values,java.util.Arrays.asList(values).indexOf(selectedSort),(d,which)->{selectedSort=values[which];sortButton.setText("Trier : "+selectedSort+"  ⌄");d.dismiss();renderStudents(search.getText().toString());}).show();}
    private void showStatusMenu(){String[] labels={"Tous les statuts","Réglés","Partiels","À payer","Sans échéance"};String[] values={"","paid","partial","unpaid","none"};new AlertDialog.Builder(this).setTitle("Statut du mois").setSingleChoiceItems(labels,java.util.Arrays.asList(values).indexOf(selectedStatus),(d,which)->{selectedStatus=values[which];d.dismiss();renderGroupChips();renderStudents(search.getText().toString());}).show();}
    private String statusLabel(String value){if("paid".equals(value))return "Réglés";if("partial".equals(value))return "Partiels";if("unpaid".equals(value))return "À payer";return "Sans échéance";}
    private boolean matchesStatus(StudentMonthBalance balance,String value){if("none".equals(value))return balance==null;if(balance==null)return false;if("paid".equals(value))return balance.paidAmount>=balance.totalAmount;if("partial".equals(value))return balance.paidAmount>0&&balance.paidAmount<balance.totalAmount;return "unpaid".equals(value)&&balance.paidAmount<=0;}
    private int remaining(StudentMonthBalance balance){return balance==null?0:balance.remainingAmount();}

    private void renderOverview(){overviewHost.removeAllViews();overviewHost.addView(monthOverview());}
    private View monthOverview(){
        if(monthBalances==null)monthBalances=database.getCurrentMonthBalances();int paid=0,partial=0,unpaid=0;for(Student student:database.findStudents("")){StudentMonthBalance b=monthBalances.get(student.id);if(b==null)continue;if(b.paidAmount>=b.totalAmount)paid++;else if(b.paidAmount>0)partial++;else unpaid++;}
        LinearLayout card=column();card.setPadding(dp(14),dp(12),dp(14),dp(13));card.setBackground(rounded(SURFACE,dp(18),BORDER,dp(1)));LinearLayout title=row();title.setGravity(Gravity.CENTER_VERTICAL);title.addView(text("Situation du mois",16,TEXT,true),new LinearLayout.LayoutParams(0,WRAP,1f));String month=new SimpleDateFormat("MMMM yyyy",Locale.FRENCH).format(new Date());month=month.substring(0,1).toUpperCase(Locale.FRENCH)+month.substring(1);title.addView(text(month,12,MUTED,false));card.addView(title);LinearLayout metrics=row();metrics.addView(metric("Réglés",paid,GREEN,GREEN_SOFT),new LinearLayout.LayoutParams(0,dp(58),1));metrics.addView(metric("Partiels",partial,AMBER,AMBER_SOFT),margins(0,dp(58),dp(8),0,0,0,1));metrics.addView(metric("À payer",unpaid,Palette.ERROR,Palette.ERROR_SOFT),margins(0,dp(58),dp(8),0,0,0,1));card.addView(metrics,margins(MATCH,dp(58),0,dp(10),0,0));return card;
    }
    private View metric(String label,int value,int color,int background){LinearLayout box=column();box.setGravity(Gravity.CENTER);box.setBackground(rounded(background,dp(12),Color.TRANSPARENT,0));box.addView(text(String.valueOf(value),18,color,true));box.addView(text(label,11,color,true));return box;}

    private View createBottomNavigation() { return BottomTabBar.create(this,StudentListActivity.class); }
    private void addTab(LinearLayout bar,int res,String label,boolean selected,View.OnClickListener click){LinearLayout tab=column();tab.setGravity(Gravity.CENTER);if(click!=null)tab.setOnClickListener(click);FrameLayout box=new FrameLayout(this);box.addView(icon(res,selected?BROWN:MUTED),new FrameLayout.LayoutParams(dp(28),dp(28),Gravity.CENTER));if(selected)box.setBackground(rounded(TERRACOTTA_SOFT,dp(18),0,0));tab.addView(box,new LinearLayout.LayoutParams(dp(72),dp(34)));TextView caption=text(label,12,selected?BROWN:MUTED,selected);caption.setGravity(Gravity.CENTER);tab.addView(caption,new LinearLayout.LayoutParams(dp(72),dp(24)));bar.addView(tab,new LinearLayout.LayoutParams(0,MATCH,1));}
    private ImageView icon(int resource,int color){ImageView image=new ImageView(this);image.setImageResource(resource);image.setColorFilter(color);image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);return image;}
    private String initials(String name){String[] p=name.trim().split("\\s+");return (p[0].substring(0,1)+(p.length>1?p[p.length-1].substring(0,1):"")).toUpperCase();}
    private TextView centeredText(String value, int size, int color, boolean bold) { TextView view = text(value, size, color, bold); view.setGravity(Gravity.CENTER); return view; }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setIncludeFontPadding(false); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout row() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL); return view; }
    private GradientDrawable rounded(int color, int radius, int stroke, int width) { GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color); drawable.setCornerRadius(radius); if (width > 0) drawable.setStroke(width, stroke); return drawable; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height); params.setMargins(left, top, right, bottom); return params; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom, float weight) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight); params.setMargins(left, top, right, bottom); return params; }
    private LinearLayout.LayoutParams weightedMargins(float weight, int left, int top, int right, int bottom) { return margins(0, WRAP, left, top, right, bottom, weight); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT, WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
}
