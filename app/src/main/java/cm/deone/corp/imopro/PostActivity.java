package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import cm.deone.corp.imopro.adapter.SignalerAdaptor;
import cm.deone.corp.imopro.fragments.CommentFragment;
import cm.deone.corp.imopro.fragments.GalleryFragment;
import cm.deone.corp.imopro.fragments.HomeFragment;
import cm.deone.corp.imopro.fragments.NotificationsFragment;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Gallery;
import cm.deone.corp.imopro.models.NotChildItem;
import cm.deone.corp.imopro.models.NotParentItem;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.Signaler;
import cm.deone.corp.imopro.outils.AppLocationService;
import cm.deone.corp.imopro.outils.LocationAddress;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.DB_BLOCKED_USERS;
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

public class PostActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener  {

    private boolean userVue = true;
    private boolean mProcessLikes = false;
    private boolean mProcessFavorites = false;
    private boolean mProcessSignal = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor ;
    private Post post;
    private static final int REQUEST_CODE_PERMISSION = 500;
    private final String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private AppLocationService appLocationService;
    private static double latitude;
    private static double longitude;
    private boolean isGeolocalisationEnable = false;
    private static String pId;
    private String pCreator;
    private String myUID;
    private String myNAME;
    private String myAVATAR;
    private String numShared;

    private DatabaseReference ref;

    private ImageView coverIv;

    private EditText commentEdtv;

    private RelativeLayout rlNewComment;
    private RelativeLayout rlCommentaires;
    private SwitchCompat swtvGeolocalisation;
    private TextView likeTv;
    private TextView favoriteTv;
    private TextView shareTv;
    private TextView noteTv;
    private TextView postDescriptionTv;
    private TextView tvPostTitle;
    private TextView tvDeletePost;

    private RecyclerView postImagesRv;
    private List<Gallery> galleryList;
    private GalleryAdaptor galleryAdaptor;

    private SignalerAdaptor signalerAdaptor;
    private List<Signaler> signalerList;

    private RecyclerView rvComments;
    private List<Comment> commentList;
    private CommentAdaptor commentAdaptor;

