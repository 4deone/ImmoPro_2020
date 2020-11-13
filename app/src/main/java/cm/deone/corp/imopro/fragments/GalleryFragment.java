package cm.deone.corp.imopro.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import cm.deone.corp.imopro.CreateGalleryActivity;
import cm.deone.corp.imopro.MainActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.adapter.GalleryAdaptor;
import cm.deone.corp.imopro.models.Gallery;
import cm.deone.corp.imopro.outils.ViewsClickListener;

public class GalleryFragment extends Fragment {

    private String pId;
    private String pCreator;
    private String myUID;
    private View view;

    private List<Gallery> galleryList;
    private RecyclerView postImagesRv;
    private GalleryAdaptor galleryAdaptor;

    private Toolbar toolbar;
    private DatabaseReference reference;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM = "pId";
    private static final String ARG_PARAM1 = "pCreator";


    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            pId = getArguments().getString(ARG_PARAM);
            pCreator = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_gallery, container, false);
        checkUsers();
        initVues();
        getGallery();
        return view;
    }

    @Override
    public void onStart() {
        checkUsers();
        getGallery();
        super.onStart();
    }

    @Override
    public void onResume() {
        checkUsers();
        getGallery();
        super.onResume();
    }

    private void checkUsers() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null){
            myUID = fUser.getUid();
        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    private void initVues() {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Gallery - Immopro");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        postImagesRv = view.findViewById(R.id.postImagesRv);
        reference = FirebaseDatabase.getInstance().getReference("Gallery");
    }

    private void getGallery() {
        galleryList = new ArrayList<>();
        DatabaseReference ref = reference.child(pId);
        ref.addValueEventListener(valPostGallery);
    }

    private final ValueEventListener valPostGallery = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            galleryList.clear();
            for (DataSnapshot ds : snapshot.getChildren()){
                Gallery gallery = ds.getValue(Gallery.class);
                galleryList.add(gallery);
                galleryAdaptor = new GalleryAdaptor(getActivity(), galleryList);
                postImagesRv.setAdapter(galleryAdaptor);
                galleryAdaptor.setOnItemClickListener(new ViewsClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                });
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gallery, menu);
        MenuItem addImageItem = menu.findItem(R.id.menu_add_gallery);
        if (pCreator.equals(myUID)){
            addImageItem.setVisible(true);
        }else{
            addImageItem.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_gallery) {
            Intent intent = new Intent(getActivity(), CreateGalleryActivity.class);
            intent.putExtra("pId", pId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}