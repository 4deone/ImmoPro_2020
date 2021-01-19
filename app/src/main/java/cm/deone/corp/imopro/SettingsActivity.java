package cm.deone.corp.imopro;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.adapter.CommentAdaptor;
import cm.deone.corp.imopro.adapter.PostsAdaptor;
import cm.deone.corp.imopro.adapter.SettingsPostAdaptor;
import cm.deone.corp.imopro.adapter.SignalerAdaptor;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.Signaler;
import cm.deone.corp.imopro.models.User;
import cm.deone.corp.imopro.outils.AppLocationService;
import cm.deone.corp.imopro.outils.LocationAddress;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.CAMERA_REQUEST_CODE;
import static cm.deone.corp.imopro.outils.Constant.DB_COMMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_POST;
import static cm.deone.corp.imopro.outils.Constant.DB_SIGNALEMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_USER;
import static cm.deone.corp.imopro.outils.Constant.IMAGE_PICK_CAMERA_CODE;
import static cm.deone.corp.imopro.outils.Constant.IMAGE_PICK_GALLERY_CODE;
import static cm.deone.corp.imopro.outils.Constant.LOCATION_REQUEST_CODE;
import static cm.deone.corp.imopro.outils.Constant.STORAGE_REQUEST_CODE;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_GALLERY_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_POST_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.suscribeNotification;
import static cm.deone.corp.imopro.outils.Constant.unsuscribeNotification;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor ;

    private ImageView avatarIv;

    private static TextView deviseTv;
    private TextView emailTv;
    private TextView phoneTv;

    private TextView userInfoTv;

    private RecyclerView rvMesposts;
    private SettingsPostAdaptor settingsPostAdaptor;
    private List<Post> postList;

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
        allPosts();
    }

    @Override
    protected void onStart() {
        checkUsers();
        getUserInfos();
        allPosts();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUsers();
        getUserInfos();
        allPosts();
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvLogoutUser: exitAccount();
                break;
            case R.id.tvDeleteUser: deleteAccount();
                break;
            case R.id.tvVisitorBlocked: showBlockedVisitorDialog();
                break;
            default:
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.camera_permission), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.storage_permission), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.postNotificationSw :
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                if (isChecked){
                    suscribeNotification(SettingsActivity.this, ""+TOPIC_POST_NOTIFICATION);
                }else{
                    unsuscribeNotification(SettingsActivity.this, ""+TOPIC_POST_NOTIFICATION);
                }
                break;
            default:
        }
    }

    private void initVues(){

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.settings));
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("Notification_SP", MODE_PRIVATE);

        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getResources().getString(R.string.update_user_information));

        SwitchCompat postNotificationSw = findViewById(R.id.postNotificationSw);

        avatarIv = findViewById(R.id.avatarIv);
        rvMesposts = findViewById(R.id.rvMesposts);

        deviseTv = findViewById(R.id.deviseTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        userInfoTv = findViewById(R.id.userInfoTv);

        TextView tvVisitorBlocked = findViewById(R.id.tvVisitorBlocked);

        firebaseAuth = FirebaseAuth.getInstance();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        boolean isPostEnable = sharedPreferences.getBoolean(""+TOPIC_POST_NOTIFICATION, false);

        if (isPostEnable) {
            postNotificationSw.setChecked(true);
        }else{
            postNotificationSw.setChecked(false);
        }

        postNotificationSw.setOnCheckedChangeListener(this);
        tvVisitorBlocked.setOnClickListener(this);

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
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference(""+DB_USER);
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

    private void allPosts() {
        postList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    if (myUID.equals(post.getpCreator()))
                        postList.add(post);
                    Collections.sort(postList);
                    settingsPostAdaptor = new SettingsPostAdaptor(SettingsActivity.this, postList);
                    rvMesposts.setAdapter(settingsPostAdaptor);
                    settingsPostAdaptor.setOnItemClickListener(new ViewsClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(SettingsActivity.this, PostActivity.class);
                            intent.putExtra("pId", postList.get(position).getpId());
                            intent.putExtra("pCreator", postList.get(position).getpCreator());
                            intent.putExtra("nType", DB_POST);
                            startActivity(intent);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBlockedVisitorDialog() {
        Dialog dialog = new Dialog(SettingsActivity.this);
        dialog.setContentView(R.layout.dialog_signaler);
        dialog.show();
    }

    private void showEditProfileDialog() {
        String[] options = {""+getResources().getString(R.string.edit_avatar),
                ""+getResources().getString(R.string.edit_username),
                ""+getResources().getString(R.string.edit_user_phone),
                ""+getResources().getString(R.string.edit_slogan) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    progressDialog.setMessage(""+getResources().getString(R.string.updating_avatar));
                    showImagePicDialog();
                }else if (which == 1){
                    progressDialog.setMessage(""+getResources().getString(R.string.updating_username));
                    showNameDeviseUpdateDialog("uName");
                }else if (which == 2){
                    progressDialog.setMessage(""+getResources().getString(R.string.updating_userphone));
                    showPhoneUpdateDialog();
                }else if (which == 3){
                    progressDialog.setMessage(""+getResources().getString(R.string.updating_userslogan));
                    showNameDeviseUpdateDialog("uDevise");
                }
            }
        });
        builder.create().show();
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(""+getResources().getString(R.string.clear_your_account));
        builder.setMessage(""+getResources().getString(R.string.supprimer_compte))
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
                                    Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.not_delete_account), Toast.LENGTH_SHORT).show();
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
        builder.setTitle(""+getResources().getString(R.string.deconnexion));
        builder.setMessage(""+getResources().getString(R.string.verification_deconnexion))
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
                    Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.champ_vide), Toast.LENGTH_SHORT).show();
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
        editText.setHint(type.equals("uName") ? ""+getResources().getString(R.string.votre_nom_complet) : ""+getResources().getString(R.string.votre_devise));
        builder.setView(customLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText updateEt = customLayout.findViewById(R.id.updateEt);
                String update = updateEt.getText().toString().trim();
                if (TextUtils.isEmpty(update)){
                    Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.champ_vide), Toast.LENGTH_SHORT).show();
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(""+DB_USER);
        ref.child(myUID).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.toast_updated_phone), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SettingsActivity.this, "Updated "
                        + (type.equals("uName") ? ""+getResources().getString(R.string.votre_nom_complet)
                        : ""+getResources().getString(R.string.votre_devise)) + "...", Toast.LENGTH_SHORT).show();
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
        String[] options = {this.getResources().getString(R.string.camera), this.getResources().getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.pick_image));
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

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(""+DB_USER);
                    reference.child(myUID).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.toast_images_updated), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.toast_error_updating_images), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //updateAvatarInPostAndComment(downloadUri);
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, ""+getResources().getString(R.string.toast_some_error_occured), Toast.LENGTH_SHORT).show();
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(""+DB_POST);
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
                    if (dataSnapshot.child(child).hasChild(""+DB_COMMENT)){
                        String child1 = ""+dataSnapshot.child(child).getKey();
                        Query child2 = FirebaseDatabase.getInstance().getReference(""+DB_POST).child(child1)
                                .child(""+DB_COMMENT).orderByChild("cCreator").equalTo(myUID);
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