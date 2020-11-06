package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cm.deone.corp.imopro.adapter.PostsAdaptor;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.ViewsClickListener;

public class HomeActivity extends AppCompatActivity {

    private String myUID;
    private RecyclerView postsRv;
    private List<Post> postList;
    private PostsAdaptor postsAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkUsers();
        initVues();
        allPosts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        manageSearchView(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_add_operation){
            startActivity(new Intent(HomeActivity.this, CreatePostActivity.class));
        }else if (item.getItemId() == R.id.menu_show_settings){
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUsers(){
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null){
            myUID = fUser.getUid();
        }else{
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initVues(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home - Immopro");
        setSupportActionBar(toolbar);
        postsRv = findViewById(R.id.postsRv);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        llManager.setStackFromEnd(true);
        postsRv.setHasFixedSize(true);
        postsRv.setLayoutManager(llManager);
    }

    private void manageSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else {
                    allPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    searchPosts(newText);
                }else {
                    allPosts();
                }
                return false;
            }
        });
    }

    private void allPosts() {
        postList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    if (post.getpPublicOrPrivate().equals("public")){
                        postList.add(post);
                    }else if(post.getpCreator().equals(myUID)){
                        postList.add(post);
                    }
                    postsAdaptor = new PostsAdaptor(HomeActivity.this, postList, myUID);
                    postsRv.setAdapter(postsAdaptor);
                    postsAdaptor.setOnItemClickListener(new ViewsClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                            intent.putExtra("pId", postList.get(position).getpId());
                            startActivity(intent);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(String searchQuery) {
        postList = new ArrayList<>();
        postList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    if (post.getpTitre().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            post.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        if (post.getpPublicOrPrivate().equals("public")){
                            postList.add(post);
                        }else if(post.getpCreator().equals(myUID)){
                            postList.add(post);
                        }
                    }
                    postsAdaptor = new PostsAdaptor(HomeActivity.this, postList, myUID);
                    postsRv.setAdapter(postsAdaptor);
                    postsAdaptor.setOnItemClickListener(new ViewsClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                            intent.putExtra("pId", postList.get(position).getpId());
                            startActivity(intent);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}