package com.centrepay.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared four-tab navigation; keeps Android's navigation buttons clear. */
final class BottomTabBar {
    private static final int TEAL=Palette.PRIMARY, MUTED=Palette.MUTED;
    private BottomTabBar(){}

    static View create(Activity activity, Class<? extends Activity> selected) {
        LinearLayout holder=new LinearLayout(activity);
        holder.setOrientation(LinearLayout.VERTICAL);
        holder.setBackgroundColor(Color.WHITE);
        holder.setElevation(dp(activity,8));
        View line=new View(activity);line.setBackgroundColor(0xffe3eaf0);
        holder.addView(line,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(activity,1)));
        LinearLayout tabs=new LinearLayout(activity);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);
        tabs.setPadding(dp(activity,8),dp(activity,4),dp(activity,8),dp(activity,4));
        add(activity,tabs,R.drawable.ic_home_24,"Accueil",MainActivity.class,selected);
        add(activity,tabs,R.drawable.ic_groups_24,"Élèves",StudentListActivity.class,selected);
        add(activity,tabs,R.drawable.ic_credit_card_24,"Paiements",PaymentListActivity.class,selected);
        add(activity,tabs,R.drawable.ic_school_24,"Profs",TeacherListActivity.class,selected);
        holder.addView(tabs,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(activity,64)));
        if(Build.VERSION.SDK_INT>=30){
            holder.setOnApplyWindowInsetsListener((view,insets)->{
                int bottom=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                holder.setPadding(0,0,0,bottom);
                return insets;
            });
            holder.requestApplyInsets();
        }
        return holder;
    }

    private static void add(Activity activity,LinearLayout tabs,int drawable,String label,Class<? extends Activity> target,Class<? extends Activity> selected) {
        boolean active=target==selected;
        LinearLayout tab=new LinearLayout(activity);tab.setOrientation(LinearLayout.VERTICAL);tab.setGravity(Gravity.CENTER);
        tab.setMinimumHeight(dp(activity,56));tab.setContentDescription(label);
        ImageView icon=new ImageView(activity);icon.setImageResource(drawable);
        icon.setImageTintList(ColorStateList.valueOf(active?TEAL:MUTED));
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(dp(activity,18),dp(activity,5),dp(activity,18),dp(activity,5));
        if(active){GradientDrawable shape=new GradientDrawable();shape.setColor(0xffe6f5f3);shape.setCornerRadius(dp(activity,19));icon.setBackground(shape);}
        tab.addView(icon,new LinearLayout.LayoutParams(dp(activity,62),dp(activity,34)));
        TextView text=new TextView(activity);text.setText(label);text.setTextSize(12);text.setTextColor(active?TEAL:MUTED);
        text.setGravity(Gravity.CENTER);text.setIncludeFontPadding(false);
        if(active)text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        tab.addView(text,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(activity,20)));
        if(!active)tab.setOnClickListener(v->open(activity,target));
        tabs.addView(tab,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1));
    }

    private static void open(Activity activity,Class<? extends Activity> target){
        Intent intent=new Intent(activity,target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }
    private static int dp(Activity activity,int value){return Math.round(value*activity.getResources().getDisplayMetrics().density);}
}
