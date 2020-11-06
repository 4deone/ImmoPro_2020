package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import cm.deone.corp.imopro.models.Post;

public class PostActivity extends AppCompatActivity {

    private String pId;

    private ImageView toolbar_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        pId = getIntent().getStringExtra("pId");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Post - Immopro");
        setSupportActionBar(toolbar);

        toolbar_image = findViewById(R.id.toolbar_image);

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByKey().equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    try {
                        Picasso.get().load(post.getpCover()).placeholder(R.drawable.ic_post).into(toolbar_image);
                    }catch(Exception e){
                        Picasso.get().load(R.drawable.ic_post).into(toolbar_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}