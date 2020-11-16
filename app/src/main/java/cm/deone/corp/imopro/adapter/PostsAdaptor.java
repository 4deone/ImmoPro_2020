package cm.deone.corp.imopro.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.User;
import cm.deone.corp.imopro.outils.ViewsClickListener;

public class PostsAdaptor extends RecyclerView.Adapter<PostsAdaptor.MyHolder> {

    private Context context;
    private ViewsClickListener listener;
    private List<Post> postList;
    private String myUID;

    public PostsAdaptor(Context context, List<Post> postList, String myUID) {
        this.context = context;
        this.postList = postList;
        this.myUID = myUID;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        String creator = postList.get(position).getpCreator();
        String cover = postList.get(position).getpCover();
        String titre = postList.get(position).getpTitre();
        String description = postList.get(position).getpDescription();

        String vues = postList.get(position).getpNVues();
        String likes = postList.get(position).getpNLikes();
        String comments = postList.get(position).getpNComments();

        String timestamp = postList.get(position).getpDate();
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("EEEE, dd MMMM yyyy hh:mm a", cal).toString();

        holder.itemdateTv.setText(dateTime);

        holder.itemTitrePostTv.setText(titre);
        holder.itemDescriptionPostTv.setText(description);

        holder.itemVuesPostTv.setText(Integer.parseInt(vues) <= 1 ? context.getResources().getString(R.string.nombre_vue, vues) : context.getResources().getString(R.string.nombre_vues, vues));
        holder.itemLikesPostTv.setText(Integer.parseInt(likes) <= 1 ? context.getResources().getString(R.string.nombre_like, likes) : context.getResources().getString(R.string.nombre_likes, likes));
        holder.itemCommentsPostTv.setText(Integer.parseInt(comments) <= 1 ? context.getResources().getString(R.string.nombre_comment, comments) : context.getResources().getString(R.string.nombre_comments, comments));

        try {
            Picasso.get().load(cover).placeholder(R.drawable.ic_post).into(holder.itemCoverIv, new Callback() {
                @Override
                public void onSuccess() {
                    holder.loadingPb.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Post_Adaptor", e.getMessage());
                }
            });
        }catch(Exception e){
            Picasso.get().load(R.drawable.ic_post).into(holder.itemCoverIv, new Callback() {
                @Override
                public void onSuccess() {
                    holder.loadingPb.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Post_Adaptor", e.getMessage());
                }
            });
        }
        getUserInfos(holder, ""+creator);
    }

    private void getUserInfos(final MyHolder holder, String creator) {
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    if (user.getuId().equals(creator)){
                        if (user.getuId().equals(myUID)){
                            holder.itemUserNameTv.setText("Vous");
                        }else{
                            holder.itemUserNameTv.setText(user.getuName());
                        }

                        try {
                            Picasso.get().load(user.getuAvatar()).placeholder(R.drawable.ic_user).into(holder.itemUserAvatarIv, new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.loadingUserPb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d("Post_Adaptor", e.getMessage());
                                }
                            });
                        }catch(Exception e){
                            Picasso.get().load(R.drawable.ic_user).into(holder.itemUserAvatarIv, new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.loadingUserPb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d("Post_Adaptor", e.getMessage());
                                }
                            });
                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ProgressBar loadingPb;
        ProgressBar loadingUserPb;
        ImageView itemCoverIv;
        ImageView itemUserAvatarIv;
        TextView itemVuesPostTv;
        TextView itemLikesPostTv;
        TextView itemCommentsPostTv;
        TextView itemUserNameTv;
        TextView itemdateTv;
        TextView itemTitrePostTv;
        TextView itemDescriptionPostTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            loadingPb = itemView.findViewById(R.id.loadingPb);
            loadingUserPb = itemView.findViewById(R.id.loadingUserPb);
            itemCoverIv = itemView.findViewById(R.id.itemCoverIv);
            itemUserAvatarIv = itemView.findViewById(R.id.itemUserAvatarIv);
            itemVuesPostTv = itemView.findViewById(R.id.itemVuesPostTv);
            itemLikesPostTv = itemView.findViewById(R.id.itemLikesPostTv);
            itemCommentsPostTv = itemView.findViewById(R.id.itemCommentsPostTv);
            itemUserNameTv = itemView.findViewById(R.id.itemUserNameTv);
            itemdateTv = itemView.findViewById(R.id.itemdateTv);
            itemTitrePostTv = itemView.findViewById(R.id.itemTitrePostTv);
            itemDescriptionPostTv = itemView.findViewById(R.id.itemDescriptionPostTv);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(v, position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onLongItemClick(v, position);
            }
            return true;
        }
    }
}
