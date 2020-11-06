package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfilActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference reference;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    private String myUID;
    private String myEmail;

    private ImageView avatarIv;
    private EditText fullNameEt;
    private EditText phoneEt;
    private EditText deviseEt;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        checkUser();
        initViews();
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.profilFab){
            verificationSaisie();
        }else if(v.getId()==R.id.avatarIv){
            showGroupAvatarPickDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(ProfilActivity.this, "Please enable camera & storage permissions.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(ProfilActivity.this, "Please enable storage permissions.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();
                avatarIv.setImageURI(imageUri);
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                avatarIv.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkUser(){
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser==null){
            startActivity(new Intent(ProfilActivity.this, MainActivity.class));
            finish();
        }else{
            myUID = fUser.getUid();
            myEmail = fUser.getEmail();
        }
    }

    private void initViews(){
        reference = FirebaseDatabase.getInstance().getReference("Users");
        avatarIv = findViewById(R.id.avatarIv);
        fullNameEt = findViewById(R.id.fullNameEt);
        phoneEt = findViewById(R.id.phoneEt);
        deviseEt = findViewById(R.id.deviseEt);
        progressDialog = new ProgressDialog(this);
        FloatingActionButton profilFab = findViewById(R.id.profilFab);
        profilFab.setOnClickListener(this);
        avatarIv.setOnClickListener(this);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void verificationSaisie() {

        String fullName = fullNameEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String devise = deviseEt.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)){
            Toast.makeText(ProfilActivity.this, "Entrez votre nom complet!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)){
            Toast.makeText(ProfilActivity.this, "Entrez votre numéro de téléphone!", Toast.LENGTH_SHORT).show();
            return;
        }
        prepareData(
                ""+fullName,
                ""+phone,
                ""+devise);
    }

    private void prepareData(String fullName, String phone, String devise) {
        HashMap<String, String> hashMapUser = new HashMap<>();
        hashMapUser.put("uId", myUID);
        hashMapUser.put("uEmail", myEmail);
        hashMapUser.put("uName", fullName);
        hashMapUser.put("uPhone", phone);
        hashMapUser.put("uDevise", devise);

        saveUserData(hashMapUser);
    }

    private void saveUserData(HashMap<String, String> hashMapUser) {
        progressDialog.setTitle("ProfilActivity utilisateur");
        progressDialog.setMessage("Mise à jour du profil de l'utilisateur...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        hashMapUser.put("uDate", timestamp);

        if (imageUri!=null){
            String filePathAndName = "Users/"+"user_"+myUID;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();
                    if (uriTask.isSuccessful()){
                        hashMapUser.put("uAvatar", downloadUri);
                        uploadUserData(hashMapUser);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfilActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            uploadUserData(hashMapUser);
        }

    }

    private void uploadUserData(HashMap<String, String> hashMapUser) {
        reference.child(myUID).setValue(hashMapUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfilActivity.this, "Sauvegarde réussie!", Toast.LENGTH_SHORT).show();
                        resetViews();
                        startActivity(new Intent(ProfilActivity.this, HomeActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfilActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void showGroupAvatarPickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner une image:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0 :
                        if (!checkCameraPermissions()){
                            requestCameraPermissions();
                        }else{
                            pickFromCamera();
                        }
                        break;
                    case 1 :
                        if (!checkStoragePermissions()){
                            requestStoragePermissions();
                        }else{
                            pickFromGallery();
                        }
                        break;
                    default:
                }
            }
        });
        builder.create().show();
    }

    private void resetViews() {
        fullNameEt.setText(null);
        fullNameEt.setHint("Votre nom complet");
        phoneEt.setText(null);
        phoneEt.setHint("Votre téléphone");
        deviseEt.setText(null);
        deviseEt.setHint("Votre dévise");
        avatarIv.setImageResource(R.drawable.ic_user);
    }
}