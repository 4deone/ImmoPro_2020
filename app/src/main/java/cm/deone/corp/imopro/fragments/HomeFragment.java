package cm.deone.corp.imopro.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;

import cm.deone.corp.imopro.MainActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.Post;

import static cm.deone.corp.imopro.outils.Constant.TAG_POST_CREATOR;
import static cm.deone.corp.imopro.outils.Constant.TAG_POST_ID;

public class HomeFragment extends Fragment  implements View.OnClickListener{

    private DatabaseReference reference;

    private String pId;
    private String pCreator;
    private String myUID;
    private String numShared;
    private Post post;

    private boolean mProcessLikes = false;
    private boolean mProcessFavorites = false;
    private boolean mProcessSignal = false;

    private ImageButton likeIb;
    private ImageButton favoriteIb;
    private ImageButton shareIb;
    private ImageButton noteIb;
    private ImageButton signalerIb;

    private TextView vueTv;
    private TextView likeTv;
    private TextView commentTv;
    private TextView noteTv;
    private TextView warningTv;


    private ImageView postCoverRv;
    private TextView postTitreTv;
    private TextView postDescriptionETv;

    private View view;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            pId = getArguments().getString(TAG_POST_ID);
            pCreator = getArguments().getString(TAG_POST_CREATOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        checkUser();
        initVues();
        getPostInformations();
        setNumShared();
        setLiked();
        setNote();
        setSignaled();
        setFavorite();
        return view;

    }

    @Override
    public void onStart() {
        checkUser();
        getPostInformations();
        setNumShared();
        setLiked();
        setNote();
        setSignaled();
        setFavorite();
        super.onStart();
    }

    @Override
    public void onResume() {
        checkUser();
        getPostInformations();
        setNumShared();
        setLiked();
        setNote();
        setSignaled();
        setFavorite();
        super.onResume();
    }

