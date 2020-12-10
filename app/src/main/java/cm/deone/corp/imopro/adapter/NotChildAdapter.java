package cm.deone.corp.imopro.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.NotChildItem;
import cm.deone.corp.imopro.outils.ViewsClickListener;

public class NotChildAdapter extends RecyclerView.Adapter<NotChildAdapter.MyHolder> {

    private Context context;
    private ViewsClickListener listener;
    private final List<NotChildItem> notChildItemList;

    public NotChildAdapter(Context context, List<NotChildItem> notChildItemList) {
        this.context = context;
        this.notChildItemList = notChildItemList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_not_child, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        NotChildItem notChildItem = notChildItemList.get(position);

        String titre = notChildItem.getnActivite();
        String description = notChildItem.getnMessage();
        String timestamp = notChildItem.getnTime();

        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("EEEE dd MMMM yyyy", cal).toString();

        holder.titreTv.setText(titre);
        holder.notifTv.setText(description);
        holder.timeTv.setText(dateTime);

    }

    @Override
    public int getItemCount() {
        return notChildItemList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        TextView titreTv;
        TextView notifTv;
        TextView timeTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            titreTv = itemView.findViewById(R.id.titreTv);
            notifTv = itemView.findViewById(R.id.notifTv);
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
