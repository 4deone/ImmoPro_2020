package cm.deone.corp.imopro.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import cm.deone.corp.imopro.MainActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.SettingsActivity;
import cm.deone.corp.imopro.models.Post;

public class HomeFragment extends Fragment  implements View.OnClickListener{

    private static final String TAG_POST_ID = "pId";
    private static final String TAG_POST_CREATOR = "pCreator";

    private DatabaseReference reference;

    private String pId;
    private String pCreator;
    private String myUID;
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


    private ImageView postCoverRv;
    private TextView postTitreTv;
    private TextView postDescriptionETv;
    private Toolbar toolbar;

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
        getPostNote();
        setLiked();
        setSignaled();
        setFavorite();
        return view;

    }

    @Override
    public void onStart() {
        checkUser();
        getPostInformations();
        getPostNote();
        setLiked();
        setSignaled();
        setFavorite();
        super.onStart();
    }

    @Override
    public void onResume() {
        checkUser();
        getPostInformations();
        getPostNote();
        setLiked();
        setSignaled();
        setFavorite();
        super.onResume();
    }

    @Override
    public void onStop() {
        if(valPostFavorite!=null){
            reference.removeEventListener(valPostFavorite);
        }
        if(valPostNotes!=null){
            reference.removeEventListener(valPostNotes);
        }
        if(postInfosVal!=null){
            reference.removeEventListener(postInfosVal);
        }
        if(valSetSignaled!=null){
            reference.removeEventListener(valSetSignaled);
        }
        if(valeSetLiked!=null){
            reference.removeEventListener(valeSetLiked);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if(valPostFavorite!=null){
            reference.removeEventListener(valPostFavorite);
        }
        if(valPostNotes!=null){
            reference.removeEventListener(valPostNotes);
        }
        if(postInfosVal!=null){
            reference.removeEventListener(postInfosVal);
        }
        if(valSetSignaled!=null){
            reference.removeEventListener(valSetSignaled);
        }
        if(valeSetLiked!=null){
            reference.removeEventListener(valeSetLiked);
        }
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
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        reference = FirebaseDatabase.getInstance().getReference();

        postCoverRv = view.findViewById(R.id.postCoverRv);
        postTitreTv = view.findViewById(R.id.postTitreTv);
        postDescriptionETv = view.findViewById(R.id.postDescriptionETv);

        vueTv = view.findViewById(R.id.vueTv);
        likeTv = view.findViewById(R.id.likeTv);
        commentTv = view.findViewById(R.id.commentTv);
        noteTv = view.findViewById(R.id.noteTv);
        signalerIb = view.findViewById(R.id.signalerIb);

        noteIb = view.findViewById(R.id.noteIb);
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
            reference.child("Favorites").child(post.getpId()).child(myUID).removeValue();
            mProcessFavorites = false;
            favoriteIb.setImageResource(R.drawable.ic_no_favorite);
        }else {
            reference.child("Favorites").child(post.getpId()).child(myUID).setValue("Favotite");
            mProcessFavorites = true;
            favoriteIb.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void setFavorite() {
        Query query = reference.child("Favorites").child(pId);
        query.addValueEventListener(valPostFavorite);
    }

    private void setSignaled() {
        reference.child("Warnings").child(pId).addValueEventListener(valSetSignaled);
    }

    private void setLiked() {
        reference.child("Likes").child(pId).addValueEventListener(valeSetLiked);
    }

    private void likePost() {
        if (mProcessLikes){
            reference.child("Posts").child(post.getpId()).child("pNLikes").setValue(""+ (Integer.parseInt(post.getpNLikes()) - 1));
            reference.child("Likes").child(post.getpId()).child(myUID).removeValue();
            mProcessLikes = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
        }else {
            reference.child("Posts").child(post.getpId()).child("pNLikes").setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));
            reference.child("Likes").child(post.getpId()).child(myUID).setValue("Liked");
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
        reference.child("Notes").child(pId).child(myUID).setValue(note);
    }

    private void getPostNote() {
        reference.child("Notes").child(pId).addValueEventListener(valPostNotes);
    }

    private void showGiveWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Signaler ce post");
        builder.setMessage("Etes-vous sur de vouloir signaler ce post ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        procederAuSignalement();
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

                setPostSignal(""+signaler);
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void setPostSignal(String signaler) {
        if (mProcessSignal){
            reference.child("Posts").child(post.getpId()).child("pNSignals").setValue(""+ (Integer.parseInt(post.getpNSignals()) - 1));
            reference.child("Warnings").child(post.getpId()).child(myUID).removeValue();
            mProcessSignal = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
        }else {
            reference.child("Posts").child(post.getpId()).child("pNSignals").setValue(""+ (Integer.parseInt(post.getpNSignals()) + 1));
            reference.child("Warnings").child(post.getpId()).child(myUID).setValue("Signaled");
            mProcessSignal = true;
            likeIb.setImageResource(R.drawable.ic_like);
        }
    }

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
                note = note + Float.parseFloat(ds.getValue().toString());
            }
            note = note/snapshot.getChildrenCount();
            noteTv.setText(""+note+"/20");
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
                    vueTv.setVisibility(View.VISIBLE);
                    likeTv.setVisibility(View.VISIBLE);
                    commentTv.setVisibility(View.VISIBLE);

                    vueTv.setText(Integer.parseInt(post.getpNVues()) <= 1 ? view.getResources().getString(R.string.nombre_vue, post.getpNVues()) :
                            view.getResources().getString(R.string.nombre_vues, post.getpNVues()));
                    likeTv.setText(Integer.parseInt(post.getpNLikes()) <= 1 ? view.getResources().getString(R.string.nombre_like, post.getpNLikes()) :
                            view.getResources().getString(R.string.nombre_likes, post.getpNLikes()));
                    commentTv.setText(Integer.parseInt(post.getpNComments()) <= 1 ? view.getResources().getString(R.string.nombre_comment, post.getpNComments()) :
                            view.getResources().getString(R.string.nombre_comments, post.getpNComments()));
                    noteTv.setText(view.getResources().getString(R.string.note_post, post.getpNote()));

                    likeIb.setVisibility(View.GONE);
                    favoriteIb.setVisibility(View.GONE);
                    noteIb.setVisibility(View.GONE);
                    signalerIb.setVisibility(View.GONE);
                }else{
                    vueTv.setVisibility(View.GONE);
                    likeTv.setVisibility(View.GONE);
                    commentTv.setVisibility(View.GONE);

                    noteTv.setGravity(Gravity.CENTER);

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

    private final ValueEventListener valSetSignaled = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(myUID)){
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

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.likeIb && !post.getpCreator().equals(myUID)){
            likePost();
        }else if (v.getId() == R.id.favoriteIb && !post.getpCreator().equals(myUID)){
            favoritePost();
        }else if (v.getId() == R.id.shareIb){

        }else if (v.getId() == R.id.noteIb && !post.getpCreator().equals(myUID)){
            showGiveNoteDialog();
        }else if (v.getId() == R.id.signalerIb && !post.getpCreator().equals(myUID)){
            showGiveWarningDialog();
        }

    }
}