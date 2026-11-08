package com.centrepay.mobile;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddPaymentActivity extends Activity {
    private static final int NAVY = Palette.PRIMARY_DARK;
    private static final int TEAL = Palette.PRIMARY;
    private static final int TEXT = Palette.TEXT;
    private static final int MUTED = Palette.MUTED;
    private static final int SURFACE = Palette.SURFACE;
    private static final int BORDER = Palette.BORDER;

    private StudentDatabaseHelper database;
    private List<Student> students;
    private Spinner groupSpinner, studentSpinner;
    private TextView studentInfo;
    private EditText amount, paymentDate, referenceMonth, note;
    private LinearLayout distributionArea;
    private long preselectedStudentId;
    private TextView saveButton;
    private boolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        database = new StudentDatabaseHelper(this);
        database.refreshMonthlyDuesAndAdvances();
        preselectedStudentId = getIntent().getLongExtra("student_id", -1);
        students = database.findStudents("");

        LinearLayout root = column();
        root.setBackgroundColor(Palette.BACKGROUND);
        root.addView(hero(), new LinearLayout.LayoutParams(MATCH, dp(88)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = column();
        content.setPadding(dp(12), dp(12), dp(12), dp(28));

        if (students.isEmpty()) {
            TextView empty = text("Ajoutez d'abord un élève et choisissez son tarif pour pouvoir saisir un paiement.", 14, MUTED, false);
            empty.setPadding(dp(16), dp(22), dp(16), dp(22));
            empty.setBackground(rounded(SURFACE, dp(14), BORDER, dp(1)));
            content.addView(empty);
            TextView add=text("+  Ajouter un élève",15,Color.WHITE,true);add.setGravity(Gravity.CENTER);add.setBackground(gradient(NAVY,TEAL,dp(12)));add.setOnClickListener(v->startActivity(new Intent(this,AddStudentActivity.class)));content.addView(add,margins(MATCH,dp(52),0,dp(12),0,0));
        } else {
            buildForm(content);
        }
        scroll.addView(content, new ScrollView.LayoutParams(MATCH, WRAP));
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        setContentView(root);
    }

    @Override protected void onResume(){super.onResume();if(database!=null&&students!=null&&students.isEmpty()&&!database.findStudents("").isEmpty())recreate();}

    private void buildForm(LinearLayout content) {
        LinearLayout studentCard = section(R.drawable.ic_groups_24, "Élève", "Sélectionnez un groupe puis un élève");
        studentCard.addView(label("Groupe / niveau *"), margins(MATCH, WRAP, 0, dp(14), 0, dp(7)));
        groupSpinner = new Spinner(this);
        String[] groups = {"Choisir un groupe", "1AC", "2AC", "3AC"};
        groupSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groups));
        groupSpinner.setBackground(rounded(SURFACE, dp(12), BORDER, dp(1)));
        studentCard.addView(groupSpinner, new LinearLayout.LayoutParams(MATCH, dp(52)));

        studentCard.addView(label("Élève *"), margins(MATCH, WRAP, 0, dp(12), 0, dp(7)));
        studentSpinner = new Spinner(this);
        studentSpinner.setBackground(rounded(SURFACE, dp(12), BORDER, dp(1)));
        studentSpinner.setEnabled(false);
        studentCard.addView(studentSpinner, new LinearLayout.LayoutParams(MATCH, dp(52)));

        studentInfo = text("", 12, TEXT, false);
        studentInfo.setPadding(dp(14), dp(14), dp(14), dp(14)); studentInfo.setTextSize(13);
        studentInfo.setBackground(rounded(Palette.PRIMARY_SOFT, dp(12), Color.TRANSPARENT, 0));
        studentCard.addView(studentInfo, margins(MATCH, WRAP, 0, dp(12), 0, 0));
        content.addView(studentCard);

        LinearLayout monthCard = section(R.drawable.ic_calendar_month_24, "Mois du paiement", "Choisissez la mensualité concernée");
        referenceMonth = input("MM/AAAA — modifiable", InputType.TYPE_CLASS_TEXT);String requestedMonth=getIntent().getStringExtra("reference_month");referenceMonth.setText(requestedMonth!=null&&requestedMonth.matches("\\d{4}-\\d{2}")?requestedMonth.substring(5)+"/"+requestedMonth.substring(0,4):new SimpleDateFormat("MM/yyyy", Locale.US).format(new Date())); makeMonthPicker(referenceMonth);
        monthCard.addView(referenceMonth, margins(MATCH, dp(52), 0, dp(14), 0, 0)); content.addView(monthCard, margins(MATCH, WRAP, 0, dp(12), 0, 0));

        LinearLayout details = section(R.drawable.ic_credit_card_24, "Détails du paiement", "Montant, date de réception et note");
        amount = input("Ex. 200", InputType.TYPE_CLASS_NUMBER);
        details.addView(label("Montant reçu (DH) *"), margins(MATCH, WRAP, 0, dp(14), 0, dp(7))); details.addView(amount, new LinearLayout.LayoutParams(MATCH, dp(52)));
        paymentDate = input("jj/mm/aaaa", InputType.TYPE_CLASS_TEXT);
        paymentDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
        makeDatePicker(paymentDate);
        details.addView(label("Date de réception *"), margins(MATCH, WRAP, 0, dp(12), 0, dp(7))); details.addView(paymentDate, new LinearLayout.LayoutParams(MATCH, dp(52)));
        note = input("Ex. paiement septembre", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        details.addView(label("Note (facultative)"), margins(MATCH, WRAP, 0, dp(12), 0, dp(7))); details.addView(note, new LinearLayout.LayoutParams(MATCH, dp(58))); content.addView(details, margins(MATCH, WRAP, 0, dp(12), 0, 0));

        distributionArea = section(R.drawable.ic_analytics_24, "Répartition du montant", "Aperçu selon le tarif de l’élève"); content.addView(distributionArea, margins(MATCH, WRAP, 0, dp(12), 0, 0));

        content.addView(infoCard(),margins(MATCH,WRAP,0,0,0,dp(12)));
        saveButton = text("▣     Enregistrer le paiement", 16, SURFACE, true);
        saveButton.setGravity(Gravity.CENTER);
        saveButton.setBackground(gradient(NAVY, TEAL, dp(12)));
        saveButton.setOnClickListener(view -> validateAndConfirm());
        content.addView(saveButton, margins(MATCH, dp(56), 0, dp(4), 0, 0));

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStudentsForGroup(position == 0 ? "" : parent.getItemAtPosition(position).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { renderStudentInfo(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        Student requestedStudent = preselectedStudentId > 0 ? database.getStudent(preselectedStudentId) : null;
        if (requestedStudent != null) {
            groupSpinner.setSelection(groupPosition(requestedStudent.level));
        } else {
            loadStudentsForGroup("");
        }
        amount.addTextChangedListener(new android.text.TextWatcher() { public void beforeTextChanged(CharSequence s,int a,int b,int c){} public void onTextChanged(CharSequence s,int a,int b,int c){} public void afterTextChanged(android.text.Editable e){ renderDistribution(); } });
    }

    private int groupPosition(String group) {
        if ("1AC".equals(group)) return 1;
        if ("2AC".equals(group)) return 2;
        if ("3AC".equals(group)) return 3;
        return 0;
    }

    private void loadStudentsForGroup(String group) {
        List<StudentChoice> choices = new ArrayList<>();
        if (!group.isEmpty()) {
            for (Student student : database.findStudents("", group)) choices.add(new StudentChoice(student));
        }
        studentSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices));
        studentSpinner.setEnabled(!choices.isEmpty());
        if (!choices.isEmpty() && preselectedStudentId > 0) {
            for (int index = 0; index < choices.size(); index++) {
                if (choices.get(index).student.id == preselectedStudentId) {
                    studentSpinner.setSelection(index);
                    break;
                }
            }
            preselectedStudentId = -1;
        }
        if (group.isEmpty()) {
            studentInfo.setText("Choisissez d'abord le groupe, puis l'élève.");
        } else if (choices.isEmpty()) {
            studentInfo.setText("Aucun élève enregistré dans le groupe " + group + ".");
        } else {
            renderStudentInfo();
        }
    }

    private void renderStudentInfo() {
        Student student = selectedStudent();
        if (student == null) return;
        studentInfo.setText(student.fullName() + "\n" + student.level + "  ·  " + (student.phone == null || student.phone.trim().isEmpty() ? "Téléphone non renseigné" : student.phone));
        renderDistribution();
    }

    private void renderDistribution() {
        if (distributionArea == null) return;
        while (distributionArea.getChildCount() > 1) distributionArea.removeViewAt(1);
        Student student = selectedStudent();
        if (student == null) { distributionArea.addView(text("Sélectionnez un élève pour voir la répartition.", 13, MUTED, false), margins(MATCH, WRAP, 0, dp(14), 0, 0)); return; }
        int received = 0; try { received = Integer.parseInt(amount.getText().toString().trim()); } catch (Exception ignored) { }
        // Both figures come from the same computation as the save itself (run then rolled back), advances included.
        PaymentPreview current=preview(student,0);
        if(current.status==PaymentPreview.BEFORE_ENROLLMENT){TextView warning=text("Ce mois est antérieur à l’inscription de l’élève ("+student.enrollmentDate+"). Choisissez un mois à partir de l’inscription.",13,Palette.WARNING,true);warning.setPadding(dp(12),dp(10),dp(12),dp(10));warning.setBackground(rounded(0xfffff3d6,dp(11),Color.TRANSPARENT,0));distributionArea.addView(warning,margins(MATCH,WRAP,0,dp(14),0,0));return;}
        int monthDue=current.monthDue>0?current.monthDue:student.negotiatedAmount,alreadyPaid=current.monthPaidBefore+current.advanceApplied,remaining=current.status==PaymentPreview.OK?current.remainingBeforePayment():student.negotiatedAmount;
        PaymentPreview planned=preview(student,received>0?received:remaining);int applied=planned.allocated,advance=received>0?planned.advanceCreated:0;
        LinearLayout situation=row();situation.setGravity(Gravity.CENTER_VERTICAL);situation.addView(situationValue("Tarif",monthDue+" DH",NAVY,Palette.PRIMARY_SOFT),new LinearLayout.LayoutParams(0,dp(66),1));situation.addView(situationValue("Déjà payé",alreadyPaid+" DH",0xff16a34a,Palette.SUCCESS_SOFT),margins(0,dp(66),dp(7),0,0,0,1));situation.addView(situationValue("Reste",remaining+" DH",0xffd97706,Palette.WARNING_SOFT),margins(0,dp(66),dp(7),0,0,0,1));distributionArea.addView(situation,margins(MATCH,WRAP,0,dp(14),0,0));
        LinearLayout shortcuts=row();TextView due=text("Utiliser le reste dû",12,NAVY,true);due.setGravity(Gravity.CENTER);due.setBackground(rounded(Palette.PRIMARY_SOFT,dp(11),Color.TRANSPARENT,0));due.setOnClickListener(v->amount.setText(String.valueOf(remaining)));shortcuts.addView(due,new LinearLayout.LayoutParams(0,dp(40),1));TextView full=text("Tarif complet",12,TEXT,true);full.setGravity(Gravity.CENTER);full.setBackground(rounded(0xfff1f5f9,dp(11),Color.TRANSPARENT,0));full.setOnClickListener(v->amount.setText(String.valueOf(monthDue)));shortcuts.addView(full,margins(0,dp(40),dp(7),0,0,0,1));distributionArea.addView(shortcuts,margins(MATCH,WRAP,0,dp(9),0,0));
        int[] split = planned.split;
        LinearLayout chips = row();
        addAllocation(chips, "Maths", split[0], Palette.PRIMARY_SOFT, NAVY);
        addAllocation(chips, "Français", split[1], Palette.BLUE_SOFT, Palette.BLUE);
        addAllocation(chips, "PC", split[2], 0xffede9fe, 0xff7e22ce);
        addAllocation(chips, "SVT", split[3], 0xfffff1e6, 0xffc45d08);
        addAllocation(chips, "Centre", split[4], 0xfffff3d6, Palette.WARNING);
        distributionArea.addView(chips, margins(MATCH, WRAP, 0, dp(14), 0, 0));
        String advanceUsed=current.advanceApplied>0?"Avance existante utilisée : "+current.advanceApplied+" DH\n":"";
        TextView total=text(received<=0?advanceUsed+"Saisissez le montant reçu pour calculer la répartition.":advanceUsed+"Affecté au mois : "+applied+" DH"+(advance>0?"   ·   Avance : "+advance+" DH":""),12,advance>0?Palette.WARNING:NAVY,true);total.setPadding(dp(12),dp(10),dp(12),dp(10));total.setBackground(rounded(advance>0?0xfffff3d6:Palette.PRIMARY_SOFT,dp(11),Color.TRANSPARENT,0));distributionArea.addView(total,margins(MATCH,WRAP,0,dp(9),0,0));
    }

    private View situationValue(String label,String value,int color,int bg){LinearLayout box=column();box.setGravity(Gravity.CENTER);box.setBackground(rounded(bg,dp(11),Color.TRANSPARENT,0));box.addView(text(label,11,color,false));box.addView(text(value,14,color,true),margins(MATCH,WRAP,0,dp(4),0,0));return box;}
    private PaymentPreview preview(Student student,int value){return database.previewPayment(student.id,value,paymentDate==null?"":paymentDate.getText().toString(),referenceMonth==null?"":referenceMonth.getText().toString());}
    private void addAllocation(LinearLayout parent, String label, int value, int bg, int color) { if (value <= 0) return; LinearLayout chip = column(); chip.setGravity(Gravity.CENTER); chip.setPadding(dp(5), dp(10), dp(5), dp(10)); chip.setBackground(rounded(bg, dp(12), Color.TRANSPARENT, 0)); chip.addView(text(label, 11, color, false)); chip.addView(text(value + " DH", 12, color, true), margins(WRAP, WRAP, 0, dp(4), 0, 0)); parent.addView(chip, margins(0, WRAP, 0, 0, dp(6), 0, 1)); }

    private void validateAndConfirm() {
        if(saving)return;amount.setError(null);paymentDate.setError(null);referenceMonth.setError(null);
        Student student = selectedStudent();
        if (student == null) {
            Toast.makeText(this, "Choisissez d'abord un groupe puis un élève.", Toast.LENGTH_SHORT).show();
            return;
        }
        int received;
        try {
            received = Integer.parseInt(amount.getText().toString().trim());
            if (received <= 0) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            amount.setError("Montant obligatoire et supérieur à 0");amount.requestFocus();
            return;
        }
        String date = paymentDate.getText().toString().trim();
        if (!validDate(date)) {
            paymentDate.setError("Date invalide");
            return;
        }
        String month = referenceMonth.getText().toString().trim();
        if (!validMonth(month)) {
            referenceMonth.setError("Mois invalide");
            return;
        }
        PaymentPreview planned=database.previewPayment(student.id,received,date,month);
        if(planned.status==PaymentPreview.BEFORE_ENROLLMENT){referenceMonth.setError("Mois antérieur à l’inscription ("+student.enrollmentDate+")");Toast.makeText(this,"Ce mois est antérieur à l’inscription de l’élève.",Toast.LENGTH_LONG).show();return;}
        if(planned.status!=PaymentPreview.OK){Toast.makeText(this,"Impossible de calculer ce paiement. Vérifiez le tarif et la date d’inscription de l’élève.",Toast.LENGTH_LONG).show();return;}
        String message="Élève : "+student.fullName()+"\nMois : "+month+"\nMontant reçu : "+received+" DH"+(planned.advanceApplied>0?"\nAvance existante utilisée d’abord : "+planned.advanceApplied+" DH":"")+"\nAffecté au mois : "+planned.allocated+" DH"+(planned.advanceCreated>0?"\nAvance créée : "+planned.advanceCreated+" DH":"");final int finalReceived=received;new AlertDialog.Builder(this).setTitle("Confirmer le paiement").setMessage(message).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->savePayment(student,finalReceived,date,month)).show();
    }

    private void savePayment(Student student,int received,String date,String month) {
        saving=true;saveButton.setEnabled(false);saveButton.setAlpha(.65f);saveButton.setText("Enregistrement…");
        PaymentResult result = database.recordPayment(student.id, received, date, month, note.getText().toString());
        if (result == null) {
            saving=false;saveButton.setEnabled(true);saveButton.setAlpha(1f);saveButton.setText("▣     Enregistrer le paiement");
            Toast.makeText(this, "Impossible d'enregistrer le paiement. Vérifiez le tarif et la date.", Toast.LENGTH_LONG).show();
            return;
        }
        String message = "Paiement enregistré : " + received + " DH. Réparti : " + result.allocatedFromPayment + " DH.";
        if (result.advanceAppliedBeforePayment > 0) message += " Une avance de " + result.advanceAppliedBeforePayment + " DH a été utilisée pour ce mois.";
        if (result.advanceCreated > 0) message += " Avance créée : " + result.advanceCreated + " DH.";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private Student selectedStudent() {
        if (studentSpinner == null || studentSpinner.getSelectedItem() == null) return null;
        return ((StudentChoice) studentSpinner.getSelectedItem()).student;
    }

    private boolean validDate(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            format.setLenient(false);
            format.parse(value);
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    private boolean validMonth(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/yyyy", Locale.US);
            format.setLenient(false);
            format.parse(value);
            return value.matches("(0[1-9]|1[0-2])/\\d{4}");
        } catch (Exception error) {
            return false;
        }
    }

    private void makeDatePicker(EditText field) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(view -> {
            Calendar selected = dateFromText(field.getText().toString());
            DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) ->
                    field.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year)),
                    selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void makeMonthPicker(EditText field) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(view -> showMonthPicker());
    }

    private void showMonthPicker() {
        Calendar selected = monthFromText(referenceMonth.getText().toString());
        final int[] year = {selected.get(Calendar.YEAR)};
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        LinearLayout content = column();
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        TextView title = text("Choisir le mois payé", 19, TEXT, true);
        content.addView(title);
        LinearLayout navigation = row();
        navigation.setGravity(Gravity.CENTER_VERTICAL);
        TextView previous = monthNavigation("‹");
        TextView yearLabel = text(String.valueOf(year[0]), 17, TEXT, true);
        yearLabel.setGravity(Gravity.CENTER);
        TextView next = monthNavigation("›");
        navigation.addView(previous, new LinearLayout.LayoutParams(dp(48), dp(48)));
        navigation.addView(yearLabel, new LinearLayout.LayoutParams(0, dp(48), 1f));
        navigation.addView(next, new LinearLayout.LayoutParams(dp(48), dp(48)));
        content.addView(navigation, margins(MATCH, WRAP, 0, dp(10), 0, dp(8)));

        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            LinearLayout monthRow = row();
            for (int column = 0; column < 4; column++) {
                int monthIndex = rowIndex * 4 + column;
                TextView monthButton = text(months[monthIndex], 14, TEXT, true);
                monthButton.setGravity(Gravity.CENTER);
                monthButton.setPadding(dp(4), dp(12), dp(4), dp(12));
                boolean chosen = selected.get(Calendar.MONTH) == monthIndex && selected.get(Calendar.YEAR) == year[0];
                monthButton.setBackground(rounded(chosen ? Color.rgb(221, 245, 242) : Color.rgb(245, 247, 250), dp(10), Color.TRANSPARENT, 0));
                final int selectedMonth = monthIndex;
                monthButton.setOnClickListener(view -> {
                    referenceMonth.setText(String.format(Locale.US, "%02d/%04d", selectedMonth + 1, year[0]));
                    renderDistribution();
                    dialog.dismiss();
                });
                monthRow.addView(monthButton, margins(0, WRAP, dp(3), dp(3), dp(3), dp(3), 1f));
            }
            content.addView(monthRow, new LinearLayout.LayoutParams(MATCH, WRAP));
        }
        previous.setOnClickListener(view -> { year[0]--; yearLabel.setText(String.valueOf(year[0])); });
        next.setOnClickListener(view -> { year[0]++; yearLabel.setText(String.valueOf(year[0])); });
        dialog.setView(content);
        dialog.show();
    }

    private Calendar dateFromText(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            format.setLenient(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(value));
            return calendar;
        } catch (Exception error) {
            return Calendar.getInstance();
        }
    }

    private Calendar monthFromText(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/yyyy", Locale.US);
            format.setLenient(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(value));
            return calendar;
        } catch (Exception error) {
            return Calendar.getInstance();
        }
    }

    private View infoCard() {
        LinearLayout card = column();
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(rounded(Color.rgb(228, 245, 240), dp(16), Color.TRANSPARENT, 0));
        card.addView(text("Règle de répartition", 13, NAVY, true));
        card.addView(text("Le mois payé est proposé avec le mois actuel, mais vous pouvez le modifier. Exemple : paiement reçu le 15/09/2026 pour le mois 10/2026. Le montant est affecté uniquement au mois payé, jamais à un autre mois. Un surplus devient une avance.", 12, TEXT, false), margins(MATCH, WRAP, 0, dp(5), 0, 0));
        return card;
    }

    private void addField(LinearLayout content, String title, EditText field) { content.addView(label(title), margins(MATCH, WRAP, 0, dp(18), 0, dp(6))); content.addView(field, new LinearLayout.LayoutParams(MATCH, title.startsWith("Note") ? dp(82) : dp(50))); }
    private EditText input(String hint, int inputType) { EditText field = new EditText(this); field.setHint(hint); field.setTextSize(14); field.setInputType(inputType); field.setPadding(dp(14), 0, dp(14), 0); field.setBackground(rounded(SURFACE, dp(12), BORDER, dp(1))); return field; }
    private TextView label(String value) { return text(value, 13, TEXT, true); }
    private TextView monthNavigation(String value) { TextView view = text(value, 28, TEAL, false); view.setGravity(Gravity.CENTER); view.setBackground(rounded(Color.rgb(221, 245, 242), dp(20), Color.TRANSPARENT, 0)); return view; }
    private LinearLayout section(int iconResource, String title, String subtitle) { LinearLayout card = column(); card.setPadding(dp(12), dp(14), dp(12), dp(14)); card.setBackground(rounded(SURFACE, dp(16), BORDER, dp(1))); card.setElevation(dp(1)); LinearLayout head = row(); head.setGravity(Gravity.CENTER_VERTICAL); LinearLayout bubble = row(); bubble.setGravity(Gravity.CENTER); bubble.setBackground(rounded(0xffddf7f4, dp(22), Color.TRANSPARENT, 0)); bubble.addView(icon(iconResource, NAVY), new LinearLayout.LayoutParams(dp(23), dp(23))); head.addView(bubble, new LinearLayout.LayoutParams(dp(44), dp(44))); LinearLayout copy = column(); copy.addView(text(title, 16, TEXT, true)); copy.addView(text(subtitle, 11, MUTED, false), margins(MATCH, WRAP, 0, dp(3), 0, 0)); head.addView(copy, margins(0, WRAP, dp(11), 0, 0, 0, 1)); card.addView(head); return card; }
    private View hero() { FrameLayout hero = new FrameLayout(this); hero.setBackground(gradient(NAVY, TEAL, 0)); LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(16), 0, dp(16), 0); ImageView back = icon(R.drawable.ic_arrow_back_24, Color.WHITE); back.setPadding(dp(4), dp(4), dp(4), dp(4)); back.setOnClickListener(v -> finish()); bar.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40))); LinearLayout titles = column(); titles.addView(text("Nouveau paiement", 20, Color.WHITE, true)); titles.addView(text("Enregistrer un paiement élève", 12, 0xffd7efed, false), margins(MATCH, WRAP, 0, dp(3), 0, 0)); bar.addView(titles, margins(0, WRAP, dp(12), 0, 0, 0, 1)); TextView brand = text("CentrePay", 18, Color.WHITE, true); bar.addView(brand); hero.addView(bar, new FrameLayout.LayoutParams(MATCH, MATCH)); return hero; }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setIncludeFontPadding(false); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout row() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL); return view; }
    private ImageView icon(int resource, int color) { ImageView view = new ImageView(this); view.setImageResource(resource); view.setImageTintList(ColorStateList.valueOf(color)); view.setScaleType(ImageView.ScaleType.CENTER_INSIDE); view.setContentDescription(""); return view; }
    private GradientDrawable rounded(int color, int radius, int stroke, int width) { GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color); drawable.setCornerRadius(radius); if (width > 0) drawable.setStroke(width, stroke); return drawable; }
    private GradientDrawable gradient(int start, int end, int radius) { GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end}); drawable.setCornerRadius(radius); return drawable; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height); params.setMargins(left, top, right, bottom); return params; }
    private LinearLayout.LayoutParams margins(int width, int height, int left, int top, int right, int bottom, float weight) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight); params.setMargins(left, top, right, bottom); return params; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private static class StudentChoice {
        final Student student;
        StudentChoice(Student student) { this.student = student; }
        @Override public String toString() { return student.fullName() + " — " + student.level + " — " + student.negotiatedAmount + " DH / mois"; }
    }
}
