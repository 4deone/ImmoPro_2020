package cm.deone.corp.imopro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.ViewsClickListener;

public class NotificationAdaptor extends RecyclerView.Adapter<NotificationAdaptor.MyHolder> {

    private Context context;
    private ViewsClickListener listener;
    private List<String> imageList;
    private String myUID;

    public NotificationAdaptor(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        String image = imageList.get(position);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_post).into(holder.itemGalleryIv, new Callback() {
                @Override
                public void onSuccess() {
                    holder.loadingPb.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Gallery_Adaptor", e.getMessage());
                }
            });
        }catch(Exception e){
            Picasso.get().load(R.drawable.ic_post).into(holder.itemGalleryIv, new Callback() {
                @Override
                public void onSuccess() {
                    holder.loadingPb.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Gallery_Adaptor", e.getMessage());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ImageView itemGalleryIv;
        ProgressBar loadingPb;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            itemGalleryIv = itemView.findViewById(R.id.itemGalleryIv);
            loadingPb = itemView.findViewById(R.id.loadingPb);

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
