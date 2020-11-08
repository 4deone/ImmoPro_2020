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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    private ProgressDialog progressDialog;

    private ImageView coverIv;
    private EditText postTitreEdtv;
    private EditText postDescriptionEdtv;
    private FloatingActionButton createPostFab;
    private CheckBox publicOrPrivateCb;

    private String myUID;
    private String myNAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        checkUser();
        initVues();
        coverIv.setOnClickListener(this);
        createPostFab.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        checkUser();
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.coverIv){
            showAvatarPickDialog();
        }else if (v.getId() == R.id.createPostFab){
            verificationSaisie();
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
                        Toast.makeText(CreatePostActivity.this, "Please enable camera & storage permissions.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CreatePostActivity.this, "Please enable storage permissions.", Toast.LENGTH_SHORT).show();
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
                coverIv.setImageURI(imageUri);
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                coverIv.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkUser(){
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null){
            myUID = fUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(myUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        myNAME = ds.child("uName").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreatePostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            startActivity(new Intent(CreatePostActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initVues(){
        coverIv = findViewById(R.id.coverIv);
        postTitreEdtv = findViewById(R.id.postTitreEdtv);
        postDescriptionEdtv = findViewById(R.id.postDescriptionEdtv);
        createPostFab = findViewById(R.id.createPostFab);
        publicOrPrivateCb = findViewById(R.id.publicOrPrivateCb);
        progressDialog = new ProgressDialog(this);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void showAvatarPickDialog() {
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
        values.put(MediaStore.Images.Media.TITLE, "Article Image Icon Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Article Image Icon Description");
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

    private void verificationSaisie() {
        String titre = postTitreEdtv.getText().toString().trim();
        String description = postDescriptionEdtv.getText().toString().trim();

        if (TextUtils.isEmpty(titre)){
            Toast.makeText(CreatePostActivity.this, "Saisissez le titre", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description)){
            Toast.makeText(CreatePostActivity.this, "Saisissez le slogan", Toast.LENGTH_SHORT).show();
            return;
        }

        prepareData(
                ""+titre,
                ""+description);
    }

    private void prepareData(String titre, String description) {
        HashMap<String, String> hashMapPost = new HashMap<>();
        hashMapPost.put("pTitre", titre);
        hashMapPost.put("pDescription", description);
        hashMapPost.put("pCreator", myUID);

        hashMapPost.put("pNote", "0");
        hashMapPost.put("pNLikes", "0");
        hashMapPost.put("pNVues", "0");
        hashMapPost.put("pNComments", "0");

        if (publicOrPrivateCb.isChecked()){
            hashMapPost.put("pPublicOrPrivate", "public");
        }else{
            hashMapPost.put("pPublicOrPrivate", "private");
        }

        savePostData(hashMapPost);
    }

    private void savePostData(HashMap<String, String> hashMapPost) {
        progressDialog.setTitle("Nouveau post");
        progressDialog.setMessage("Creation d'un nouveau post...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        hashMapPost.put("pId", timestamp);
        hashMapPost.put("pDate", timestamp);

        if (imageUri != null){
            saveCoverDatabase(hashMapPost, timestamp);
        }else{
            uploadData(
                    hashMapPost,
                    ""+timestamp);
        }
    }

    private void saveCoverDatabase(HashMap<String, String> hashMapPost, String timestamp) {
        String filePathAndName = "Posts/"+"post_"+ myUID + "_" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()){
                    hashMapPost.put("pCover", downloadUri);
                    uploadData(
                            hashMapPost,
                            ""+timestamp);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(HashMap<String, String> hashMapPost, String timestamp) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.child(timestamp).setValue(hashMapPost)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String titre = postTitreEdtv.getText().toString().trim();
                        String description = postDescriptionEdtv.getText().toString().trim();
                        prepareNotification(
                                ""+timestamp,
                                ""+myNAME+" a ajouté un post",
                                ""+ titre + "\n" + description,
                                "PostNotification",
                                "POST");
                        progressDialog.dismiss();
                        Toast.makeText(CreatePostActivity.this, "Opération réussie!", Toast.LENGTH_SHORT).show();
                        resetViews();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void prepareNotification(String pId, String titre, String description, String notificationType, String notificationTopic){
        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = titre;
        String NOTIFICATION_DESCRIPTION = description;
        String NOTIFICATION_TYPE = notificationType;

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", myUID);
            notificationBodyJo.put("pId", pId);
            notificationBodyJo.put("pTitre", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_DESCRIPTION);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);
        }catch (Exception e){
            Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+ response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CreatePostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAZCzfbyE:APA91bFgMvLCJrB-y0h_8jzGXYePXl5gicO0KcfMwXWRK8rHNv81UGdhvxzD9_SGADkKFxbvXFXut6ZX7bFx6RleoFbawR7igk-t1BALJGyFrSuhSZYu9hQkAimLNOya0REEAfRe2rYl");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void resetViews() {
        postTitreEdtv.setText(null);
        postTitreEdtv.setHint("Titre du post");
        postDescriptionEdtv.setText(null);
        postDescriptionEdtv.setHint("Description du post");
        coverIv.setImageResource(R.drawable.ic_post);
    }

}