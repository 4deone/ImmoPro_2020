package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import cm.deone.corp.imopro.adapter.NotParentAdaptor;
import cm.deone.corp.imopro.fragments.CommentFragment;
import cm.deone.corp.imopro.fragments.GalleryFragment;
import cm.deone.corp.imopro.fragments.HomeFragment;
import cm.deone.corp.imopro.fragments.NotificationsFragment;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Gallery;
import cm.deone.corp.imopro.models.NotChildItem;
import cm.deone.corp.imopro.models.NotParentItem;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.DB_COMMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_FAVORIES;
import static cm.deone.corp.imopro.outils.Constant.DB_GALLERY;
import static cm.deone.corp.imopro.outils.Constant.DB_LIKES;
import static cm.deone.corp.imopro.outils.Constant.DB_NOTES;
import static cm.deone.corp.imopro.outils.Constant.DB_POST;
import static cm.deone.corp.imopro.outils.Constant.DB_SHARES;
import static cm.deone.corp.imopro.outils.Constant.DB_SIGNALEMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_VUES;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_GALLERY_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TOPIC_POST_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_GALLERY_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_POST_NOTIFICATION;

public class PostActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

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

    private ImageView coverIv;

    private RelativeLayout rlLike;
    private RelativeLayout rlComment;
    private EditText commentEdtv;

    private TextView likeTv;
    private TextView favoriteTv;
    private TextView shareTv;
    private TextView signalerTv;
    private TextView noteTv;

    private TextView postDescriptionTv;

    private RecyclerView postImagesRv;
    private List<Gallery> galleryList;
    private GalleryAdaptor galleryAdaptor;

    private RecyclerView commentsRv;
    private List<Comment> commentList;
    private CommentAdaptor commentAdaptor;

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
        if (v.getId() == R.id.likeTv && !pCreator.equals(myUID)){
            likePost();
        }else if (v.getId() == R.id.favoriteTv && !pCreator.equals(myUID)){
            favoritePost();
        }else if (v.getId() == R.id.shareTv){
            sharePost(post.getpTitre(), post.getpDescription());
        }else if (v.getId() == R.id.noteTv && !pCreator.equals(myUID)){
            showGiveNoteDialog();
        }else if (v.getId() == R.id.signalerTv && !pCreator.equals(myUID)){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_add_operation).setVisible(false);
        menu.findItem(R.id.menu_add_image).setVisible(false);
        MenuItem itemVues = menu.findItem(R.id.menu_show_vues);
        itemVues.setVisible(true);
        View rootViewVues = MenuItemCompat.getActionView(itemVues);
        final TextView tvVues = (TextView) rootViewVues.findViewById(R.id.menuItemVuesTv);
        MenuItem itemComments = menu.findItem(R.id.menu_show_comments);
        itemComments.setVisible(true);
        View rootViewComments = MenuItemCompat.getActionView(itemComments);
        final TextView tvComments = (TextView) rootViewComments.findViewById(R.id.menuItemCommentsTv);

        menuPost(tvComments, tvVues);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_show_settings){
            Intent intent = new Intent(PostActivity.this, PostSettingsActivity.class);
            intent.putExtra("pId", pId);
            intent.putExtra("pCreator", pCreator);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setTitle("");

        coverIv = findViewById(R.id.coverIv);

        signalerTv = findViewById(R.id.signalerTv);
        favoriteTv = findViewById(R.id.favoriteTv);
        shareTv = findViewById(R.id.shareTv);
        likeTv = findViewById(R.id.likeTv);
        noteTv = findViewById(R.id.noteTv);

        postDescriptionTv = findViewById(R.id.postDescriptionTv);

        commentEdtv = findViewById(R.id.commentEdtv);

        postImagesRv = findViewById(R.id.postImagesRv);
        commentsRv = findViewById(R.id.commentsRv);

        RelativeLayout rlCommentFooter = findViewById(R.id.commentFooterRl);
        rlComment = findViewById(R.id.rlComment);

        /*likeIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        favoriteIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        noteIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);
        signalerIb.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);*/

        rlCommentFooter.setVisibility(pCreator.equals(myUID)? View.GONE: View.VISIBLE);

        //warningTv.setVisibility(pCreator.equals(myUID)?View.GONE:View.VISIBLE);

        coverIv.setOnLongClickListener(this);
        findViewById(R.id.sendIb).setOnClickListener(this);
        likeTv.setOnClickListener(this);
        favoriteTv.setOnClickListener(this);
        noteTv.setOnClickListener(this);
        shareTv.setOnClickListener(this);
        signalerTv.setOnClickListener(this);
    }

    private void getPost() {
        galleryList = new ArrayList<>();
        commentList = new ArrayList<>();
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
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
            ref.child(post.getpId()).child("pNLikes").setValue(""+ (Integer.parseInt(post.getpNLikes()) - 1));
            ref.child(post.getpId()).child(DB_LIKES).child(myUID).removeValue();
            likeTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_no_like,0,0,0);
            mProcessLikes = false;
        }else {
            ref.child(post.getpId()).child("pNLikes").setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));
            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("lId", myUID);
            hashMap.put("lDate", timestamp);
            hashMap.put("uName", myNAME);
            hashMap.put("uAvatar", myAVATAR);

            ref.child(post.getpId()).child(DB_LIKES).child(myUID).setValue(hashMap);
            likeTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
            mProcessLikes = true;
        }
    }

    private void favoritePost() {
        if (mProcessFavorites){
            ref.child(post.getpId()).child("pNFavories").setValue(""+ (Integer.parseInt(post.getpNLikes()) - 1));
            ref.child(post.getpId()).child(DB_FAVORIES).child(myUID).removeValue();
            mProcessFavorites = false;
            favoriteTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_no_favorite,0,0,0);
        }else {
            ref.child(post.getpId()).child("pNFavories").setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));
            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("fId", myUID);
            hashMap.put("fDate", timestamp);
            hashMap.put("uName", myNAME);
            hashMap.put("uAvatar", myAVATAR);

            ref.child(post.getpId()).child(DB_FAVORIES).child(myUID).setValue(hashMap);
            mProcessFavorites = true;
            favoriteTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite,0,0,0);
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

    private void sharedPost() {
        ref.child(pId).child("pNShares").setValue(""+(Integer.parseInt(post.getpNLikes())+1));
        ref.child(pId).child(DB_SHARES).child(myUID).setValue(TextUtils.isEmpty(numShared)?"1":""+(Integer.parseInt(numShared)+1));
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

        ref.child(pId).child(DB_NOTES).child(myUID).setValue(hashMap)
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
            ref.child(post.getpId()).child(DB_SIGNALEMENT).child(myUID).removeValue();
            mProcessSignal = false;
            signalerTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_no_signaler,0,0,0);
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
            signalerTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_signaler,0,0,0);
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

    private void afficherPhotoCouverturePost(String postCover) {
        try {
            Picasso.get().load(post.getpCover()).placeholder(R.drawable.ic_post).into(coverIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_post).into(coverIv);
        }
    }

    private void initialiserNombreVuesPost(DataSnapshot ds) {
        if (userVue){
            ref.child(pId).child("pNVues").setValue(""+ (Integer.parseInt(post.getpNVues()) + 1));
            ref.child(pId).child(DB_VUES).child(myUID).setValue(ds.child(DB_VUES).hasChild(myUID)?
                    ""+ (Integer.parseInt(ds.child(DB_VUES).child(myUID).getValue(String.class)) + 1):"1");
            userVue = false;
        }
    }

    private void setHisNombreSharePost(DataSnapshot ds) {
        shareTv.setText(!ds.child(DB_SHARES).hasChild(myUID)?"0":ds.child(DB_SHARES).child(myUID).getValue().toString());
    }

    private void setHisNotePost(DataSnapshot ds) {
        noteTv.setText(!ds.child(DB_NOTES).hasChild(myUID)?"0":""+ds.child(DB_NOTES).child(myUID).child("nNote").getValue(String.class));
    }

    private void menuPost(final TextView tvComments, final TextView tvVues) {
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    tvComments.setText(ds.child("pNComments").getValue(String.class));
                    tvComments.setVisibility(ds.child("pNComments").getValue(String.class).equals("0")?View.GONE:View.VISIBLE);
                    if (ds.child("pCreator").getValue(String.class).equals(myUID)){
                        tvVues.setText(ds.child("pNVues").getValue(String.class));
                        tvVues.setVisibility(ds.child("pNVues").getValue(String.class).equals("0")?View.GONE:View.VISIBLE);
                    }else{
                        tvVues.setText(ds.child("Vues").hasChild(myUID)? ds.child("Vues").child(myUID).getValue(String.class): "0");
                        tvVues.setVisibility(ds.child("Vues").hasChild(myUID)?View.VISIBLE:View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ValueEventListener postInfosVal =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                postDescriptionTv.setText(post.getpDescription());
                likeTv.setText(""+post.getpNLikes());
                noteTv.setText(""+post.getpNote());
                signalerTv.setText(""+post.getpNSignals());
                shareTv.setText(""+post.getpNShares());
                favoriteTv.setText(""+post.getpNFavories());
                if (!post.getpCreator().equals(myUID)){
                    initialiserNombreVuesPost(ds);
                    setHisNombreSharePost(ds);
                    setHisNotePost(ds);
                    numShared = ds.child(DB_SHARES).hasChild(myUID)?ds.child(DB_SHARES).child(myUID).getValue(String.class):"";
                    shareTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            !ds.child(DB_SHARES).hasChild(myUID)?R.drawable.ic_no_share:R.drawable.ic_share,
                            0,
                            0,
                            0);
                    noteTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            !ds.child(DB_NOTES).hasChild(myUID)?R.drawable.ic_no_note:R.drawable.ic_note,
                            0,
                            0,
                            0);
                    signalerTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ds.child(DB_SIGNALEMENT).hasChild(myUID)?R.drawable.ic_signaler:R.drawable.ic_no_signaler,
                            0,
                            0,
                            0);
                    favoriteTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ds.child(DB_FAVORIES).hasChild(myUID)?R.drawable.ic_favorite:R.drawable.ic_no_favorite,
                            0,
                            0,
                            0);
                    likeTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ds.child(DB_LIKES).hasChild(myUID)?R.drawable.ic_like:R.drawable.ic_no_like,
                            0,
                            0,
                            0);
                    
                    if (ds.child(DB_SIGNALEMENT).hasChild(myUID))
                        mProcessSignal = true;
                    else
                        mProcessSignal = false;
                    if (ds.child(DB_FAVORIES).hasChild(myUID))
                        mProcessFavorites = true;
                    else
                        mProcessFavorites = false;
                    if (ds.child(DB_LIKES).hasChild(myUID))
                        mProcessLikes = true;
                    else
                        mProcessLikes = false;
                }
                afficherPhotoCouverturePost(""+post.getpCover());
                ref.child(pId).child(DB_GALLERY).addValueEventListener(valPostGallery);
                ref.child(pId).child(DB_COMMENT).addValueEventListener(valAllComments);
                ref.child(pId).child(DB_NOTES).addValueEventListener(valPostNotes);
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
            rlComment.setVisibility((pCreator.equals(myUID)&&commentList.size()==0)? View.GONE : View.VISIBLE);
            //rlLike.setVisibility((pCreator.equals(myUID)&&commentList.size()==0)? View.GONE : View.VISIBLE);
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
            coverIv.setVisibility(galleryList.size()>1? View.GONE: View.VISIBLE);
            postImagesRv.setVisibility(galleryList.size()<=1? View.GONE: View.VISIBLE);
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
            noteTv.setText(""+note);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("pNote", ""+note);

            ref.child(pId).updateChildren(hashMap);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}