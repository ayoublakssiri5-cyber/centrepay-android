package com.centrepay.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TariffTeacherAssignmentActivity extends Activity {
    public static final String EXTRA_TARIFF_ID="tariff_id";
    private static final int TEAL=Palette.PRIMARY,DARK=Palette.PRIMARY_DARK,BG=Palette.BACKGROUND,TEXT=Palette.TEXT,MUTED=Palette.MUTED,BORDER=Palette.BORDER;
    private StudentDatabaseHelper database;
    private TariffOption tariff;
    private final Map<String,Teacher> choices=new LinkedHashMap<>();
    private LinearLayout cards;
    private TextView progressCount,completionTitle,completionDetail,completionBadge,saveButton;
    private View progressBar;
    private boolean addingTeacher;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(DARK);
        database=new StudentDatabaseHelper(this);
        tariff=database.getTariffOption(getIntent().getLongExtra(EXTRA_TARIFF_ID,-1));
        if(tariff==null){Toast.makeText(this,"Tarif introuvable.",Toast.LENGTH_SHORT).show();finish();return;}
        database.autoAssignUniqueTeachers(tariff.id);
        choices.putAll(database.getTariffTeacherAssignments(tariff.id));
        removeIncompatibleAssignments();
        LinearLayout root=column();root.setBackgroundColor(BG);
        root.addView(header(),new LinearLayout.LayoutParams(MATCH,dp(88)));
        ScrollView scroll=new ScrollView(this);
        LinearLayout content=column();content.setPadding(dp(12),dp(12),dp(12),dp(28));
        content.addView(summary());
        content.addView(progressCard(),margin(MATCH,WRAP,0,dp(12),0,0));
        cards=column();content.addView(cards,margin(MATCH,WRAP,0,dp(12),0,0));
        content.addView(infoCard(),margin(MATCH,WRAP,0,dp(2),0,0));
        content.addView(completionCard(),margin(MATCH,WRAP,0,dp(12),0,0));
        saveButton=text("▣  Enregistrer les affectations",16,Color.WHITE,true);
        saveButton.setGravity(Gravity.CENTER);saveButton.setBackground(gradient(TEAL,DARK,dp(12)));
        saveButton.setOnClickListener(v->confirmSave());
        content.addView(saveButton,margin(MATCH,dp(56),0,dp(12),0,0));
        scroll.addView(content,new ScrollView.LayoutParams(MATCH,WRAP));
        root.addView(scroll,new LinearLayout.LayoutParams(MATCH,0,1));
        setContentView(root);render();
    }

    private View header(){
        LinearLayout bar=row();bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(14),0,dp(16),0);
        bar.setBackground(gradient(TEAL,DARK,0));
        ImageView back=icon(R.drawable.ic_arrow_back_24,Color.WHITE);back.setOnClickListener(v->finish());
        bar.addView(back,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout titles=column();
        titles.addView(text("Affectation des professeurs",19,Color.WHITE,true));
        titles.addView(text("Associer les enseignants à un tarif",12,0xffd7efed,false),margin(MATCH,WRAP,0,dp(3),0,0));
        bar.addView(titles,new LinearLayout.LayoutParams(0,WRAP,1));
        bar.addView(text("CentrePay",17,Color.WHITE,true));
        return bar;
    }

    private View summary(){
        LinearLayout card=card();
        LinearLayout line=row();line.setGravity(Gravity.CENTER_VERTICAL);
        line.addView(bubble(R.drawable.ic_school_24,DARK,0xffdef4f1,58),new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout copy=column();
        copy.addView(text(levelLabel(),18,TEXT,true));
        copy.addView(text("Tarif mensuel",14,MUTED,false),margin(MATCH,WRAP,0,dp(5),0,0));
        copy.addView(text(tariff.studentAmount+" DH",27,DARK,true),margin(MATCH,WRAP,0,dp(3),0,0));
        line.addView(copy,margin(0,WRAP,dp(12),0,0,0,1));card.addView(line);
        LinearLayout chips=row();
        for(String key:subjects()){
            TextView chip=text(subjectLabel(key),12,subjectColor(key),true);
            chip.setGravity(Gravity.CENTER);chip.setPadding(dp(6),dp(7),dp(6),dp(7));
            chip.setBackground(round(subjectBackground(key),dp(10),Color.TRANSPARENT,0));
            chips.addView(chip,margin(0,WRAP,0,dp(12),dp(5),0,1));
        }
        card.addView(chips);return card;
    }

    private View progressCard(){
        LinearLayout card=card();LinearLayout line=row();line.setGravity(Gravity.CENTER_VERTICAL);
        line.addView(icon(R.drawable.ic_analytics_24,TEAL),new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout copy=column();
        copy.addView(text(subjects().size()+" matières à affecter",17,TEXT,true));
        copy.addView(text("Assignez un professeur à chaque matière de ce tarif.",13,MUTED,false),margin(MATCH,WRAP,0,dp(4),0,0));
        line.addView(copy,margin(0,WRAP,dp(9),0,0,0,1));
        progressCount=text("0/0",16,DARK,true);progressCount.setPadding(dp(10),dp(7),dp(10),dp(7));
        progressCount.setBackground(round(Palette.SUCCESS_SOFT,dp(22),Color.TRANSPARENT,0));line.addView(progressCount);card.addView(line);
        LinearLayout track=row();track.setBackground(round(0xffe5e7eb,dp(5),Color.TRANSPARENT,0));
        progressBar=new View(this);progressBar.setBackground(round(TEAL,dp(5),Color.TRANSPARENT,0));
        track.addView(progressBar,new LinearLayout.LayoutParams(0,dp(7)));
        card.addView(track,margin(MATCH,dp(7),0,dp(14),0,0));return card;
    }

    private void render(){
        cards.removeAllViews();
        for(String key:subjects())cards.addView(subjectCard(key),margin(MATCH,WRAP,0,0,0,dp(12)));
        int count=assignedCount(),total=subjects().size();
        progressCount.setText(count+"/"+total);
        View track=(View)progressBar.getParent();
        track.post(()->{progressBar.getLayoutParams().width=total==0?0:track.getWidth()*count/total;progressBar.requestLayout();});
        boolean done=total>0&&count==total;
        completionTitle.setText(done?"Affectation complète":"Affectation à compléter");
        completionDetail.setText(count+"/"+total+" matières attribuées");
        completionBadge.setText(done?"Tout est prêt !":"En attente");
        completionBadge.setTextColor(done?DARK:Palette.WARNING);
        completionBadge.setBackground(round(done?Palette.SUCCESS_SOFT:0xfffff3d6,dp(20),Color.TRANSPARENT,0));
        saveButton.setEnabled(done);saveButton.setAlpha(done?1f:.45f);
    }

    private View subjectCard(String key){
        LinearLayout card=card();LinearLayout line=row();line.setGravity(Gravity.CENTER_VERTICAL);
        line.addView(bubble(subjectIcon(key),subjectColor(key),subjectBackground(key),50),new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout copy=column();copy.addView(text(subjectLabel(key),16,TEXT,true));
        copy.addView(text("Part du tarif",12,MUTED,false),margin(MATCH,WRAP,0,dp(4),0,0));
        copy.addView(text(subjectAmount(key)+" DH",18,DARK,true),margin(MATCH,WRAP,0,dp(2),0,0));
        line.addView(copy,margin(0,WRAP,dp(11),0,0,0,1));card.addView(line);
        card.addView(text("Professeur assigné",13,MUTED,false),margin(MATCH,WRAP,0,dp(14),0,dp(6)));
        Teacher teacher=choices.get(key);
        TextView picker=text((teacher==null?"Sélectionner un professeur":teacher.fullName)+"  ▾",15,TEXT,false);
        picker.setGravity(Gravity.CENTER_VERTICAL);picker.setPadding(dp(13),0,dp(10),0);
        picker.setBackground(round(Color.WHITE,dp(10),0xffd6e0ea,dp(1)));
        picker.setOnClickListener(v->choose(key));
        card.addView(picker,new LinearLayout.LayoutParams(MATCH,dp(48)));
        return card;
    }

    private View infoCard(){
        LinearLayout card=card();LinearLayout line=row();line.setGravity(Gravity.TOP);
        line.addView(icon(R.drawable.ic_description_24,Palette.BLUE),new LinearLayout.LayoutParams(dp(28),dp(28)));
        LinearLayout copy=column();copy.addView(text("Informations importantes",15,TEXT,true));
        copy.addView(text("Seuls les professeurs pouvant enseigner la matière sélectionnée apparaissent dans la liste. Ils sont filtrés selon leurs compétences et leur niveau.",13,MUTED,false),margin(MATCH,WRAP,0,dp(7),0,0));
        line.addView(copy,margin(0,WRAP,dp(10),0,0,0,1));card.addView(line);return card;
    }

    private View completionCard(){
        LinearLayout card=card();LinearLayout line=row();line.setGravity(Gravity.CENTER_VERTICAL);
        line.addView(icon(R.drawable.ic_check_circle_24,0xff16a34a),new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout copy=column();completionTitle=text("",15,TEXT,true);completionDetail=text("",13,MUTED,false);
        copy.addView(completionTitle);copy.addView(completionDetail,margin(MATCH,WRAP,0,dp(5),0,0));
        line.addView(copy,margin(0,WRAP,dp(10),0,0,0,1));
        completionBadge=text("",12,DARK,true);completionBadge.setPadding(dp(10),dp(8),dp(10),dp(8));
        completionBadge.setBackground(round(Palette.SUCCESS_SOFT,dp(20),Color.TRANSPARENT,0));line.addView(completionBadge);
        card.addView(line);return card;
    }

    private void choose(String key){
        List<Teacher> compatible=database.getCompatibleTeachers(tariff.level,key);
        if(compatible.isEmpty()){
            new AlertDialog.Builder(this).setTitle("Aucun professeur compatible").setMessage("Ajoutez un professeur qui enseigne "+subjectLabel(key)+" au niveau "+tariff.level+".").setNegativeButton("Fermer",null).setPositiveButton("Ajouter un professeur",(d,w)->{addingTeacher=true;startActivity(new Intent(this,AddTeacherActivity.class));}).show();
            return;
        }
        List<String> names=new ArrayList<>();names.add("Non attribué");
        for(Teacher teacher:compatible)names.add(teacher.fullName+"\n"+teacher.level+" · "+teacher.subjects);
        new AlertDialog.Builder(this).setTitle("Professeur de "+subjectLabel(key)).setItems(names.toArray(new String[0]),(d,which)->{
            choices.put(key,which==0?null:compatible.get(which-1));render();
        }).show();
    }
    private void confirmSave(){
        int count=assignedCount(),total=subjects().size();
        if(count!=total){Toast.makeText(this,"Affectez un professeur à chaque matière.",Toast.LENGTH_LONG).show();return;}
        StringBuilder recap=new StringBuilder();for(String key:subjects())recap.append("\n").append(subjectLabel(key)).append(" : ").append(choices.get(key).fullName);
        new AlertDialog.Builder(this).setTitle("Confirmer les affectations").setMessage("Tarif : "+levelLabel()+" — "+tariff.studentAmount+" DH"+recap).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->save()).show();
    }
    private void save(){
        for(String key:subjects()){
            Teacher teacher=choices.get(key);
            if(teacher==null)database.removeTariffTeacherAssignment(tariff.id,key);
            else database.saveTariffTeacherAssignment(tariff.id,key,teacher.id);
        }
        Toast.makeText(this,"Affectations enregistrées.",Toast.LENGTH_SHORT).show();finish();
    }
    private void removeIncompatibleAssignments(){for(String key:subjects()){Teacher selected=choices.get(key);if(selected==null)continue;boolean compatible=false;for(Teacher candidate:database.getCompatibleTeachers(tariff.level,key))if(candidate.id==selected.id){compatible=true;break;}if(!compatible){choices.remove(key);database.removeTariffTeacherAssignment(tariff.id,key);}}}
    @Override protected void onResume(){super.onResume();if(addingTeacher&&tariff!=null){addingTeacher=false;database.autoAssignUniqueTeachers(tariff.id);choices.putAll(database.getTariffTeacherAssignments(tariff.id));if(cards!=null)render();}}
    private int assignedCount(){int n=0;for(String key:subjects())if(choices.get(key)!=null)n++;return n;}
    private List<String> subjects(){
        List<String> keys=new ArrayList<>();
        if(tariff.mathAmount>0)keys.add("math");
        if(tariff.pcAmount>0)keys.add("pc");
        if(tariff.frenchAmount>0)keys.add("french");
        if(tariff.svtAmount>0)keys.add("svt");
        return keys;
    }
    private String levelLabel(){switch(tariff.level){case "1AC":return "1ère année – Collège";case "2AC":return "2ème année – Collège";case "3AC":return "3ème année – Collège";case "TC":return "Tronc commun";default:return tariff.level;}}
    private String subjectLabel(String k){switch(k){case "math":return "Maths";case "pc":return "Physique-Chimie";case "french":return "Français";case "svt":return "SVT";default:return k;}}
    private int subjectAmount(String k){switch(k){case "math":return tariff.mathAmount;case "pc":return tariff.pcAmount;case "french":return tariff.frenchAmount;case "svt":return tariff.svtAmount;default:return 0;}}
    private int subjectColor(String k){switch(k){case "math":return TEAL;case "pc":return 0xff7e22ce;case "french":return Palette.BLUE;case "svt":return 0xffc46b08;default:return DARK;}}
    private int subjectBackground(String k){switch(k){case "math":return 0xffe8f7f1;case "pc":return 0xfff3edff;case "french":return 0xffeaf3ff;case "svt":return 0xfffff3e5;default:return 0xffe6f7f4;}}
    private int subjectIcon(String k){switch(k){case "math":return R.drawable.ic_equalizer_24;case "pc":return R.drawable.ic_analytics_24;case "french":return R.drawable.ic_menu_book_24;case "svt":return R.drawable.ic_school_24;default:return R.drawable.ic_description_24;}}
    private LinearLayout card(){LinearLayout l=column();l.setPadding(dp(16),dp(16),dp(16),dp(16));l.setBackground(round(Color.WHITE,dp(16),BORDER,dp(1)));l.setElevation(dp(2));return l;}
    private LinearLayout bubble(int drawable,int color,int bg,int size){LinearLayout l=row();l.setGravity(Gravity.CENTER);l.setBackground(round(bg,dp(15),Color.TRANSPARENT,0));l.addView(icon(drawable,color),new LinearLayout.LayoutParams(dp(size/2),dp(size/2)));return l;}
    private ImageView icon(int resource,int color){ImageView v=new ImageView(this);v.setImageResource(resource);v.setImageTintList(ColorStateList.valueOf(color));v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);return v;}
    private TextView text(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setIncludeFontPadding(false);if(b)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private GradientDrawable round(int c,int r,int stroke,int w){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(r);if(w>0)d.setStroke(w,stroke);return d;}
    private GradientDrawable gradient(int a,int b,int r){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{a,b});d.setCornerRadius(r);return d;}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(l,t,r,b);return p;}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h,weight);p.setMargins(l,t,r,b);return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private static final int MATCH=ViewGroup.LayoutParams.MATCH_PARENT,WRAP=ViewGroup.LayoutParams.WRAP_CONTENT;
}
