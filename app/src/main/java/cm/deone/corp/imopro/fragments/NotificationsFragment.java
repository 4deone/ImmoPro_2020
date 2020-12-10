package cm.deone.corp.imopro.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.deone.corp.imopro.MainActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.adapter.NotParentAdaptor;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.NotChildItem;
import cm.deone.corp.imopro.models.NotParentItem;
import cm.deone.corp.imopro.models.User;

import static cm.deone.corp.imopro.outils.Constant.TAG_POST_ID;

public class NotificationsFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private String pId;
    private String myUID;

    private DatabaseReference refPosts;
    private DatabaseReference refUsers;

    private RecyclerView notificationsRv;
    private NotParentAdaptor notParentAdaptor;

    private HashMap<String, List<String>> hashMapNotification;

    private List<String> likerList;
    private List<String> commenterList;

    private List<NotParentItem> notParentItemList;
    private List<NotChildItem> notChildItemList;

    private View view;

    public NotificationsFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notifications, container, false);
        checkUser();
        initVues();
        allLiker();
        allCommenter();
        LikesNotification();
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

    private void initVues() {
        refPosts = FirebaseDatabase.getInstance().getReference("Posts");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        notificationsRv = view.findViewById(R.id.notificationsRv);
    }

    private void allCommenter() {
        commenterList = new ArrayList<>();
        refPosts.child(pId).child("Comments").addValueEventListener(valCommenter);
    }

    private void allLiker() {
        likerList = new ArrayList<>();
        refPosts.child(pId).child("Likes").addValueEventListener(valLiker);
    }

    private void LikesNotification() {
        notParentItemList = new ArrayList<>();
        notChildItemList = new ArrayList<>();
        refUsers.addValueEventListener(valUserInfo);
    }

    private final ValueEventListener valLiker = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            likerList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                String item = ds.getKey();
                likerList.add(item);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valCommenter = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            commenterList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                Comment comment = ds.getValue(Comment.class);
                if (!commenterList.contains(comment.getcId())){
                    commenterList.add(comment.getcId());
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private final ValueEventListener valUserInfo = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                User user = ds.getValue(User.class);
                notChildItemList.clear();

                NotParentItem notParentItem = new NotParentItem(""+user.getuAvatar(),
                        ""+user.getuName());

                if (likerList.contains(user.getuId())){
                    notChildItemList.add(new NotChildItem("Like", "Aime votre post", "10 Novembre 1980"));
                    Log.e("TAG_LIKES_NUMBER", ""+likerList.size());
                }

                if (commenterList.contains(user.getuId())){
                    notChildItemList.add(new NotChildItem("Comment", "Commentez votre post", "21 Novembre 1990"));
                    Log.e("TAG_COMMENTS_NUMBER", ""+commenterList.size());
                }

                notParentItem.setNotChildItemList(notChildItemList);

                notParentItemList.add(notParentItem);

                notParentAdaptor = new NotParentAdaptor(getActivity(), notParentItemList);
                notificationsRv.setAdapter(notParentAdaptor);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}