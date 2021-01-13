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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Signaler;
import cm.deone.corp.imopro.outils.ViewsClickListener;

import static cm.deone.corp.imopro.outils.Constant.DB_BLOCKED_USERS;

public class SignalerAdaptor extends RecyclerView.Adapter<SignalerAdaptor.MyHolder> {

    private FirebaseUser firebaseUser;

    private final Context context;
    private ViewsClickListener listener;
    private final List<Signaler> signalerList;

    public SignalerAdaptor(Context context, List<Signaler> signalerList) {
        this.context = context;
        this.signalerList = signalerList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signaler_activity, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        String avatar = signalerList.get(position).getuAvatar();
        String name = signalerList.get(position).getuName();
        String message = signalerList.get(position).getsMessage();
        String timestamp = signalerList.get(position).getsDate();
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("EEEE, dd MMMM yyyy hh:mm a", cal).toString();

        holder.commentTv.setText(message);
        holder.timeTv.setText(dateTime);
        holder.hisNameTv.setText(name);

        try {
            Picasso.get().load(avatar).placeholder(R.drawable.ic_user).into(holder.avatarIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_user).into(holder.avatarIv);
        }

    }

    @Override
    public int getItemCount() {
        return signalerList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        ImageView avatarIv;
        TextView hisNameTv;
        TextView commentTv;
        TextView timeTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            hisNameTv = itemView.findViewById(R.id.hisNameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
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
