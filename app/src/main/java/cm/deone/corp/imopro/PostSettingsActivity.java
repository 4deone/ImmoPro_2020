package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cm.deone.corp.imopro.adapter.NotParentAdaptor;
import cm.deone.corp.imopro.models.NotChildItem;
import cm.deone.corp.imopro.models.NotParentItem;
import cm.deone.corp.imopro.models.Post;

import static cm.deone.corp.imopro.outils.Constant.TOPIC_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_GALLERY_NOTIFICATION;

public class PostSettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{


    private DatabaseReference ref;

    private  Toolbar toolbar;
    private Post post;
    private String pId;
    private String pCreator;
    private String myUID;
    private String myNAME;
    private String myAVATAR;

    private RecyclerView activitesRv;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor ;
    private List<NotParentItem> parentList;
    private List<NotChildItem> childList;
    private NotParentAdaptor notParentAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_settings);
        checkUser();
        initViews();
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
            default:
        }
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null){
            startActivity(new Intent(PostSettingsActivity.this, MainActivity.class));
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
                    Toast.makeText(PostSettingsActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initViews(){
        pId = getIntent().getStringExtra("pId");
        pCreator = getIntent().getStringExtra("pCreator");
        ref = FirebaseDatabase.getInstance().getReference("Posts");
        sharedPreferences = getSharedPreferences("POST_NOTIF_SP", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Post settings");

        activitesRv = findViewById(R.id.activitesRv);

        SwitchCompat galleryNotificationSw = findViewById(R.id.galleryNotificationSw);
        SwitchCompat commentNotificationSw = findViewById(R.id.commentNotificationSw);

        boolean isCommentEnable = sharedPreferences.getBoolean(""+TOPIC_COMMENT_NOTIFICATION+""+pId, false);
        boolean isGalleryEnable = sharedPreferences.getBoolean(""+TOPIC_GALLERY_NOTIFICATION+""+pId, false);

        commentNotificationSw.setChecked(isCommentEnable);
        galleryNotificationSw.setChecked(isGalleryEnable);

        commentNotificationSw.setOnCheckedChangeListener(this);
        galleryNotificationSw.setOnCheckedChangeListener(this);
    }

    private void getPost() {
        parentList = new ArrayList<>();
        childList = new ArrayList<>();
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
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
                        Toast.makeText(PostSettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PostSettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNotificationHisUser(DataSnapshot ds) {
        NotParentItem notParent = new NotParentItem(""+myAVATAR, ""+myNAME);
        if (ds.child("Likes").hasChild(myUID)){
            NotChildItem notChild = new NotChildItem("Like",
                    "Vous avez aimé cette publication",
                    ""+ds.child("Likes").child(myUID).child("lDate").getValue(String.class));
            childList.add(notChild);
        }
        if (ds.child("Favorites").hasChild(myUID)){
            NotChildItem notChild = new NotChildItem("Favorite",
                    "Cette publication est votre favorie",
                    ""+ds.child("Favorites").child(myUID).child("fDate").getValue(String.class));
            childList.add(notChild);
        }
        notParent.setNotChildItemList(childList);
        parentList.add(notParent);
        notParentAdaptor = new NotParentAdaptor(PostSettingsActivity.this, parentList);
        activitesRv.setAdapter(notParentAdaptor);
    }

    private final ValueEventListener postInfosVal =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            childList.clear();
            parentList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                toolbar.setSubtitle(""+post.getpTitre());
                if (post.getpCreator().equals(myUID)){


                }else{

                    showNotificationHisUser(ds);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostSettingsActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}