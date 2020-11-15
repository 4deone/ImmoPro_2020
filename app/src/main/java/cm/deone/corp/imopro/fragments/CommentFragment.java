package cm.deone.corp.imopro.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.deone.corp.imopro.MainActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.adapter.CommentAdaptor;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.User;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.TAG_POST_ID;

public class CommentFragment extends Fragment {

    private DatabaseReference reference;
    private String pId;
    private String myUID;
    private Toolbar toolbar;
    private ImageView coverPostIv;
    private TextView titreTv;
    private TextView nbreComments;
    private RecyclerView commentsRv;
    private CommentAdaptor commentAdaptor;

    private List<Comment> commentList;
    private ImageView userIv;
    private EditText commentEdtv;
    private ImageButton sendIb;

    private View view;

    private Post post;

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            pId = getArguments().getString(TAG_POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_comment, container, false);
        checkUsers();
        initVues();
        allComments();
        getMyAvatar();
        getPost();
        return view;
    }

    @Override
    public void onStart() {
        checkUsers();
        allComments();
        getMyAvatar();
        getPost();
        super.onStart();
    }

    @Override
    public void onResume() {
        checkUsers();
        allComments();
        getMyAvatar();
        getPost();
        super.onResume();
    }

    private final ValueEventListener valGetPost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                post = ds.getValue(Post.class);
                if (post.getpId().equals(pId)){
                    titreTv.setText(post.getpTitre());
                    try {
                        Picasso.get().load(post.getpCover()).placeholder(R.drawable.ic_post).into(coverPostIv);
                    }catch(Exception e){
                        Picasso.get().load(R.drawable.ic_post).into(coverPostIv);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void getPost() {
        DatabaseReference refPost = reference.child("Posts");
        refPost.addValueEventListener(valGetPost);
    }

    private final ValueEventListener valGetUser = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                User user = ds.getValue(User.class);
                if (user.getuId().equals(myUID)){
                    try {
                        Picasso.get().load(user.getuAvatar()).placeholder(R.drawable.ic_user).into(userIv);
                    }catch(Exception e){
                        Picasso.get().load(R.drawable.ic_user).into(userIv);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void getMyAvatar() {
        DatabaseReference refUser = reference.child("Users");
        refUser.addValueEventListener(valGetUser);
    }

    private void allComments() {
        commentList = new ArrayList<>();
        DatabaseReference refComment = reference.child("Comments").child(pId);
        refComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Comment comment = ds.getValue(Comment.class);
                    commentList.add(comment);
                    commentAdaptor = new CommentAdaptor(getActivity(), commentList);
                    commentsRv.setAdapter(commentAdaptor);
                    commentAdaptor.setOnItemClickListener(new ViewsClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    });
                }
                nbreComments.setText(commentList.size() <= 1 ? getActivity().getResources().getString(R.string.nombre_comment, ""+commentList.size()) :
                        view.getResources().getString(R.string.nombre_comments, ""+commentList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUsers() {
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
        coverPostIv = view.findViewById(R.id.coverPostIv);
        titreTv = view.findViewById(R.id.titreTv);
        nbreComments = view.findViewById(R.id.nbreComments);
        commentsRv = view.findViewById(R.id.commentsRv);
        userIv = view.findViewById(R.id.userIv);
        commentEdtv = view.findViewById(R.id.commentEdtv);
        sendIb = view.findViewById(R.id.sendIb);
        reference = FirebaseDatabase.getInstance().getReference();
        sendIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificationDeSaisie();
            }
        });
    }

    private void verificationDeSaisie() {
        String message = commentEdtv.getText().toString().trim();
        if (TextUtils.isEmpty(message)){
            Toast.makeText(getActivity(), "Votre commentaire est vide!", Toast.LENGTH_SHORT).show();
            return;
        }
        prepareData(""+message);
    }

    private void prepareData(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, String> hashMapComment = new HashMap<>();
        hashMapComment.put("cCreator", myUID);
        hashMapComment.put("cMessage", message);
        hashMapComment.put("cId", timestamp);
        hashMapComment.put("cDate", timestamp);

        uploadData(
                hashMapComment,
                ""+timestamp);

    }

    private void uploadData(HashMap<String, String> hashMapComment, String timestamp) {
        DatabaseReference refUpload = reference.child("Comments").child(pId);
        refUpload.child(timestamp).setValue(hashMapComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                resetVues();

                HashMap<String, Object> hashMapNbreComment = new HashMap<>();
                hashMapNbreComment.put("pNComments", ""+ commentList.size());

                reference.child("Posts").child(pId).updateChildren(hashMapNbreComment);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetVues() {
        commentEdtv.setText(null);
        commentEdtv.setHint("Votre commentaire");
    }

}