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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.Signaler;
import cm.deone.corp.imopro.outils.ViewsClickListener;

public class SettingsPostAdaptor extends RecyclerView.Adapter<SettingsPostAdaptor.MyHolder> {

    private FirebaseUser firebaseUser;

    private final Context context;
    private ViewsClickListener listener;
    private final List<Post> postList;

    public SettingsPostAdaptor(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_settings, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        String avatar = postList.get(position).getpCover();
        String titre = postList.get(position).getpTitre();
        String description = postList.get(position).getpDescription();
        String vues = postList.get(position).getpNVues();

        holder.itemTitlePostTv.setText(titre);
        holder.itemDescriptionPostTv.setText(description);
        holder.itemViewsPostTv.setText(vues);

        try {
            Picasso.get().load(avatar).placeholder(R.drawable.ic_user).into(holder.ivItemPost);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_user).into(holder.ivItemPost);
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        ImageView ivItemPost;
        TextView itemTitlePostTv;
        TextView itemDescriptionPostTv;
        TextView itemViewsPostTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            ivItemPost = itemView.findViewById(R.id.ivItemPost);
            itemTitlePostTv = itemView.findViewById(R.id.itemTitlePostTv);
            itemDescriptionPostTv = itemView.findViewById(R.id.itemDescriptionPostTv);
            itemViewsPostTv = itemView.findViewById(R.id.itemViewsPostTv);

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
