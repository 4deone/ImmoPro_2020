package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.deone.corp.imopro.adapter.CommentAdaptor;
import cm.deone.corp.imopro.adapter.GalleryAdaptor;
import cm.deone.corp.imopro.fragments.CommentFragment;
import cm.deone.corp.imopro.fragments.GalleryFragment;
import cm.deone.corp.imopro.fragments.HomeFragment;
import cm.deone.corp.imopro.fragments.NotificationsFragment;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Gallery;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.DB_COMMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_GALLERY;
import static cm.deone.corp.imopro.outils.Constant.DB_POST;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_GALLERY_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_POST_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_GALLERY_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_POST_NOTIFICATION;

public class PostActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnLongClickListener {

    private boolean userVue = true;
    private boolean mProcessLikes = false;
    private boolean mProcessFavorites = false;
    private boolean mProcessSignal = false;
    private Post post;
    private String pId;
    private String pCreator;
    private String myUID;
    private String myNAME;
    private String myAVATAR;
    private String numShared;
    private DatabaseReference ref;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView coverIv;
    private ImageButton likeIb;
    private ImageButton favoriteIb;
    private ImageButton noteIb;
    private ImageButton shareIb;
    private ImageButton signalerIb;
    private RelativeLayout rlLike;
    private RelativeLayout rlComment;
    private EditText commentEdtv;
    private TextView warningTv;
    private TextView vueTv;
    private TextView likeTv;
    private TextView commentTv;
    private TextView noteTv;
    private TextView postDescriptionTv;
    private RecyclerView postImagesRv;
    private RecyclerView commentsRv;
    private List<Gallery> galleryList;
    private GalleryAdaptor galleryAdaptor;
    private List<Comment> commentList;
    private CommentAdaptor commentAdaptor;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.likeIb && !pCreator.equals(myUID)){
            likePost();
        }else if (v.getId() == R.id.favoriteIb && !pCreator.equals(myUID)){
            favoritePost();
        }else if (v.getId() == R.id.shareIb){
            sharePost(post.getpTitre(), post.getpDescription());
        }else if (v.getId() == R.id.noteIb && !pCreator.equals(myUID)){
            showGiveNoteDialog();
        }else if (v.getId() == R.id.signalerIb && !pCreator.equals(myUID)){
            showGiveWarningDialog();
        }else if (v.getId() == R.id.sendIb && !pCreator.equals(myUID)){
            verificationDeSaisie();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.coverIv && pCreator.equals(myUID)){
            showCoverDialog();
        }
        return true;
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
            startActivity(new Intent(PostActivity.this, MainActivity.class));
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
                    Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initVues() {
        pId = getIntent().getStringExtra("pId");
        pCreator = getIntent().getStringExtra("pCreator");
        ref = FirebaseDatabase.getInstance().getReference("Posts");
        sharedPreferences = getSharedPreferences("POST_NOTIF_SP", MODE_PRIVATE);

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        coverIv = findViewById(R.id.coverIv);
        warningTv = findViewById(R.id.warningTv);
        vueTv = findViewById(R.id.vueTv);
        likeTv = findViewById(R.id.likeTv);
        commentTv = findViewById(R.id.commentTv);
        noteTv = findViewById(R.id.noteTv);
        likeIb = findViewById(R.id.likeIb);
        favoriteIb = findViewById(R.id.favoriteIb);
        noteIb = findViewById(R.id.noteIb);
        shareIb = findViewById(R.id.shareIb);
        signalerIb = findViewById(R.id.signalerIb);
        postDescriptionTv = findViewById(R.id.postDescriptionTv);
        postImagesRv = findViewById(R.id.postImagesRv);
        commentsRv = findViewById(R.id.commentsRv);
        RelativeLayout commentFooterRl = findViewById(R.id.commentFooterRl);
        RelativeLayout rlNotification = findViewById(R.id.rlNotification);

        rlLike = findViewById(R.id.rlLike);
        rlComment = findViewById(R.id.rlComment);

        rlNotification.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        likeIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        favoriteIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        noteIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        signalerIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        commentFooterRl.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        warningTv.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);

        SwitchCompat galleryNotificationSw = findViewById(R.id.galleryNotificationSw);
        SwitchCompat commentNotificationSw = findViewById(R.id.commentNotificationSw);

        boolean isCommentEnable = sharedPreferences.getBoolean(
                ""+TOPIC_COMMENT_NOTIFICATION+""+pId,
                false);
        boolean isGalleryEnable = sharedPreferences.getBoolean(
                ""+TOPIC_GALLERY_NOTIFICATION+""+pId,
                false);

        commentNotificationSw.setChecked(isCommentEnable);
        galleryNotificationSw.setChecked(isGalleryEnable);

        commentNotificationSw.setOnCheckedChangeListener(this);
        galleryNotificationSw.setOnCheckedChangeListener(this);
        coverIv.setOnLongClickListener(this);
        findViewById(R.id.sendIb).setOnClickListener(this);
        likeIb.setOnClickListener(this);
        favoriteIb.setOnClickListener(this);
        noteIb.setOnClickListener(this);
        shareIb.setOnClickListener(this);
        signalerIb.setOnClickListener(this);
    }

    private void getPost() {
        galleryList = new ArrayList<>();
        commentList = new ArrayList<>();
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
    }

    private void sharedPost() {
        ref.child(post.getpId()).child("pNShares").setValue(""+(Integer.parseInt(post.getpNLikes())+1));
        ref.child(post.getpId()).child("Shares").child(myUID).setValue(TextUtils.isEmpty(numShared)?"1":""+(Integer.parseInt(numShared)+1));
    }

    private void showCoverDialog() {
        String[] options = {"Créer une galerie", "Modifier l'image"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Sélectionner une action :");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0 :
                        // Créer une galerie
                        Intent intent = new Intent(PostActivity.this, CreateGalleryActivity.class);
                        intent.putExtra("pId", pId);
                        startActivity(intent);
                        break;
                    case 1 :
                        // Modifier l'image
                        break;
                    default:
                }
            }
        });
        builder.create().show();
    }

    private void showGalleryMenu() {
        String[] options = {"Ajouter une image", "Modifier l'image", "Supprimer l'image"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Sélectionner une action :");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0 :
                        // Ajouter une image
                        Intent intent = new Intent(PostActivity.this, CreateGalleryActivity.class);
                        intent.putExtra("pId", pId);
                        startActivity(intent);
                        break;
                    case 1 :
                        // Details du commentaire
                        break;
                    case 2 :
                        // Envoyer un message
                        break;
                    default:
                }
            }
        });
        builder.create().show();
    }

    private void likePost() {
        if (mProcessLikes){
            ref.child(post.getpId()).child("pNLikes")
                    .setValue(""+ (Integer.parseInt(post.getpNLikes()) - 1));
            ref.child(post.getpId()).child("Likes").child(myUID).removeValue();
            mProcessLikes = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
        }else {
            ref.child(post.getpId()).child("pNLikes")
                    .setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("lId", myUID);
            hashMap.put("lDate", timestamp);

            hashMap.put("uName", myNAME);
            hashMap.put("uAvatar", myAVATAR);

            ref.child(post.getpId()).child("Likes").child(myUID).setValue(hashMap);
            mProcessLikes = true;
            likeIb.setImageResource(R.drawable.ic_like);
        }
    }

    private void favoritePost() {
        if (mProcessFavorites){
            ref.child(post.getpId()).child("Favorites").child(myUID).removeValue();
            mProcessFavorites = false;
            favoriteIb.setImageResource(R.drawable.ic_no_favorite);
        }else {
            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("fId", myUID);
            hashMap.put("fDate", timestamp);

            hashMap.put("uName", myNAME);
            hashMap.put("uAvatar", myAVATAR);

            ref.child(post.getpId()).child("Favorites").child(myUID).setValue(hashMap);
            mProcessFavorites = true;
            favoriteIb.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void sharePost(String pTitle, String pDescription) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable)coverIv.getDrawable();
        if (bitmapDrawable == null){
            shareTextOnly(pTitle, pDescription);
        }else {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            shareImageAndTextOnly(pTitle, pDescription, bitmap);
        }
    }

    private void shareImageAndTextOnly(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription + "\n" + getResources().getString(R.string.signature);
        Uri uri = saveImageToShare(bitmap);
        Intent sIntent =new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share Via"));
        sharedPost();
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(PostActivity.this, "cm.deone.corp.imopro.fileprovider", file);

        }catch (Exception e){
            Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n"+ pDescription + "\n" + getResources().getString(R.string.signature);
        Intent sIntent =new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sIntent, "Share Via"));
        sharedPost();
    }

    private void showGiveNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Noter ce post");
        final View customLayout = getLayoutInflater().inflate(R.layout.item_note, null);
        builder.setView(customLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText noteEdtv = customLayout.findViewById(R.id.noteEdtv);
                String userNote = noteEdtv.getText().toString().trim();

                if (TextUtils.isEmpty(userNote) || Integer.parseInt(userNote)<=0 || Integer.parseInt(userNote)>20){
                    Toast.makeText(PostActivity.this, "Votre note est incorrecte!", Toast.LENGTH_SHORT).show();
                    return;
                }

                setPostNote(""+userNote);
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void setPostNote(String note) {

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("nId", myUID);
        hashMap.put("nNote", note);
        hashMap.put("nDate", timestamp);

        hashMap.put("uName", myNAME);
        hashMap.put("uAvatar", myAVATAR);

        ref.child(pId).child("Notes").child(myUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PostActivity.this, "Note ajouté avec succès", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGiveWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
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
                    Toast.makeText(PostActivity.this, "Vous n'avez donné aucune raison!", Toast.LENGTH_SHORT).show();
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
            ref.child(post.getpId()).child("Signalements").child(myUID).removeValue();
            mProcessSignal = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
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

            ref.child(post.getpId()).child("Signalements").child(myUID).setValue(hashMap);
            mProcessSignal = true;
            likeIb.setImageResource(R.drawable.ic_like);
        }
    }

    private void verificationDeSaisie() {
        String message = commentEdtv.getText().toString().trim();
        if (TextUtils.isEmpty(message)){
            Toast.makeText(PostActivity.this, "Votre commentaire est vide!", Toast.LENGTH_SHORT).show();
            return;
        }
        prepareCommentData(""+message);
    }

    private void prepareCommentData(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, String> hashMapComment = new HashMap<>();
        hashMapComment.put("cCreator", myUID);
        hashMapComment.put("cMessage", message);
        hashMapComment.put("cId", timestamp);
        hashMapComment.put("cDate", timestamp);

        hashMapComment.put("uName", myNAME);
        hashMapComment.put("uAvatar", myAVATAR);

        uploadCommentData(
                hashMapComment,
                ""+timestamp);

    }

    private void uploadCommentData(HashMap<String, String> hashMapComment, String timestamp) {
        DatabaseReference refUpload = ref.child(pId).child("Comments");
        refUpload.child(timestamp).setValue(hashMapComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                resetVues();
                ref.child(pId).child("pNComments").setValue(""+ (Integer.parseInt(post.getpNComments()) + 1));
                String description = commentEdtv.getText().toString().trim();
                prepareNotification(
                        ""+timestamp,
                        ""+myNAME+" a ajouté un commentaire",
                        ""+  description,
                        ""+TYPE_POST_NOTIFICATION,
                        ""+TOPIC_GALLERY_NOTIFICATION+""+pId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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

    private void resetVues() {
        commentEdtv.setText(null);
        commentEdtv.setHint("Votre commentaire");
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
                        Toast.makeText(PostActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PostActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final ValueEventListener postInfosVal =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                collapsingToolbarLayout.setTitle(post.getpTitre());
                postDescriptionTv.setText(post.getpDescription());

                if (post.getpCreator().equals(myUID)){
                    vueTv.setText(Integer.parseInt(post.getpNVues()) <= 1 ? getResources()
                            .getString(R.string.nombre_vue, post.getpNVues()) : getResources()
                            .getString(R.string.nombre_vues, post.getpNVues()));
                    likeTv.setText(Integer.parseInt(post.getpNLikes()) <= 1 ? getResources()
                            .getString(R.string.nombre_like, post.getpNLikes()) : getResources()
                            .getString(R.string.nombre_likes, post.getpNLikes()));
                    commentTv.setText(Integer.parseInt(post.getpNComments()) <= 1 ? getResources()
                            .getString(R.string.nombre_comment, post.getpNComments()) : getResources()
                            .getString(R.string.nombre_comments, post.getpNComments()));
                    noteTv.setText(ds.child("pNote").exists()?getResources().getString(R.string.note_post, post.getpNote()):"Note de la publication");
                    warningTv.setText(ds.child("pNSignals").exists()?getResources().getString(R.string.signalement_post, post.getpNSignals()): "Total signalement");
                }else{
                    if (userVue){
                        ref.child(pId).child("pNVues").setValue(""+ (Integer.parseInt(post.getpNVues()) + 1));
                        ref.child(pId).child("Vues").child(myUID).setValue(ds.child("Vues").hasChild(myUID)?
                                ""+ (Integer.parseInt(ds.child("Vues").child(myUID).getValue(String.class)) + 1):"1");
                        userVue = false;
                    }
                    vueTv.setText(!ds.child("Vues").hasChild(myUID)?getResources().getString(R.string.nombre_vue, "0"):
                            Integer.parseInt(ds.child("Vues").child(myUID).getValue(String.class)) <= 1 ?
                                    getResources().getString(R.string.nombre_vue, ds.child("Vues").child(myUID).getValue(String.class)):
                                    getResources().getString(R.string.nombre_vues, ds.child("Vues").child(myUID).getValue(String.class)));

                    likeTv.setText(!ds.child("Shares").hasChild(myUID)?
                            getResources().getString(R.string.nombre_share, "0"):
                            Integer.parseInt(ds.child("Shares").child(myUID).getValue().toString())
                                    <= 1 ? getResources()
                                    .getString(R.string.nombre_share, ds.child("Shares").child(myUID).getValue().toString()) :
                                    getResources().getString(R.string.nombre_shares, ds.child("Shares").child(myUID).getValue().toString()));

                    shareIb.setImageResource(!ds.child("Shares").hasChild(myUID)?R.drawable.ic_no_share:R.drawable.ic_share);
                    ref.child(post.getpId()).child("Comments")
                            .orderByChild("cCreator").equalTo(myUID)
                            .addValueEventListener(myNumbCommentsVal);
                    noteIb.setImageResource(!ds.child("Notes").hasChild(myUID)?R.drawable.ic_no_note:R.drawable.ic_note);
                    noteTv.setText(!ds.child("Notes").hasChild(myUID)?getResources().getString(R.string.note_post, "0"):
                            getResources().getString(R.string.note_post, ds.child(myUID).child("nNote").getValue(String.class)));
                    signalerIb.setImageResource(ds.child("Signalements").hasChild(myUID)?R.drawable.ic_signaler:R.drawable.ic_no_signaler);
                    mProcessSignal = ds.child("Signalements").hasChild(myUID);
                    favoriteIb.setImageResource(ds.child("Favorites").hasChild(myUID)?R.drawable.ic_favorite:R.drawable.ic_no_favorite);
                    mProcessFavorites = ds.child("Favorites").hasChild(myUID);
                    likeIb.setImageResource(ds.child("Likes").hasChild(myUID)?R.drawable.ic_like:R.drawable.ic_no_like);
                    mProcessLikes = ds.child("Likes").hasChild(myUID);
                }
                try {
                    Picasso.get().load(post.getpCover()).placeholder(R.drawable.ic_post).into(coverIv);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_post).into(coverIv);
                }

                if (ds.child("Shares").hasChild(myUID))
                    numShared = ds.child("Shares").child(myUID).getValue(String.class);
                else
                    Log.e("TAG_SHARE", "Not shares found");

                ref.child(pId).child("Gallery").addValueEventListener(valPostGallery);
                ref.child(pId).child("Comments").addValueEventListener(valAllComments);
                ref.child(pId).child("Notes").addValueEventListener(valPostNotes);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valAllComments = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            commentList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                Comment comment = ds.getValue(Comment.class);
                commentList.add(comment);
                commentAdaptor = new CommentAdaptor(PostActivity.this, commentList);
                commentsRv.setAdapter(commentAdaptor);
                commentAdaptor.setOnItemClickListener(new ViewsClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Comment comment = commentList.get(position);
                        /*if (comment.getcCreator().equals(myUID) || pCreator.equals(myUID))
                            showCommentDialog(comment, blockedList.contains(comment.getcCreator()));*/
                    }
                });
            }
            rlComment.setVisibility(pCreator.equals(myUID)&&commentList.size()==0?View.GONE:View.VISIBLE);
            rlLike.setVisibility(pCreator.equals(myUID)&&commentList.size()==0?View.GONE:View.VISIBLE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private final ValueEventListener valPostGallery = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            galleryList.clear();
            galleryList.add(new Gallery(post.getpCover(), "Post cover"));
            for (DataSnapshot ds : snapshot.getChildren()){
                Gallery gallery = ds.getValue(Gallery.class);
                galleryList.add(gallery);
                galleryAdaptor = new GalleryAdaptor(PostActivity.this, galleryList);
                postImagesRv.setAdapter(galleryAdaptor);
                galleryAdaptor.setOnItemClickListener(new ViewsClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        if (pCreator.equals(myUID)){
                            showGalleryMenu();
                        }
                    }
                });
            }
            coverIv.setVisibility(galleryList.size()>1?View.GONE:View.VISIBLE);
            postImagesRv.setVisibility(galleryList.size()<=1?View.GONE:View.VISIBLE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valPostNotes = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            float note = 0;
            for (DataSnapshot ds : snapshot.getChildren()){
                String item = ds.child("nNote").getValue(String.class);
                note = note + Float.parseFloat(item);
            }
            note = note/snapshot.getChildrenCount();
            noteTv.setText(""+note+"/20");

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("pNote", ""+note);

            ref.child(pId).updateChildren(hashMap);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNumbCommentsVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            long count = snapshot.getChildrenCount();
            commentTv.setText(count <= 1 ? getResources()
                    .getString(R.string.nombre_comment, ""+count) : getResources()
                    .getString(R.string.nombre_comments, ""+count));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}