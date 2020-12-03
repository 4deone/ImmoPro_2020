package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cm.deone.corp.imopro.adapter.PostsAdaptor;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.outils.ViewsClickListener;
import cm.deone.corp.imopro.notification.Token;

import static cm.deone.corp.imopro.outils.Constant.DB_COMMENT;
import static cm.deone.corp.imopro.outils.Constant.DB_POST;

public class HomeActivity extends AppCompatActivity {

    private String myUID;
    private String search;
    private RecyclerView postsRv;
    private List<Post> postList;
    private PostsAdaptor postsAdaptor;
    private FloatingActionButton searchFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkUsers();
        initVues();
        initGroupTopic();
        allPosts();
        updateToken();
    }

    @Override
    protected void onStart() {
        checkUsers();
        initGroupTopic();
        allPosts();
        updateToken();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUsers();
        initGroupTopic();
        allPosts();
        updateToken();
        super.onResume();
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
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", myUID);
            editor.apply();
        }else{
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initVues(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setSubtitle(getResources().getString(R.string.app_subtitle));
        setSupportActionBar(toolbar);
        searchFab = findViewById(R.id.searchFab);
        postsRv = findViewById(R.id.postsRv);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        llManager.setStackFromEnd(true);
        postsRv.setHasFixedSize(true);
        postsRv.setLayoutManager(llManager);
        searchFab.setOnClickListener(searchListener);
    }

    private final View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Create new search
            Intent intent = new Intent(HomeActivity.this, CreateSearchActivity.class);
            intent.putExtra("sDescription", search);
            startActivity(intent);
        }
    };

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
                    /*assert post != null;
                    if (!ds.child("BlockedUsers").hasChild(myUID) && (post.getpPublicOrPrivate().equals("public") || post.getpCreator().equals(myUID))){
                        postList.add(post);
                    }*/
                    postList.add(post);
                    postsAdaptor = new PostsAdaptor(HomeActivity.this, postList, myUID);
                    postsRv.setAdapter(postsAdaptor);
                    postsAdaptor.setOnItemClickListener(new ViewsClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                            intent.putExtra("pId", postList.get(position).getpId());
                            intent.putExtra("pCreator", postList.get(position).getpCreator());
                            intent.putExtra("nType", DB_POST);
                            startActivity(intent);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    });
                }
                if (postList.isEmpty()){
                    search = "";
                    searchFab.setVisibility(View.VISIBLE);
                }else{
                    searchFab.setVisibility(View.GONE);
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
                        if (post.getpPublicOrPrivate().equals("public") || post.getpCreator().equals(myUID) || !ds.child("BlockedUsers").hasChild(myUID)){
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
                            intent.putExtra("pCreator", postList.get(position).getpCreator());
                            intent.putExtra("nType", DB_POST);
                            intent.putExtra("pTopicGallery", post.getpTopicGallery());
                            intent.putExtra("pTopicComment", post.getpTopicComment());
                            startActivity(intent);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    });
                }
                if (postList.isEmpty()){
                    search = "";
                    searchFab.setVisibility(View.VISIBLE);
                } else{
                    search = searchQuery;
                    searchFab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void initGroupTopic() {

    }
}