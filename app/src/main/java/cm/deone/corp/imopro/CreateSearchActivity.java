package cm.deone.corp.imopro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CreateSearchActivity extends AppCompatActivity {

    private String myUID;
    private String sDescription;

    private EditText queCherchezVousEdtv;
    private EditText ouCherchezVousEdtv;
    private EditText votreBudgetEdtv;
    private CheckBox echeanceCb;
    private long timeStamp;
    private int mYear, mMonth, mDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_search);
        checkUser();
        initVues();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUser();
    }

    private void checkUser(){
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser!=null){
            myUID = fUser.getUid();
        }else{
            startActivity(new Intent(CreateSearchActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initVues(){
        sDescription = getIntent().getStringExtra("sDescription");
        queCherchezVousEdtv = findViewById(R.id.queCherchezVousEdtv);
        ouCherchezVousEdtv = findViewById(R.id.ouCherchezVousEdtv);
        votreBudgetEdtv = findViewById(R.id.votreBudgetEdtv);
        echeanceCb = findViewById(R.id.echeanceCb);
        echeanceCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    showDateDialog();
                }
            }
        });
        findViewById(R.id.searchPostFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificationDesSaisies();
            }
        });
    }

    private void showDateDialog() {
        final Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateSearchActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        GregorianCalendar gc = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        timeStamp = gc.getTimeInMillis();

                        echeanceCb.setText(getResources().getString(R.string.echeance_search, ""+dayOfMonth, ""+monthOfYear, ""+year));

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void verificationDesSaisies(){
        String queCherchezVous = queCherchezVousEdtv.getText().toString().trim();
        String ouCherchezVous = ouCherchezVousEdtv.getText().toString().trim();
        String votreBudget = votreBudgetEdtv.getText().toString().trim();

        if (TextUtils.isEmpty(queCherchezVous)){
            Toast.makeText(CreateSearchActivity.this,
                    ""+getResources().getString(R.string.toast_que_cherchez_vous),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(ouCherchezVous)){
            Toast.makeText(CreateSearchActivity.this,
                    ""+getResources().getString(R.string.toast_ou_cherchez_vous),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(votreBudget)){
            Toast.makeText(CreateSearchActivity.this,
                    ""+getResources().getString(R.string.toast_votre_budget),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        initSearch(""+queCherchezVous, ""+ouCherchezVous, ""+votreBudget);
    }

    private void initSearch(String queCherchezVous, String ouCherchezVous, String votreBudget) {

    }

}