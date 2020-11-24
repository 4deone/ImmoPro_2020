package cm.deone.corp.imopro.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.User;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.MSG_TYPE_IN;
import static cm.deone.corp.imopro.outils.Constant.MSG_TYPE_OUT;

public class ChatAdaptor extends RecyclerView.Adapter<ChatAdaptor.MyHolder> {

    private FirebaseUser firebaseUser;

    private Context context;
    private ViewsClickListener listener;
    private List<Comment> commentList;

    public ChatAdaptor(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_IN){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_in, parent, false);
            return new MyHolder(view);
        }else if (viewType == MSG_TYPE_OUT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_out, parent, false);
            return new MyHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        String creator = commentList.get(position).getcCreator();
        getHisInformation(holder, ""+creator);
        String message = commentList.get(position).getcMessage();
        holder.hisCommentTv.setText(message);
        String timestamp = commentList.get(position).getcDate();
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("EEEE, dd MMMM yyyy hh:mm a", cal).toString();
        holder.timeTv.setText(dateTime);
        getHisInformation(holder, creator);

    }

    private void getHisInformation(MyHolder holder, String creator) {
        FirebaseDatabase.getInstance().getReference("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    try {
                        Picasso.get().load(user.getuAvatar()).placeholder(R.drawable.ic_user).into(holder.userLikedAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_user).into(holder.userLikedAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (commentList.get(position).getcCreator().equals(firebaseUser.getUid())){
            return MSG_TYPE_OUT;
        }else{
            return MSG_TYPE_IN;
        }
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ImageView userLikedAvatarIv;
        TextView hisCommentTv;
        TextView timeTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            userLikedAvatarIv = itemView.findViewById(R.id.userLikedAvatarIv);
            hisCommentTv = itemView.findViewById(R.id.hisCommentTv);
            timeTv = itemView.findViewById(R.id.timeTv);

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
