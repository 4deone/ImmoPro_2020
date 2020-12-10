package cm.deone.corp.imopro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.models.NotParentItem;
import cm.deone.corp.imopro.outils.ViewsClickListener;

public class NotParentAdaptor extends RecyclerView.Adapter<NotParentAdaptor.MyHolder> {

    private Context context;
    private ViewsClickListener listener;
    private List<NotParentItem> notParentItemList;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public NotParentAdaptor(Context context, List<NotParentItem> notParentItemList) {
        this.context = context;
        this.notParentItemList = notParentItemList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_not_parent, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        NotParentItem notParentItem = notParentItemList.get(position);

        String name = notParentItem.getnName();
        String image = notParentItem.getnAvatar();

        holder.userNameTv.setText(name);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_user).into(holder.avatarIv);
        }catch(Exception e){
            Picasso.get().load(R.drawable.ic_user).into(holder.avatarIv);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.childRv.getContext());
        layoutManager.setInitialPrefetchItemCount(notParentItem.getNotChildItemList().size());

        NotChildAdapter notChildAdapter = new NotChildAdapter(context,
                notParentItem.getNotChildItemList());
        holder.childRv.setLayoutManager(layoutManager);
        holder.childRv.setAdapter(notChildAdapter);
        holder.childRv.setRecycledViewPool(viewPool);

    }

    @Override
    public int getItemCount() {
        return notParentItemList.size();
    }

    public void setOnItemClickListener(ViewsClickListener listener){
        this.listener = listener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ImageView avatarIv;
        TextView userNameTv;
        RecyclerView childRv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            userNameTv = itemView.findViewById(R.id.userNameTv);
            childRv = itemView.findViewById(R.id.childRv);

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
