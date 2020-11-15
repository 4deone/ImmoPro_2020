package cm.deone.corp.imopro.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.Gallery;
import cm.deone.corp.imopro.outils.ViewsClickListener;

public class GalleryAdaptor extends RecyclerView.Adapter<GalleryAdaptor.MyHolder> {

    private Context context;
    private ViewsClickListener listener;
    private final List<Gallery> galleryList;
    private String myUID;

    public GalleryAdaptor(Context context, List<Gallery> galleryList) {
        this.context = context;
        this.galleryList = galleryList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        String image = galleryList.get(position).getgImage();
        String description = galleryList.get(position).getgDescription();
        String timestamp = galleryList.get(position).getgDate();

        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("EEEE dd MMMM yyyy", cal).toString();

        holder.descriptionTv.setText(description+"\n"+dateTime);

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
        return galleryList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ImageView itemGalleryIv;
        ProgressBar loadingPb;
        TextView descriptionTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            itemGalleryIv = itemView.findViewById(R.id.itemGalleryIv);
            loadingPb = itemView.findViewById(R.id.loadingPb);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);

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
