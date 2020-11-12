package cm.deone.corp.imopro;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import cm.deone.corp.imopro.models.User;

public class CreateGalleryActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    private String myUID;
    private String myNAME;
    private String pId;

    private ImageView iTemCoverIv;
    private EditText itemDescriptionEdtv;
    private FloatingActionButton createPostFab;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gallery);
        checkUser();
        initVues();
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null){
            myUID = fUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        if (user.getuId().equals(myUID))
                            myNAME = user.getuName();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreateGalleryActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            startActivity(new Intent(CreateGalleryActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initVues() {
        pId = getIntent().getStringExtra("pId");
        iTemCoverIv = findViewById(R.id.iTemCoverIv);
        itemDescriptionEdtv = findViewById(R.id.itemDescriptionEdtv);
        createPostFab = findViewById(R.id.createPostFab);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        progressDialog = new ProgressDialog(this);

        iTemCoverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarPickDialog();
            }
        });

        createPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificationSaisie();
            }
        });

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
                        Toast.makeText(CreateGalleryActivity.this, "Please enable camera & storage permissions.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CreateGalleryActivity.this, "Please enable storage permissions.", Toast.LENGTH_SHORT).show();
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
                iTemCoverIv.setImageURI(imageUri);
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                iTemCoverIv.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void verificationSaisie() {
        String description = itemDescriptionEdtv.getText().toString().trim();

        if (TextUtils.isEmpty(description)){
            Toast.makeText(CreateGalleryActivity.this, "Saisissez un description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null){
            Toast.makeText(CreateGalleryActivity.this, "Vous n'avez selectionner aucune image!", Toast.LENGTH_SHORT).show();
            return;
        }

        prepareData(""+description);
    }

    private void prepareData(String description) {
        HashMap<String, String> hashMapImage = new HashMap<>();
        hashMapImage.put("gDescription", description);

        saveImageData(hashMapImage);
    }

    private void saveImageData(HashMap<String, String> hashMapImage) {

        progressDialog.setTitle("Nouvelle Image");
        progressDialog.setMessage("Ajout d'une nouvelle image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        hashMapImage.put("gId", timestamp);
        hashMapImage.put("gDate", timestamp);

        saveCoverDatabase(hashMapImage, ""+timestamp);

    }

    private void saveCoverDatabase(HashMap<String, String> hashMapImage, String timestamp) {
        String filePathAndName = "Gallery/"+"gallery_"+ myUID + "_" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()){
                    hashMapImage.put("gImage", downloadUri);
                    uploadData(
                            hashMapImage,
                            ""+timestamp);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateGalleryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(HashMap<String, String> hashMapImage, String timestamp) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Gallery");
        reference.child(pId).child(timestamp).setValue(hashMapImage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /*String description = itemDescriptionEdtv.getText().toString().trim();
                        prepareNotification(
                                ""+timestamp,
                                ""+myNAME+" a ajouté une nouvelle image",
                                ""+description,
                                "GalleryNotification",
                                "GALLERY");*/
                        progressDialog.dismiss();
                        Toast.makeText(CreateGalleryActivity.this, "Opération réussie!", Toast.LENGTH_SHORT).show();
                        resetViews();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateGalleryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(CreateGalleryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendGalleryNotification(notificationJo);
    }

    private void sendGalleryNotification(JSONObject notificationJo) {
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
                        Toast.makeText(CreateGalleryActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image Icon Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image Icon Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void resetViews() {
        itemDescriptionEdtv.setText(null);
        itemDescriptionEdtv.setHint("Description de l'image");
        iTemCoverIv.setImageResource(R.drawable.ic_post);
    }

}