    @Override
    public void onStop() {
        reference.removeEventListener(valPostFavorite);
        reference.removeEventListener(valPostNotes);
        reference.removeEventListener(postInfosVal);
        reference.removeEventListener(valSetSignaled);
        reference.removeEventListener(valeSetLiked);
        reference.removeEventListener(valSharedPost);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        reference.removeEventListener(valPostFavorite);
        reference.removeEventListener(valPostNotes);
        reference.removeEventListener(postInfosVal);
        reference.removeEventListener(valSetSignaled);
        reference.removeEventListener(valeSetLiked);
        reference.removeEventListener(valSharedPost);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_add_operation).setVisible(false);
        menu.findItem(R.id.menu_show_settings).setVisible(false);
        if (pCreator.equals(myUID)){
            menu.findItem(R.id.menu_add_image).setVisible(true);
        }else{
            menu.findItem(R.id.menu_add_image).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_image) {
            /*Intent intent = new Intent(getActivity(), VoirArticleActivity.class);
            intent.putExtra("sId", sId);
            startActivity(intent);*/
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null){
            myUID = fUser.getUid();
        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    private void initVues() {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Infos du post");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        reference = FirebaseDatabase.getInstance().getReference();

        postCoverRv = view.findViewById(R.id.postCoverRv);
        postDescriptionETv = view.findViewById(R.id.postDescriptionETv);
        postTitreTv = view.findViewById(R.id.postTitreTv);

        vueTv = view.findViewById(R.id.vueTv);
        likeTv = view.findViewById(R.id.likeTv);
        commentTv = view.findViewById(R.id.commentTv);
        noteTv = view.findViewById(R.id.noteTv);
        warningTv = view.findViewById(R.id.warningTv);

        noteIb = view.findViewById(R.id.noteIb);
        signalerIb = view.findViewById(R.id.signalerIb);
        likeIb = view.findViewById(R.id.likeIb);
        favoriteIb = view.findViewById(R.id.favoriteIb);
        shareIb = view.findViewById(R.id.shareIb);

        likeIb.setOnClickListener(this);
        favoriteIb.setOnClickListener(this);
        shareIb.setOnClickListener(this);
        noteIb.setOnClickListener(this);
        signalerIb.setOnClickListener(this);
    }

    private void favoritePost() {
        if (mProcessFavorites){
            reference.child("Posts").child(post.getpId()).child("Favorites").child(myUID).removeValue();
            mProcessFavorites = false;
            favoriteIb.setImageResource(R.drawable.ic_no_favorite);
        }else {
            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("fId", myUID);
            hashMap.put("fDate", timestamp);

            reference.child("Posts").child(post.getpId()).child("Favorites").child(myUID).setValue(hashMap);
            mProcessFavorites = true;
            favoriteIb.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void setNote() {
        reference.child("Posts").child(pId).child("Notes").addValueEventListener(valPostNotes);
    }

    private void setFavorite() {
        Query query = reference.child("Posts").child(pId).child("Favorites");
        query.addValueEventListener(valPostFavorite);
    }

    private void setSignaled() {
        reference.child("Posts").child(pId).child("Signalements").orderByChild("sId").equalTo(myUID).addValueEventListener(valSetSignaled);
    }

    private void setLiked() {
        reference.child("Posts").child(pId).child("Likes").addValueEventListener(valeSetLiked);
    }

    private void likePost() {
        if (mProcessLikes){
            reference.child("Posts").child(post.getpId()).child("pNLikes")
                    .setValue(""+ (Integer.parseInt(post.getpNLikes()) - 1));
            reference.child("Posts").child(post.getpId()).child("Likes").child(myUID).removeValue();
            mProcessLikes = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
        }else {
            reference.child("Posts").child(post.getpId()).child("pNLikes")
                    .setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("lId", myUID);
            hashMap.put("lDate", timestamp);

            reference.child("Posts").child(post.getpId()).child("Likes").child(myUID).setValue(hashMap);
            mProcessLikes = true;
            likeIb.setImageResource(R.drawable.ic_like);
        }
    }

    private void getPostInformations() {
        Query query = reference.child("Posts").orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
    }

    private void showGiveNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    Toast.makeText(getActivity(), "Votre note est incorrecte!", Toast.LENGTH_SHORT).show();
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

        reference.child("Posts").child(pId).child("Notes").child(myUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "Note ajouté avec succès", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGiveWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    Toast.makeText(getActivity(), "Vous n'avez donné aucune raison!", Toast.LENGTH_SHORT).show();
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
            reference.child("Posts").child(post.getpId()).child("pNSignals")
                    .setValue(""+ (Integer.parseInt(post.getpNSignals()) - 1));
            reference.child("Posts").child(post.getpId()).child("Signalements").child(myUID).removeValue();
            mProcessSignal = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
        }else {
            reference.child("Posts").child(post.getpId()).child("pNSignals")
                    .setValue(""+ (Integer.parseInt(post.getpNSignals()) + 1));

            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("sId", myUID);
            hashMap.put("sMessage", signaler);
            hashMap.put("sDate", timestamp);

            reference.child("Posts").child(post.getpId()).child("Signalements").child(myUID).setValue(hashMap);
            mProcessSignal = true;
            likeIb.setImageResource(R.drawable.ic_like);
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
        String shareBody = pTitle + "\n" + pDescription + "\n" + getActivity().getResources().getString(R.string.signature);
        Uri uri = saveImageToShare(bitmap);
        Intent sIntent =new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        getActivity().startActivity(Intent.createChooser(sIntent, "Share Via"));
        sharedPost();
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getActivity().getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(getActivity(), "cm.deone.corp.imopro.fileprovider", file);

        }catch (Exception e){
            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n"+ pDescription + "\n" + getActivity().getResources().getString(R.string.signature);
        Intent sIntent =new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        getActivity().startActivity(Intent.createChooser(sIntent, "Share Via"));
        sharedPost();
    }

    private void sharedPost() {
        reference.child("Posts").child(post.getpId()).child("pNShares").setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));
        if (TextUtils.isEmpty(numShared)){
            reference.child("Posts").child(post.getpId()).child("Shares").child(myUID).setValue("1");
        }else{
            reference.child("Posts").child(post.getpId()).child("Shares").child(myUID).setValue(""+ (Integer.parseInt(numShared) + 1));
        }
    }

    private void setNumShared() {
        reference.child("Posts").child(pId).child("Shares").child(myUID).addValueEventListener(valSharedPost);
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
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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

            reference.child("Posts").child(pId).updateChildren(hashMap);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener postInfosVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                if (post.getpCreator().equals(myUID)){

                    warningTv.setVisibility(View.VISIBLE);

                    vueTv.setText(Integer.parseInt(post.getpNVues()) <= 1 ? view.getResources()
                            .getString(R.string.nombre_vue, post.getpNVues()) : view.getResources()
                            .getString(R.string.nombre_vues, post.getpNVues()));
                    likeTv.setText(Integer.parseInt(post.getpNLikes()) <= 1 ? view.getResources()
                            .getString(R.string.nombre_like, post.getpNLikes()) : view.getResources()
                            .getString(R.string.nombre_likes, post.getpNLikes()));
                    commentTv.setText(Integer.parseInt(post.getpNComments()) <= 1 ? view.getResources()
                            .getString(R.string.nombre_comment, post.getpNComments()) : view.getResources()
                            .getString(R.string.nombre_comments, post.getpNComments()));

                    if (ds.child("pNote").exists())
                        noteTv.setText(view.getResources().getString(R.string.note_post, post.getpNote()));
                    else
                        noteTv.setText("0/20");

                    if (ds.child("pNSignals").exists())
                        warningTv.setText(view.getResources().getString(R.string.signalement_post, post.getpNSignals()));
                    else
                        warningTv.setText("0 Warning");

                    likeIb.setVisibility(View.GONE);
                    favoriteIb.setVisibility(View.GONE);
                    noteIb.setVisibility(View.GONE);
                    signalerIb.setVisibility(View.GONE);

                }else{

                    reference.child("Posts").child(post.getpId()).child("Vues").child(myUID)
                            .addValueEventListener(myNumbVuesVal);
                    reference.child("Posts").child(post.getpId()).child("Comments")
                            .orderByChild("cCreator").equalTo(myUID)
                            .addValueEventListener(myNumbCommentsVal);
                    reference.child("Posts").child(post.getpId()).child("Shares")
                            .addValueEventListener(myNumbSharesVal);
                    reference.child("Posts").child(post.getpId()).child("Notes").orderByChild("nId").equalTo(myUID)
                            .addValueEventListener(myNoteVal);

                    warningTv.setVisibility(View.GONE);

                    likeIb.setVisibility(View.VISIBLE);
                    favoriteIb.setVisibility(View.VISIBLE);
                    noteIb.setVisibility(View.VISIBLE);
                    signalerIb.setVisibility(View.VISIBLE);

                }

                postTitreTv.setText(post.getpTitre());
                postDescriptionETv.setText(post.getpDescription());

                try {
                    Picasso.get().load(post.getpCover()).placeholder(R.drawable.ic_post).into(postCoverRv);
                }catch(Exception e){
                    Picasso.get().load(R.drawable.ic_post).into(postCoverRv);
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNumbCommentsVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            long count = snapshot.getChildrenCount();
            commentTv.setText(count <= 1 ? view.getResources()
                    .getString(R.string.nombre_comment, ""+count) : view.getResources()
                    .getString(R.string.nombre_comments, ""+count));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNumbSharesVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.hasChild(myUID)){
                likeTv.setText(view.getResources().getString(R.string.nombre_share, "0"));
                shareIb.setImageResource(R.drawable.ic_no_share);
            }else{
                likeTv.setText(Integer.parseInt(snapshot.child(myUID).getValue().toString()) <= 1 ? view.getResources()
                        .getString(R.string.nombre_share, snapshot.child(myUID).getValue().toString()) : view.getResources()
                        .getString(R.string.nombre_shares, snapshot.child(myUID).getValue().toString()));
                shareIb.setImageResource(R.drawable.ic_share);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNoteVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()){
                noteIb.setImageResource(R.drawable.ic_no_note);
                noteTv.setText(view.getResources().getString(R.string.note_post, "0"));
            } else{
                String item = snapshot.child(myUID).child("nNote").getValue(String.class);
                noteIb.setImageResource(R.drawable.ic_note);
                noteTv.setText(view.getResources().getString(R.string.note_post, item));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener myNumbVuesVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (TextUtils.isEmpty(snapshot.getValue().toString()))
                vueTv.setText(view.getResources().getString(R.string.nombre_vue, "0"));
            else
                vueTv.setText(Integer.parseInt(snapshot.getValue().toString()) <= 1 ? view.getResources()
                        .getString(R.string.nombre_vue, snapshot.getValue().toString()) : view.getResources()
                        .getString(R.string.nombre_vues, snapshot.getValue().toString()));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void addToHisNotifications(String hisUid, String pId, String notification){
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.likeIb && !post.getpCreator().equals(myUID)){
            likePost();
        }else if (v.getId() == R.id.favoriteIb && !post.getpCreator().equals(myUID)){
            favoritePost();
        }else if (v.getId() == R.id.shareIb){
            sharePost(post.getpTitre(), post.getpDescription());
        }else if (v.getId() == R.id.noteIb && !post.getpCreator().equals(myUID)){
            showGiveNoteDialog();
        }else if (v.getId() == R.id.signalerIb && !post.getpCreator().equals(myUID)){
            showGiveWarningDialog();
        }

    }
}