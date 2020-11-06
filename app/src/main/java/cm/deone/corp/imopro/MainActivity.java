package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private EditText userEmailTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialisationDesVues();
        checkUser();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkEmailFab){
            String email = userEmailTv.getText().toString().trim();
            if (TextUtils.isEmpty(email)){
                Toast.makeText(MainActivity.this, "Renseignez votre email!", Toast.LENGTH_SHORT).show();
                return;
            }
            checkEmailStatus(""+email);
        }
    }

    private void checkUser(){
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        if (fUser != null){
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void initialisationDesVues(){
        firebaseAuth = FirebaseAuth.getInstance();
        userEmailTv = findViewById(R.id.userEmailTv);
        FloatingActionButton checkEmailFab = findViewById(R.id.checkEmailFab);
        checkEmailFab.setOnClickListener(this);
    }

    private void checkEmailStatus(String email) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                if (isNewUser){
                    Intent intent = new Intent(MainActivity.this, NewUserActivity.class);
                    intent.putExtra(getResources().getString(R.string.email), email);
                    intent.putExtra(getResources().getString(R.string.userQuality), "new");
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(MainActivity.this, NewUserActivity.class);
                    intent.putExtra(getResources().getString(R.string.email), email);
                    intent.putExtra(getResources().getString(R.string.userQuality), "old");
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}