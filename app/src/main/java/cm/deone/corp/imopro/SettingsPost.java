package cm.deone.corp.imopro;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.outils.AppLocationService;
import cm.deone.corp.imopro.outils.LocationAddress;

import static cm.deone.corp.imopro.outils.Constant.DB_SIGNALEMENT;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_GALLERY_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_GEOLOCALISTION_NOTIFICATION;

public class SettingsPost extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_CODE_PERMISSION = 500;
    private final String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    private AppLocationService appLocationService;

    private DatabaseReference ref;

    private boolean mProcessSignal = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor ;

    private Post post;

    private static TextView tvLocalisation;
    private TextView tvSignalerPost;
    private RelativeLayout rlDelete;
    private RelativeLayout rlSignaler;

    private String myUID;
    private String myNAME;
    private String myAVATAR;
    private String pId;
    private String pCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_post);
        checkUser();
        initVues();
        getPost();
    }

    @Override
    protected void onStart() {
        checkUser();
        getPost();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUser();
        getPost();
        super.onResume();
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null){
            startActivity(new Intent(SettingsPost.this, MainActivity.class));
            finish();
        }else{
            myUID = fUser.getUid();
            Query query = FirebaseDatabase.getInstance().getReference("Users")
                    .orderByKey().equalTo(myUID);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        myNAME = ds.child("uName").getValue(String.class);
                        myAVATAR = ds.child("uAvatar").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SettingsPost.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            //checkPermission(""+mPermission, REQUEST_CODE_PERMISSION);
        }
    }

    /*private void checkPermission(String permission, int requestCode) {
        try {
            if (ActivityCompat.checkSelfPermission(SettingsPost.this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SettingsPost.this, new String[]{permission}, requestCode);
            }else{
                //Toast.makeText(SettingsPost.this, "LOCATION permission granted!", Toast.LENGTH_SHORT).show();
                getPostAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void initVues() {
        pId = getIntent().getStringExtra("pId");
        pCreator = getIntent().getStringExtra("pCreator");
        ref = FirebaseDatabase.getInstance().getReference("Posts");
        sharedPreferences = getSharedPreferences("POST_NOTIF_SP", MODE_PRIVATE);
        appLocationService = new AppLocationService(SettingsPost.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvLocalisation = findViewById(R.id.tvLocalisation);
        tvSignalerPost = findViewById(R.id.tvSignalerPost);
        rlDelete = findViewById(R.id.rlDelete);
        rlDelete.setVisibility(pCreator.equals(myUID)? View.VISIBLE: View.GONE);
        rlSignaler = findViewById(R.id.rlSignaler);
        rlSignaler.setVisibility(pCreator.equals(myUID)? View.GONE: View.VISIBLE);

        SwitchCompat swtvGeolocalisation = findViewById(R.id.swtvGeolocalisation);
        SwitchCompat galleryNotificationSw = findViewById(R.id.galleryNotificationSw);
        galleryNotificationSw.setVisibility(pCreator.equals(myUID)? View.GONE: View.VISIBLE);
        SwitchCompat commentNotificationSw = findViewById(R.id.commentNotificationSw);

        boolean isCommentEnable = sharedPreferences.getBoolean(""+TOPIC_COMMENT_NOTIFICATION+""+pId, false);
        boolean isGalleryEnable = sharedPreferences.getBoolean(""+TOPIC_GALLERY_NOTIFICATION+""+pId, false);
        boolean isGeolocalisationEnable = sharedPreferences.getBoolean(""+TOPIC_GEOLOCALISTION_NOTIFICATION+""+pId, false);

        commentNotificationSw.setChecked(isCommentEnable);
        galleryNotificationSw.setChecked(isGalleryEnable);
        swtvGeolocalisation.setChecked(isGeolocalisationEnable);

        commentNotificationSw.setOnCheckedChangeListener(this);
        galleryNotificationSw.setOnCheckedChangeListener(this);
        swtvGeolocalisation.setOnCheckedChangeListener(this);
    }

    private void unsuscribeNotification(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = ""+getResources().getString(R.string.not_receive_notification);
                        if(!task.isSuccessful()){
                            msg = ""+getResources().getString(R.string.subscription_failed);
                        }
                        Toast.makeText(SettingsPost.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void suscribeNotification(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = ""+getResources().getString(R.string.receive_notification);
                        if(!task.isSuccessful()){
                            msg = ""+getResources().getString(R.string.unsubscription_failed);
                        }
                        Toast.makeText(SettingsPost.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getPost() {
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
    }

    private void deleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsPost.this);
        builder.setTitle("Sélectionner une action :");
        builder.setMessage("Etes vous sure de vouloir supprime cette publication ?");
        builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePost();
            }
        }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deletePost() {
        //
    }

    private void showGiveWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsPost.this);
        builder.setTitle("Signaler ce post");
        builder.setMessage(mProcessSignal ? "Etes-vous sur de vouloir supprimer le signalement ce post ?" : "Etes-vous sur de vouloir signaler ce post ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mProcessSignal){
                            setPostSignalement("");
                        }
                        else{
                            procederAuSignalement();
                        }
                    }
                }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void procederAuSignalement() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsPost.this);
        builder.setTitle("Signaler ce post");
        final View customLayout = getLayoutInflater().inflate(R.layout.item_signaler, null);
        builder.setView(customLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText signalerEdtv = customLayout.findViewById(R.id.signalerEdtv);
                String signaler = signalerEdtv.getText().toString().trim();

                if (TextUtils.isEmpty(signaler)){
                    Toast.makeText(SettingsPost.this, "Vous n'avez donné aucune raison!", Toast.LENGTH_SHORT).show();
                    return;
                }

                setPostSignalement(""+signaler);
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void setPostSignalement(String signaler) {
        if (mProcessSignal){
            ref.child(post.getpId()).child("pNSignals")
                    .setValue(""+ (Integer.parseInt(post.getpNSignals()) - 1));
            ref.child(post.getpId()).child(DB_SIGNALEMENT).child(myUID).removeValue();
            mProcessSignal = false;
        }else {
            ref.child(post.getpId()).child("pNSignals")
                    .setValue(""+ (Integer.parseInt(post.getpNSignals()) + 1));

            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("sId", myUID);
            hashMap.put("sMessage", signaler);
            hashMap.put("sDate", timestamp);
            hashMap.put("uName", myNAME);
            hashMap.put("uAvatar", myAVATAR);

            ref.child(post.getpId()).child(DB_SIGNALEMENT).child(myUID).setValue(hashMap);
            mProcessSignal = true;
        }
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(SettingsPost.this, mPermission) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{mPermission}, REQUEST_CODE_PERMISSION);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsPost.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        SettingsPost.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void getPostAddress() {
        if (!checkLocationPermission()){
            requestLocationPermission();
        }else {
            Location location = appLocationService.getLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LocationAddress locationAddress = new LocationAddress();
                LocationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
            } else {
                showSettingsAlert();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getPostAddress();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvSignalerPost && !pCreator.equals(myUID)){
            showGiveWarningDialog();
        }else if (v.getId() == R.id.tvDeletePost){
            deleteConfirmation();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.commentNotificationSw :
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_COMMENT_NOTIFICATION+""+pId, isChecked);
                editor.apply();
                if (isChecked){
                    suscribeNotification(""+TOPIC_COMMENT_NOTIFICATION+""+pId);
                }else{
                    unsuscribeNotification(""+TOPIC_COMMENT_NOTIFICATION+""+pId);
                }
                break;
            case R.id.galleryNotificationSw :
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_GALLERY_NOTIFICATION+""+pId, isChecked);
                editor.apply();
                if (isChecked){
                    suscribeNotification(""+TOPIC_GALLERY_NOTIFICATION+""+pId);
                }else{
                    unsuscribeNotification(""+TOPIC_GALLERY_NOTIFICATION+""+pId);
                }
                break;
            case R.id.swtvGeolocalisation :
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_GEOLOCALISTION_NOTIFICATION+""+pId, isChecked);
                editor.apply();
                if (isChecked){
                    getPostAddress();
                }else{
                    tvLocalisation.setText("Pas de localisation");
                }
                break;
            default:
        }
    }

    private final ValueEventListener postInfosVal =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                if (ds.child(DB_SIGNALEMENT).hasChild(myUID))
                    mProcessSignal = true;
                else
                    mProcessSignal = false;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(SettingsPost.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private static class GeocoderHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            tvLocalisation.setText(locationAddress);
        }
    }

}