    private List<String> blockedList;

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
        }else if (v.getId() == R.id.tvSignalerPost && !pCreator.equals(myUID)){
            showGiveWarningDialog();
        }else if (v.getId() == R.id.tvDeletePost){
            deleteConfirmation();
        }else if (v.getId() == R.id.tvSgnalerActivity){
            showActivitiesDialog("signalers");
        }else if (v.getId() == R.id.tvCommentsctivity){
            showActivitiesDialog("comments");
        }else if (v.getId() == R.id.tvNotesctivity){
            showNotesDialog();
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
            case R.id.swtvGeolocalisation :
                if (isChecked){
                    if (!isGeolocalisationEnable)
                        showLocaliserDialog();
                }
                else{
                    if (isGeolocalisationEnable)
                        showUnLocaliserDialog();
                }
                break;
            default:
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_add_operation).setVisible(false);
        menu.findItem(R.id.menu_add_image).setVisible(false);
        menu.findItem(R.id.menu_show_settings).setVisible(false);
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
        if (item.getItemId() == R.id.menu_show_comments){

        }else if (item.getItemId() == R.id.menu_show_vues){

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
        sharedPreferences = getSharedPreferences("POST_NOTIF_SP", MODE_PRIVATE);
        appLocationService = new AppLocationService(PostActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setTitle("");

        coverIv = findViewById(R.id.coverIv);

        tvDeletePost = findViewById(R.id.tvDeletePost);

        favoriteTv = findViewById(R.id.favoriteTv);
        shareTv = findViewById(R.id.shareTv);
        likeTv = findViewById(R.id.likeTv);
        noteTv = findViewById(R.id.noteTv);

        tvPostTitle = findViewById(R.id.tvPostTitle);
        postDescriptionTv = findViewById(R.id.postDescriptionTv);

        postImagesRv = findViewById(R.id.postImagesRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.HORIZONTAL, false);
        postImagesRv.setLayoutManager(layoutManager);

        RelativeLayout rlDelete = findViewById(R.id.rlDelete);
        rlDelete.setVisibility(pCreator.equals(myUID)? View.VISIBLE: View.GONE);
        TextView tvDeletePost = findViewById(R.id.tvDeletePost);
        RelativeLayout rlSignaler = findViewById(R.id.rlSignaler);
        rlSignaler.setVisibility(pCreator.equals(myUID)? View.GONE: View.VISIBLE);
        TextView tvSignalerPost = findViewById(R.id.tvSignalerPost);

        TextView tvSgnalerActivity = findViewById(R.id.tvSgnalerActivity);
        TextView tvNotesctivity = findViewById(R.id.tvNotesctivity);
        TextView tvCommentsctivity = findViewById(R.id.tvCommentsctivity);

        swtvGeolocalisation = findViewById(R.id.swtvGeolocalisation);
        swtvGeolocalisation.setEnabled(pCreator.equals(myUID));
        SwitchCompat galleryNotificationSw = findViewById(R.id.galleryNotificationSw);
        galleryNotificationSw.setVisibility(pCreator.equals(myUID)? View.GONE: View.VISIBLE);
        SwitchCompat commentNotificationSw = findViewById(R.id.commentNotificationSw);

        boolean isCommentEnable = sharedPreferences.getBoolean(""+TOPIC_COMMENT_NOTIFICATION+""+pId, false);
        boolean isGalleryEnable = sharedPreferences.getBoolean(""+TOPIC_GALLERY_NOTIFICATION+""+pId, false);

        commentNotificationSw.setChecked(isCommentEnable);
        galleryNotificationSw.setChecked(isGalleryEnable);

        coverIv.setOnLongClickListener(this);
        likeTv.setOnClickListener(this);
        favoriteTv.setOnClickListener(this);
        noteTv.setOnClickListener(this);
        shareTv.setOnClickListener(this);

        tvSgnalerActivity.setOnClickListener(this);
        tvNotesctivity.setOnClickListener(this);
        tvCommentsctivity.setOnClickListener(this);

        tvSignalerPost.setOnClickListener(this);
        tvDeletePost.setOnClickListener(this);

        commentNotificationSw.setOnCheckedChangeListener(this);
        galleryNotificationSw.setOnCheckedChangeListener(this);
        swtvGeolocalisation.setOnCheckedChangeListener(this);

    }

    private void getPost() {
        galleryList = new ArrayList<>();
        blockedList = new ArrayList<>();
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
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

    private void confirmationRequise(String action, String identifiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Etes-vous sur de vouloir effectuer cette opération ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (action.equals("Bloquer")){
                            bloquerLutilisateur(""+identifiant);
                        }else if (action.equals("Débloquer")){
                            debloquerLutilisateur(""+identifiant);
                        }else if (action.equals("Supprimer")){
                            // Delete comment
                            // Reduidre la valeur du nombre de commenataire du post
                            supprimerLeCommentaire(""+identifiant);
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

    private void bloquerLutilisateur(String cCreator) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bId", cCreator);
        hashMap.put("bDate", timestamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(pId).child("BlockedUsers").child(cCreator).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PostActivity.this, "Utilisateur bloqué", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void debloquerLutilisateur(String cCreator) {
        ref.child(pId).child("BlockedUsers").orderByChild("bId").equalTo(cCreator)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(PostActivity.this, "Utilisateur débloqué", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void supprimerLeCommentaire(String cId) {
        ref.child(pId).child("Comments").child(cId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PostActivity.this, "Commentaire supprimé", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        ref.child(pId).addListenerForSingleValueEvent(valUpdateCommentNumber);
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

    private void getPostAddress() {
        if (!checkLocationPermission()){
            requestLocationPermission();
        }else {
            Location location = appLocationService.getLocation();

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                //LocationAddress locationAddress = new LocationAddress();
                LocationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
            } else {
                swtvGeolocalisation.setText("Recherche de localisation...");
                showSettingsAlert();
            }
        }
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(PostActivity.this, mPermission) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{mPermission}, REQUEST_CODE_PERMISSION);
    }

    private void deleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
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

    private void showActivitiesDialog(String activity) {
        Dialog dialog = new Dialog(PostActivity.this);
        dialog.setContentView(R.layout.dialog_signaler);
        RecyclerView recyclerView = dialog.findViewById(R.id.rvSignalers);
        EditText edtvSearch = dialog.findViewById(R.id.edtvSearch);
        RelativeLayout rlComment = dialog.findViewById(R.id.rlComment);
        rlComment.setVisibility(activity.equals("comments") && !pCreator.equals(myUID) ? View.VISIBLE: View.GONE);
        if (activity.equals("comments")){
            ImageButton sendIb = dialog.findViewById(R.id.sendIb);
            sendIb.setOnClickListener(this);
            commentEdtv = dialog.findViewById(R.id.commentEdtv);
            ImageView userIv = dialog.findViewById(R.id.userIv);
            try {
                Picasso.get().load(myAVATAR).placeholder(R.drawable.ic_user).into(userIv);
            } catch (Exception e) {
                Picasso.get().load(R.drawable.ic_user).into(userIv);
            }
            commentList = new ArrayList<>();
            ref.child(pId).child(DB_COMMENT).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    commentList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Comment comment = ds.getValue(Comment.class);
                        commentList.add(comment);
                        commentAdaptor = new CommentAdaptor(PostActivity.this, commentList, pId);
                        recyclerView.setAdapter(commentAdaptor);
                        commentAdaptor.setOnItemClickListener(new ViewsClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Comment comment = commentList.get(position);
                                if (comment.getcCreator().equals(myUID) || pCreator.equals(myUID))
                                    showCommentDialog(comment, blockedList.contains(comment.getcCreator()));
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else if (activity.equals("signalers")){
            signalerList = new ArrayList<>();
            ref.child(pId).child(DB_SIGNALEMENT).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    signalerList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Signaler signaler = ds.getValue(Signaler.class);
                        signalerList.add(signaler);
                        signalerAdaptor = new SignalerAdaptor(PostActivity.this, signalerList);
                        recyclerView.setAdapter(signalerAdaptor);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        dialog.show();
    }

    private void showNotesDialog() {

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

    private void showCommentDialog(Comment comment, boolean isblocked) {
        String[] optionsUsers = {"Supprimer le commentaire", "Details du commentaire", "Envoyer un message", "Pofile de l'utilisateur"};
        String[] optionsCreator = {isblocked ? "Débloquer l'utilisateur" : "Bloquer l'utilisateur",
                "Details du commentaire", "Envoyer un message"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Sélectionner une action :");
        builder.setItems(pCreator.equals(myUID) ? optionsCreator : optionsUsers, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0 :
                        if (comment.getcCreator().equals(myUID))
                            confirmationRequise("Supprimer", ""+comment.getcId());
                        else if (pCreator.equals(myUID))
                            confirmationRequise(isblocked ? "Débloquer" : "Bloquer", ""+comment.getcCreator());
                        break;
                    case 1 :
                        // Details du commentaire
                        break;
                    case 2 :
                        // Envoyer un message
                        break;
                    case 3 :
                        // Pofile de l'utilisateur
                        break;
                    default:
                }
            }
        });
        builder.create().show();
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PostActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        PostActivity.this.startActivity(intent);
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

    private void showLocaliserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Localiser ce post");
        builder.setMessage("Etes-vous sur de vouloir localiser ce post ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getPostAddress();
                    }
                }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                swtvGeolocalisation.setChecked(false);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showUnLocaliserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Localiser ce post");
        builder.setMessage("Etes-vous sur de vouloir supprimer la localisation ce post ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        swtvGeolocalisation.setText("Pas de localisation");
                        ref.child(pId).child("Geolocalisation").removeValue();
                    }
                }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                swtvGeolocalisation.setChecked(true);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private final ValueEventListener valUpdateCommentNumber = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String comments = "" + snapshot.child("pNComments").getValue();
            int newCommentVal = Integer.parseInt(comments) - 1;
            ref.child(pId).child("pNComments").setValue(""+newCommentVal);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener postInfosVal =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                tvPostTitle.setText(post.getpTitre());
                postDescriptionTv.setText(post.getpDescription());
                likeTv.setText(""+post.getpNLikes());
                noteTv.setText(""+post.getpNote()+"/20");
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
                    /*signalerTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ds.child(DB_SIGNALEMENT).hasChild(myUID)?R.drawable.ic_signaler:R.drawable.ic_no_signaler,
                            0,
                            0,
                            0);*/
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

                    mProcessFavorites = ds.child(DB_FAVORIES).hasChild(myUID);
                    mProcessLikes = ds.child(DB_LIKES).hasChild(myUID);

                    /*if (ds.child(DB_FAVORIES).hasChild(myUID))
                        mProcessFavorites = true;
                    else
                        mProcessFavorites = false;
                    if (ds.child(DB_LIKES).hasChild(myUID))
                        mProcessLikes = true;
                    else
                        mProcessLikes = false;*/
                    mProcessSignal = ds.child(DB_SIGNALEMENT).hasChild(myUID);

                    String adresse = ds.child("Geolocalisation").child("adresse").getValue(String.class);
                    isGeolocalisationEnable = ds.child("Geolocalisation").hasChild("adresse");
                    swtvGeolocalisation.setChecked(isGeolocalisationEnable);
                    swtvGeolocalisation.setText(isGeolocalisationEnable && ds.child("Geolocalisation").hasChild("adresse") ? adresse:"Pas de localisation");

                }
                afficherPhotoCouverturePost(""+post.getpCover());
                ref.child(pId).child(DB_GALLERY).addValueEventListener(valPostGallery);
                ref.child(pId).child(DB_NOTES).addValueEventListener(valPostNotes);
                ref.child(pId).child(DB_BLOCKED_USERS).addValueEventListener(valPostBlokedUsers);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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

    private final ValueEventListener valPostBlokedUsers = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            blockedList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                String item = ds.child("bId").getValue(String.class);
                blockedList.add(item);
            }
            if (blockedList.contains(myUID))
                rlNewComment.setVisibility(View.GONE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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
            //swtvGeolocalisation.setText(locationAddress);
            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", timestamp);
            hashMap.put("latitude", ""+latitude);
            hashMap.put("longitude", ""+longitude);
            hashMap.put("adresse", locationAddress);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
            reference.child(pId).child("Geolocalisation").setValue(hashMap);
        }
    }

}