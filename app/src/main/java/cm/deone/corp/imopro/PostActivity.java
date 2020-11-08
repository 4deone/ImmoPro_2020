package cm.deone.corp.imopro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import cm.deone.corp.imopro.fragments.CommentFragment;
import cm.deone.corp.imopro.fragments.HomeFragment;
import cm.deone.corp.imopro.fragments.NotificationsFragment;
import cm.deone.corp.imopro.models.Post;

public class PostActivity extends AppCompatActivity {

    private String pId;
    private String pCreator;
    private String myUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        checkUser();
        initVues();
    }

    @Override
    protected void onStart() {
        checkUser();
        super.onStart();
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null){
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }else{
            myUID = fUser.getUid();
        }
    }

    private void initVues() {
        pId = getIntent().getStringExtra("pId");
        pCreator = getIntent().getStringExtra("pCreator");
        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.getMenu().clear();
        if (pCreator.equals(myUID)){
            navigationView.inflateMenu(R.menu.nav_post);
        }else{
            navigationView.inflateMenu(R.menu.nav_post_his);
        }
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        pushFragment(new HomeFragment());
    }

    protected void pushFragment(Fragment fragment){
        if (fragment == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString("pId", pId);
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_fragment, fragment, "");
        ft.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.menu_post_home:
                    pushFragment(new HomeFragment());
                    return true;
                case R.id.menu_post_comments:
                    pushFragment(new CommentFragment());
                    return true;
                case R.id.menu_post_notifications:
                    pushFragment(new NotificationsFragment());
                    return true;
                default:
            }
            return false;
        }
    };



}