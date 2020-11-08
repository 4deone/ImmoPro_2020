package cm.deone.corp.imopro.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.deone.corp.imopro.MainActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.adapter.GalleryAdaptor;
import cm.deone.corp.imopro.models.Post;

public class HomeFragment extends Fragment {

    private static final String TAG_POST_ID = "pId";

    private DatabaseReference reference;
    private DatabaseReference refLikes;
    private DatabaseReference refFavorites;

    private String pId;
    private String myUID;
    private Post post;

    private boolean mProcessLikes = false;
    private boolean mProcessFavorites = false;
    private ImageButton likeIb;
    private ImageButton favoriteIb;
    private ImageButton shareIb;
    private ImageButton noteIb;

    private TextView vueTv;
    private TextView likeTv;
    private TextView commentTv;
    private TextView noteTv;

    private List<String> imageList;
    private RecyclerView postImagesRv;
    private GalleryAdaptor galleryAdaptor;
    private TextView postTitreTv;
    private TextView postDescriptionETv;
    private Toolbar toolbar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pId = getArguments().getString(TAG_POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        checkUser();
        initVues(view);
        getPostInformations();
        setLiked();
        setFavorite();
        return view;

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_add_operation).setVisible(false);
        menu.findItem(R.id.menu_show_settings).setVisible(false);
        menu.findItem(R.id.menu_add_image).setVisible(true);
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

    private void initVues(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        //toolbar.setTitle("Post - Immopro");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        reference = FirebaseDatabase.getInstance().getReference("Posts");
        refLikes = FirebaseDatabase.getInstance().getReference("Likes");
        refFavorites = FirebaseDatabase.getInstance().getReference("Favorites");

        postImagesRv = view.findViewById(R.id.postImagesRv);
        postTitreTv = view.findViewById(R.id.postTitreTv);
        postDescriptionETv = view.findViewById(R.id.postDescriptionETv);

        vueTv = view.findViewById(R.id.vueTv);
        likeTv = view.findViewById(R.id.likeTv);
        commentTv = view.findViewById(R.id.commentTv);
        noteTv = view.findViewById(R.id.noteTv);

        noteIb = view.findViewById(R.id.likeIb);
        likeIb = view.findViewById(R.id.likeIb);
        favoriteIb = view.findViewById(R.id.favoriteIb);
        shareIb = view.findViewById(R.id.shareIb);
        likeIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post.getpCreator().equals(myUID)){
                    likePost();
                }
            }
        });
        favoriteIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post.getpCreator().equals(myUID)){
                    favoritePost();
                }
            }
        });
        shareIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        noteIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void favoritePost() {
        if (mProcessFavorites){
            refFavorites.child(post.getpId()).child(myUID).removeValue();
            mProcessFavorites = false;
            favoriteIb.setImageResource(R.drawable.ic_no_favorite);
        }else {
            refFavorites.child(post.getpId()).child(myUID).setValue("Favotite");
            mProcessFavorites = true;
            favoriteIb.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void setFavorite() {
        Query query = refFavorites.child(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(myUID)){
                    favoriteIb.setImageResource(R.drawable.ic_favorite);
                    mProcessFavorites = true;
                }else {
                    favoriteIb.setImageResource(R.drawable.ic_no_favorite);
                    mProcessFavorites = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLiked() {
        Query query = refLikes.child(pId);
        query.addValueEventListener(new ValueEventListener() {
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
        });
    }

    private void likePost() {
        if (mProcessLikes){
            reference.child(post.getpId()).child("pNLikes").setValue(""+ (Integer.parseInt(post.getpNLikes()) - 1));
            refLikes.child(post.getpId()).child(myUID).removeValue();
            mProcessLikes = false;
            likeIb.setImageResource(R.drawable.ic_no_like);
        }else {
            reference.child(post.getpId()).child("pNLikes").setValue(""+ (Integer.parseInt(post.getpNLikes()) + 1));
            refLikes.child(post.getpId()).child(myUID).setValue("Liked");
            mProcessLikes = true;
            likeIb.setImageResource(R.drawable.ic_like);
        }
    }

    private void getPostInformations() {
        imageList = new ArrayList<>();
        Query query = reference.orderByKey().equalTo(pId);
        query.addValueEventListener(postInfosVal);
    }

    private final ValueEventListener postInfosVal = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            imageList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);

                if (post.getpCreator().equals(myUID)){
                    vueTv.setVisibility(View.VISIBLE);
                    likeTv.setVisibility(View.VISIBLE);
                    commentTv.setVisibility(View.VISIBLE);

                    vueTv.setText(Integer.parseInt(post.getpNVues()) <= 1 ? getActivity().getResources().getString(R.string.nombre_vue, post.getpNVues()) :
                            getActivity().getResources().getString(R.string.nombre_vues, post.getpNVues()));
                    likeTv.setText(Integer.parseInt(post.getpNLikes()) <= 1 ? getActivity().getResources().getString(R.string.nombre_like, post.getpNLikes()) :
                            getActivity().getResources().getString(R.string.nombre_likes, post.getpNLikes()));
                    commentTv.setText(Integer.parseInt(post.getpNComments()) <= 1 ? getActivity().getResources().getString(R.string.nombre_comment, post.getpNComments()) :
                            getActivity().getResources().getString(R.string.nombre_comments, post.getpNComments()));
                    noteTv.setText(getActivity().getResources().getString(R.string.note_post, post.getpNote()));

                    likeIb.setVisibility(View.GONE);
                    favoriteIb.setVisibility(View.GONE);
                    noteIb.setVisibility(View.GONE);
                }else{
                    vueTv.setVisibility(View.GONE);
                    likeTv.setVisibility(View.GONE);
                    commentTv.setVisibility(View.GONE);

                    likeIb.setVisibility(View.VISIBLE);
                    favoriteIb.setVisibility(View.VISIBLE);
                    noteIb.setVisibility(View.VISIBLE);
                }

                postTitreTv.setText(post.getpTitre());
                postDescriptionETv.setText(post.getpDescription());
                imageList.add(post.getpCover());

                galleryAdaptor = new GalleryAdaptor(getActivity(), imageList);
                postImagesRv.setAdapter(galleryAdaptor);


            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}