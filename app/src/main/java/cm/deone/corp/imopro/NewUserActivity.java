package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NewUserActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;

    private String email;
    private String userQuality;

    private ProgressDialog progressDialog;

    private EditText userPasswordTv;
    private EditText userPasswordConfirmationTv;
    private FloatingActionButton checkPasswordFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        initialisationDesVues();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkPasswordFab){
            verificationDesSaisies();
        }
    }

    private void initialisationDesVues(){
        firebaseAuth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra(getResources().getString(R.string.email));
        userQuality = getIntent().getStringExtra(getResources().getString(R.string.userQuality));
        userPasswordTv = findViewById(R.id.userPasswordTv);
        userPasswordConfirmationTv = findViewById(R.id.userPasswordConfirmationTv);
        userPasswordConfirmationTv.setVisibility(userQuality.equals("new") ? View.VISIBLE:View.GONE);
        checkPasswordFab = findViewById(R.id.checkPasswordFab);
        checkPasswordFab.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    private void verificationDesSaisies(){
        progressDialog.setMessage("Connexion...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String password = userPasswordTv.getText().toString().trim();
        String confirm = userPasswordConfirmationTv.getText().toString().trim();
        if (TextUtils.isEmpty(password)){
            Toast.makeText(NewUserActivity.this, "Renseignez votre mot de passe!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userQuality.equals("new")){
            if (!password.equals(confirm)){
                Toast.makeText(NewUserActivity.this, "Impossible de confirmer votre mot de passe!", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                String email = user.getEmail();
                                String uid = user.getUid();

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uEmail", email);
                                hashMap.put("uId", uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(NewUserActivity.this, "Registered... \n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(NewUserActivity.this, ProfilActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(NewUserActivity.this, "Erreur lors de la sauvegarde des infos!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(NewUserActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else if (userQuality.equals("old")){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                startActivity(new Intent(NewUserActivity.this, HomeActivity.class));
                                finish();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(NewUserActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}