package cm.deone.corp.imopro.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import cm.deone.corp.imopro.SettingsActivity;
import cm.deone.corp.imopro.adapter.CommentAdaptor;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.User;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.TAG_POST_CREATOR;
import static cm.deone.corp.imopro.outils.Constant.TAG_POST_ID;

public class CommentFragment extends Fragment {

    private DatabaseReference reference;

    private String pId;
    private String topicComment;
    private String pCreator;
    private String myUID;

    private ImageView coverPostIv;
    private TextView titreTv;
    private TextView nbreComments;
    private RelativeLayout commentFooterRl;

    private RecyclerView commentsRv;
    private CommentAdaptor commentAdaptor;
    private List<Comment> commentList;
    private List<String> blockedList;

    private ImageView userIv;
    private EditText commentEdtv;

    private View view;

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            pId = getArguments().getString(""+TAG_POST_ID);
            pCreator = getArguments().getString(""+TAG_POST_CREATOR);
            topicComment = getArguments().getString("pTopicComment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_comment, container, false);
        checkUsers();
        initVues();
        reference.child("Users").addValueEventListener(valGetUser);
        reference.child("Posts").addValueEventListener(valGetPost);
        blockedList = new ArrayList<>();
        reference.child("Posts").child(pId).child("BlockedUsers").addValueEventListener(valGetBlockedList);
        commentList = new ArrayList<>();
        reference.child("Posts").child(pId).child("Comments").addValueEventListener(valAllComments);
        return view;
    }

    @Override
    public void onStart() {
        checkUsers();
        reference.child("Users").addValueEventListener(valGetUser);
        reference.child("Posts").addValueEventListener(valGetPost);
        blockedList = new ArrayList<>();
        reference.child("Posts").child(pId).child("BlockedUsers").addValueEventListener(valGetBlockedList);
        commentList = new ArrayList<>();
        reference.child("Posts").child(pId).child("Comments").addValueEventListener(valAllComments);
        super.onStart();
    }

    @Override
    public void onResume() {
        checkUsers();
        reference.child("Users").addValueEventListener(valGetUser);
        reference.child("Posts").addValueEventListener(valGetPost);
        blockedList = new ArrayList<>();
        reference.child("Posts").child(pId).child("BlockedUsers").addValueEventListener(valGetBlockedList);
        commentList = new ArrayList<>();
        reference.child("Posts").child(pId).child("Comments").addValueEventListener(valAllComments);
        super.onResume();
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
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        coverPostIv = view.findViewById(R.id.coverPostIv);
        commentFooterRl = view.findViewById(R.id.commentFooterRl);
        titreTv = view.findViewById(R.id.titreTv);
        nbreComments = view.findViewById(R.id.nbreComments);
        commentsRv = view.findViewById(R.id.commentsRv);
        userIv = view.findViewById(R.id.userIv);
        commentEdtv = view.findViewById(R.id.commentEdtv);
        ImageButton sendIb = view.findViewById(R.id.sendIb);

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
        DatabaseReference refUpload = reference.child("Posts").child(pId).child("Comments");
        refUpload.child(timestamp).setValue(hashMapComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                resetVues();

                HashMap<String, Object> hashMapNbreComment = new HashMap<>();
                hashMapNbreComment.put("pNComments", ""+ commentList.size());

                reference.child("Posts").child(pId).updateChildren(hashMapNbreComment);

                //addToHisNotifications(""+post.getpCreator(), ""+post.getpId(), "Commenté votre post");

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

    private void showCommentDialog(Comment comment, boolean isblocked) {
        String[] optionsUsers = {"Supprimer le commentaire", "Details du commentaire", "Envoyer un message", "Pofile de l'utilisateur"};
        String[] optionsCreator = {isblocked ? "Débloquer l'utilisateur" : "Bloquer l'utilisateur",
                "Details du commentaire", "Envoyer un message"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void confirmationRequise(String action, String identifiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                Toast.makeText(getActivity(), "Utilisateur bloqué", Toast.LENGTH_SHORT).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void debloquerLutilisateur(String cCreator) {
        reference.child("Posts").child(pId).child("BlockedUsers").orderByChild("bId").equalTo(cCreator)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "Utilisateur débloqué", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void supprimerLeCommentaire(String cId) {
        reference.child("Posts").child(pId).child("Comments").child(cId).removeValue()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "Commentaire supprimé", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        reference.child("Posts").child(pId).addListenerForSingleValueEvent(valUpdateCommentNumber);
    }

    private final ValueEventListener valAllComments = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            commentList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                Comment comment = ds.getValue(Comment.class);
                commentList.add(comment);
                commentAdaptor = new CommentAdaptor(getActivity(), commentList, pId);
                commentsRv.setAdapter(commentAdaptor);
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
            nbreComments.setText(commentList.size() <= 1 ? getActivity().getResources().getString(R.string.nombre_comment, ""+commentList.size()) :
                    view.getResources().getString(R.string.nombre_comments, ""+commentList.size()));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private final ValueEventListener valUpdateCommentNumber = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String comments = "" + snapshot.child("pNComments").getValue();
            int newCommentVal = Integer.parseInt(comments) - 1;
            reference.child("Posts").child(pId).child("pNComments").setValue(""+newCommentVal);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valGetPost = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                Post post = ds.getValue(Post.class);
                if (post.getpId().equals(pId)){
                    titreTv.setText(post.getpTitre());
                    commentFooterRl.setVisibility(post.getpCreator().equals(myUID) ? View.GONE : View.VISIBLE);
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

    private final ValueEventListener valGetBlockedList = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            blockedList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                blockedList.add(ds.getKey());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}