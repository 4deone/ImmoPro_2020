package cm.deone.corp.imopro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PhotoActivity extends AppCompatActivity {

    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private String gImage;
    private String gDescription;

    private ImageView itemGalleryIv;
    private TextView descriptionTv;
    private ProgressBar loadingPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        checkUser();
        initVues();
        setItemImage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            itemGalleryIv.setScaleX(mScaleFactor);
            itemGalleryIv.setScaleY(mScaleFactor);
            return true;
        }
    }

    private void checkUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null){
            startActivity(new Intent(PhotoActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initVues() {
        gImage = getIntent().getStringExtra("gImage");
        gDescription = getIntent().getStringExtra("gDescription");

        itemGalleryIv = findViewById(R.id.itemGalleryIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        loadingPb =  new ProgressBar(this);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    private void setItemImage() {

        descriptionTv.setText(gDescription);

        try {
            Picasso.get().load(gImage).placeholder(R.drawable.ic_post).into(itemGalleryIv, new Callback() {
                @Override
                public void onSuccess() {
                    loadingPb.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Gallery_Adaptor", e.getMessage());
                }
            });
        }catch(Exception e){
            Picasso.get().load(R.drawable.ic_post).into(itemGalleryIv, new Callback() {
                @Override
                public void onSuccess() {
                    loadingPb.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Gallery_Adaptor", e.getMessage());
                }
            });
        }

    }
}