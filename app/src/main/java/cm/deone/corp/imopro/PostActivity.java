package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.deone.corp.imopro.adapter.GalleryAdaptor;
import cm.deone.corp.imopro.fragments.CommentFragment;
import cm.deone.corp.imopro.fragments.GalleryFragment;
import cm.deone.corp.imopro.fragments.HomeFragment;
import cm.deone.corp.imopro.fragments.NotificationsFragment;
import cm.deone.corp.imopro.models.Gallery;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.DB_COMMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_GALLERY;
import static cm.deone.corp.imopro.outils.Constant.DB_POST;
import static cm.deone.corp.imopro.outils.Constant.TYPE_COMMENT_NOTIFICATION;
import static cm.deone.corp.imopro.outils.Constant.TYPE_GALLERY_NOTIFICATION;

public class PostActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference ref;
    private boolean userVue = true;
    private String pId;
    private String pCreator;
    private String myUID;
    private String myVUES;
    private String numShared;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private boolean mProcessLikes = false;
    private boolean mProcessFavorites = false;
    private boolean mProcessSignal = false;

    private Post post;

    private ImageView postCoverRv;

    private ImageButton likeIb;
    private ImageButton favoriteIb;
    private ImageButton noteIb;
    private ImageButton shareIb;
    private ImageButton signalerIb;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        checkUser();
        initVues();
        getMyVue();
        getPost();
        getGallery();
        setNumShared();
        setLiked();
        setNote();
        setSignaled();
        setFavorite();
    }

    @Override
    protected void onStart() {
        checkUser();
        getMyVue();
        getPost();
        getGallery();
        setNumShared();
        setLiked();
        setNote();
        setSignaled();
        setFavorite();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUser();
        getMyVue();
        getPost();
        getGallery();
        setNumShared();
        setLiked();
        setNote();
        setSignaled();
        setFavorite();
        super.onResume();
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null){
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }else{
            myUID = fUser.getUid();
        }
    }

    private void initVues() {
        pId = getIntent().getStringExtra("pId");
        pCreator = getIntent().getStringExtra("pCreator");
        ref = FirebaseDatabase.getInstance().getReference("Posts");

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);

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

        findViewById(R.id.addCommentIb).setOnClickListener(this);

        likeIb.setOnClickListener(this);
        favoriteIb.setOnClickListener(this);
        noteIb.setOnClickListener(this);
        shareIb.setOnClickListener(this);
        signalerIb.setOnClickListener(this);

    }

    private void getPost() {
        galleryList = new ArrayList<>();
        Query query = ref.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
    }

    private void sharedPost() {
        ref.child(post.getpId()).child("pNShares").setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));
        if (TextUtils.isEmpty(numShared)){
            ref.child(post.getpId()).child("Shares").child(myUID).setValue("1");
        }else{
            ref.child(post.getpId()).child("Shares").child(myUID).setValue(""+ (Integer.parseInt(numShared) + 1));
        }
    }

    private void getMyVue() {
        if (!pCreator.equals(myUID)){
            Query query = ref.child(pId).child("Vues").orderByKey().equalTo(myUID);
            query.addValueEventListener(valMyVues);
        }
    }

    private void getGallery() {
        ref.child(pId).child("Gallery").addValueEventListener(valPostGallery);
    }

    private void setNote() {
        ref.child(pId).child("Notes").addValueEventListener(valPostNotes);
    }

    private void setLiked() {
        ref.child(pId).child("Likes").addValueEventListener(valeSetLiked);
    }

    private void setFavorite() {
        Query query = ref.child(pId).child("Favorites");
        query.addValueEventListener(valPostFavorite);
    }

    private void setSignaled() {
        ref.child(pId).child("Signalements").orderByChild("sId")
                .equalTo(myUID).addValueEventListener(valSetSignaled);
    }

    private void setNumShared() {
        ref.child(pId).child("Shares").child(myUID)
                .addValueEventListener(valSharedPost);
    }

    private final ValueEventListener valSharedPost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                numShared = ds.getValue().toString();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valeSetLiked = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(myUID)){
                likeIb.setImageResource(R.drawable.ic_like);
                mProcessLikes = true;
            }else {
                likeIb.setImageResource(R.drawable.ic_no_like);
                mProcessLikes = false;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(PostActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valPostFavorite = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChild(myUID)){
                favoriteIb.setImageResource(R.drawable.ic_favorite);
                mProcessFavorites = true;
            }else {
                favoriteIb.setImageResource(R.drawable.ic_no_favorite);
                mProcessFavorites = false;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valSetSignaled = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()){
                signalerIb.setImageResource(R.drawable.ic_signaler);
                mProcessSignal = true;
            }else {
                signalerIb.setImageResource(R.drawable.ic_no_signaler);
                mProcessSignal = false;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(PostActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

    private final ValueEventListener valMyVues = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                myVUES = ds.getValue().toString();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener postInfosVal =  new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            galleryList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                collapsingToolbarLayout.setTitle(post.getpTitre());
                postDescriptionTv.setText(post.getpDescription());
                galleryList.add(new Gallery(post.getpCover(), "Post cover"));
                if (post.getpCreator().equals(myUID)){
                    likeIb.setVisibility(View.GONE);
                    favoriteIb.setVisibility(View.GONE);
                    noteIb.setVisibility(View.GONE);
                    signalerIb.setVisibility(View.GONE);

                    warningTv.setVisibility(View.VISIBLE);

                    vueTv.setText(Integer.parseInt(post.getpNVues()) <= 1 ? getResources()
                            .getString(R.string.nombre_vue, post.getpNVues()) : getResources()
                            .getString(R.string.nombre_vues, post.getpNVues()));
                    likeTv.setText(Integer.parseInt(post.getpNLikes()) <= 1 ? getResources()
                            .getString(R.string.nombre_like, post.getpNLikes()) : getResources()
                            .getString(R.string.nombre_likes, post.getpNLikes()));
                    commentTv.setText(Integer.parseInt(post.getpNComments()) <= 1 ? getResources()
                            .getString(R.string.nombre_comment, post.getpNComments()) : getResources()
                            .getString(R.string.nombre_comments, post.getpNComments()));

                    if (ds.child("pNote").exists())
                        noteTv.setText(getResources().getString(R.string.note_post, post.getpNote()));
                    else
                        noteTv.setText("Note de la publication");

                    if (ds.child("pNSignals").exists())
                        warningTv.setText(getResources().getString(R.string.signalement_post, post.getpNSignals()));
                    else
                        warningTv.setText("Total signalement");

                }else{
                    likeIb.setVisibility(View.VISIBLE);
                    favoriteIb.setVisibility(View.VISIBLE);
                    noteIb.setVisibility(View.VISIBLE);
                    signalerIb.setVisibility(View.VISIBLE);
                    if (userVue){
                        ref.child(pId).child("pNVues").setValue(""+ (Integer.parseInt(post.getpNVues()) + 1));
                        if (TextUtils.isEmpty(myVUES))
                            ref.child(pId).child("Vues").child(myUID).setValue("1");
                        else
                            ref.child(pId).child("Vues").child(myUID).setValue(""+ (Integer.parseInt(myVUES) + 1));
                        userVue = false;
                    }
                    ref.child(post.getpId()).child("Vues").child(myUID)
                            .addValueEventListener(myNumbVuesVal);
                    ref.child(post.getpId()).child("Comments")
                            .orderByChild("cCreator").equalTo(myUID)
                            .addValueEventListener(myNumbCommentsVal);
                    ref.child(post.getpId()).child("Shares")
                            .addValueEventListener(myNumbSharesVal);
                    ref.child(post.getpId()).child("Notes").orderByChild("nId").equalTo(myUID)
                            .addValueEventListener(myNoteVal);

                    warningTv.setVisibility(View.GONE);
                }
            }
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

    private final ValueEventListener myNumbSharesVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.hasChild(myUID)){
                likeTv.setText(getResources().getString(R.string.nombre_share, "0"));
                shareIb.setImageResource(R.drawable.ic_no_share);
            }else{
                likeTv.setText(Integer.parseInt(snapshot.child(myUID).getValue().toString()) <= 1 ? getResources()
                        .getString(R.string.nombre_share, snapshot.child(myUID).getValue().toString()) : getResources()
                        .getString(R.string.nombre_shares, snapshot.child(myUID).getValue().toString()));
                shareIb.setImageResource(R.drawable.ic_share);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNoteVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()){
                noteIb.setImageResource(R.drawable.ic_no_note);
                noteTv.setText(getResources().getString(R.string.note_post, "0"));
            } else{
                String item = snapshot.child(myUID).child("nNote").getValue(String.class);
                noteIb.setImageResource(R.drawable.ic_note);
                noteTv.setText(getResources().getString(R.string.note_post, item));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNumbVuesVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (TextUtils.isEmpty(snapshot.getValue().toString()))
                vueTv.setText(getResources().getString(R.string.nombre_vue, "0"));
            else
                vueTv.setText(Integer.parseInt(snapshot.getValue().toString()) <= 1 ? getResources()
                        .getString(R.string.nombre_vue, snapshot.getValue().toString()) : getResources()
                        .getString(R.string.nombre_vues, snapshot.getValue().toString()));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valPostGallery = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                        postCoverRv = view.findViewById(R.id.itemGalleryIv);
                        if (pCreator.equals(myUID)){
                            showGalleryMenu();
                        }
                    }
                });
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

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

            // unsuscribe gallery & comment notification
            FirebaseMessaging.getInstance().unsubscribeFromTopic(post.getpTopicGallery());
            FirebaseMessaging.getInstance().unsubscribeFromTopic(post.getpTopicComment());
        }else {
            ref.child(post.getpId()).child("pNLikes")
                    .setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("lId", myUID);
            hashMap.put("lDate", timestamp);

            ref.child(post.getpId()).child("Likes").child(myUID).setValue(hashMap);
            mProcessLikes = true;
            likeIb.setImageResource(R.drawable.ic_like);

            // suscribe gallery & comment notification
            FirebaseMessaging.getInstance().subscribeToTopic(post.getpTopicGallery());
            FirebaseMessaging.getInstance().subscribeToTopic(post.getpTopicComment());
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

            ref.child(post.getpId()).child("Favorites").child(myUID).setValue(hashMap);
            mProcessFavorites = true;
            favoriteIb.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void sharePost(String pTitle, String pDescription) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable)postCoverRv.getDrawable();
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

            ref.child(post.getpId()).child("Signalements").child(myUID).setValue(hashMap);
            mProcessSignal = true;
            likeIb.setImageResource(R.drawable.ic_like);
        }
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
        }else if (v.getId() == R.id.addCommentIb && !pCreator.equals(myUID)){

        }

    }
}