package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import cm.deone.corp.imopro.models.User;

public class SettingsActivity extends AppCompatActivity {

    private static final String TOPIC_POST_NOTIFICATION = "POST";
    private static final String TOPIC_COMMENT_NOTIFICATION = "COMMENT";
    private static final String TOPIC_GALLERY_NOTIFICATION = "GALLERY";

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private SwitchCompat postNotificationSw;
    private SwitchCompat commentNotificationSw;
    private SwitchCompat galleryNotificationSw;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ImageView avatarIv;

    private TextView deviseTv;
    private TextView emailTv;
    private TextView phoneTv;
    private ProgressDialog progressDialog;

    private String myUID;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initVues();

        checkUsers();

        getUserInfos();

        boolean isPostEnable = sharedPreferences.getBoolean(""+TOPIC_POST_NOTIFICATION, false);
        boolean isCommentEnable = sharedPreferences.getBoolean(""+TOPIC_COMMENT_NOTIFICATION, false);
        boolean isGalleryEnable = sharedPreferences.getBoolean(""+TOPIC_GALLERY_NOTIFICATION, false);

        if (isPostEnable) {
            postNotificationSw.setChecked(true);
        }else{
            postNotificationSw.setChecked(false);
        }

        if (isCommentEnable) {
            commentNotificationSw.setChecked(true);
        }else{
            commentNotificationSw.setChecked(false);
        }

        if (isGalleryEnable) {
            galleryNotificationSw.setChecked(true);
        }else{
            galleryNotificationSw.setChecked(false);
        }

        postNotificationSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                if (isChecked){
                    suscribeNotification(""+TOPIC_POST_NOTIFICATION);
                }else{
                    unsuscribeNotification(""+TOPIC_POST_NOTIFICATION);
                }
            }
        });
        commentNotificationSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_COMMENT_NOTIFICATION, isChecked);
                editor.apply();
                if (isChecked){
                    suscribeNotification(""+TOPIC_COMMENT_NOTIFICATION);
                }else{
                    unsuscribeNotification(""+TOPIC_COMMENT_NOTIFICATION);
                }
            }
        });
        galleryNotificationSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_GALLERY_NOTIFICATION, isChecked);
                editor.apply();
                if (isChecked){
                    suscribeNotification(""+TOPIC_GALLERY_NOTIFICATION);
                }else{
                    unsuscribeNotification(""+TOPIC_GALLERY_NOTIFICATION);
                }
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(scrollRange == -1){
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange+verticalOffset == 0){
                    //collapsingToolbarLayout.setTitle("One");
                    toolbar.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                    isShow = true;
                }else if (isShow){
                    //collapsingToolbarLayout.setTitle("Two");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

    }

    private void unsuscribeNotification(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Vous allez recevoir de notifications";
                        if(!task.isSuccessful()){
                            msg = "Subscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void suscribeNotification(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Vous n'allez plus recevoir de notifications";
                        if(!task.isSuccessful()){
                            msg = "Unsubscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        checkUsers();
        getUserInfos();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUsers();
        getUserInfos();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_profil){
            showEditProfileDialog();
        }else if (item.getItemId() == R.id.menu_exit_app){
            exitAccount();
        }else if (item.getItemId() == R.id.menu_delete_account){
            deleteAccount();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();
                avatarIv.setImageURI(imageUri);
                uploadAvatar(imageUri);
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                avatarIv.setImageURI(imageUri);
                uploadAvatar(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(SettingsActivity.this, "Please enable camera & storage permissions.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SettingsActivity.this, "Please enable storage permissions.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
    }

    private void initVues(){

        appBarLayout = findViewById(R.id.appBarLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("Notification_SP", MODE_PRIVATE);

        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Update User informations");

        postNotificationSw = findViewById(R.id.postNotificationSw);
        commentNotificationSw = findViewById(R.id.commentNotificationSw);
        galleryNotificationSw = findViewById(R.id.galleryNotificationSw);

        avatarIv = findViewById(R.id.avatarIv);

        deviseTv = findViewById(R.id.deviseTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);

        firebaseAuth = FirebaseAuth.getInstance();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void checkUsers(){
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        if (fUser != null){
            myUID = fUser.getUid();
        }else{
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        }
    }

    private void getUserInfos() {
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    if (myUID.equals(user.getuId())){

                        collapsingToolbarLayout.setTitle(""+user.getuName());

                        /*toolbar.setTitle(""+user.getuName());
                        toolbar.setSubtitle(""+user.getuDevise());*/

                        deviseTv.setText(user.getuDevise());
                        emailTv.setText(user.getuEmail());
                        phoneTv.setText(user.getuPhone());

                        try {
                            Picasso.get().load(user.getuAvatar()).placeholder(R.drawable.ic_user).into(avatarIv);
                        }catch(Exception e){
                            Picasso.get().load(R.drawable.ic_user).into(avatarIv);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suppression de compte");
        builder.setMessage("Etes-vous sur de vouloir supprimer votre compte ?\nNB : Cette action supprimera de manière définitive toutes vos informations personnelles.")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete all posts
                        // Delete all user

                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    checkUsers();
                                }else{
                                    Toast.makeText(SettingsActivity.this, "Impossible de supprimer ce compte", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void exitAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Déconnexion");
        builder.setMessage("Etes-vous sur de vouloir déconnecter votre compte ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth.signOut();
                        checkUsers();
                    }
                }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showEditProfileDialog() {
        String[] options = {"Edit avatar", "Edit username", "Edit user phone", "Edit user dévise" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    progressDialog.setMessage("Uploading user avatar ...");
                    showImagePicDialog();
                }else if (which == 1){
                    progressDialog.setMessage("Uploading username");
                    showNameDeviseUpdateDialog("uName");
                }else if (which == 2){
                    progressDialog.setMessage("Uploading user phone");
                    showPhoneUpdateDialog();
                }else if (which == 3){
                    progressDialog.setMessage("Uploading user dévise");
                    showNameDeviseUpdateDialog("uDevise");
                }
            }
        });
        builder.create().show();
    }

    private void showPhoneUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update phone");
        final View customLayout = getLayoutInflater().inflate(R.layout.item_phone, null);
        builder.setView(customLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText updatePhoneEt = customLayout.findViewById(R.id.updatePhoneEt);
                String phone = updatePhoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phone)){
                    Toast.makeText(SettingsActivity.this, "Ce champ ne doit pas etre vide!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendDialogDataPhoneToActivity(""+phone);
            }
        });
        builder.create().show();
    }

    private void showNameDeviseUpdateDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update "+type);
        final View customLayout = getLayoutInflater().inflate(R.layout.item_name, null);
        EditText editText = customLayout.findViewById(R.id.updateEt);
        editText.setHint(type.equals("uName") ? "Votre nom complet" : "Votre dévise");
        builder.setView(customLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText updateEt = customLayout.findViewById(R.id.updateEt);
                String update = updateEt.getText().toString().trim();
                if (TextUtils.isEmpty(update)){
                    Toast.makeText(SettingsActivity.this, "Ce champ ne doit pas etre vide!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendDialogDataToActivity(""+type, ""+update);
            }
        });
        builder.create().show();
    }

    private void sendDialogDataPhoneToActivity(String phone) {
        progressDialog.show();
        HashMap<String, Object> results = new HashMap<>();
        results.put("uPhone", phone);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUID).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, "Updated phone...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDialogDataToActivity(String type, String update) {
        progressDialog.show();
        HashMap<String, Object> results = new HashMap<>();
        results.put(type, update);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUID).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, "Updated " + (type.equals("uName") ? "Votre nom complet" : "Votre dévise") + "...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Images");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }else if (which == 1){
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "User Image Icon Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "User Image Icon Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void uploadAvatar(Uri uri) {
        progressDialog.show();
        String filePathAndName = "Users/"+"user_"+myUID;

        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                final Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();
                    results.put("uAvatar", downloadUri.toString());

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.child(myUID).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Images Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error Updating Images...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //updateAvatarInPostAndComment(downloadUri);
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAvatarInPostAndComment(Uri downloadUri) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pCreator").equalTo(myUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String child = ds.getKey();
                    assert child != null;
                    dataSnapshot.getRef().child(child).child("uAvatar").setValue(downloadUri.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String child = ds.getKey();
                    if (dataSnapshot.child(child).hasChild("Comments")){
                        String child1 = ""+dataSnapshot.child(child).getKey();
                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("cCreator").equalTo(myUID);
                        child2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds:dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uAvatar").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}