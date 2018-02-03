package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewPhotoActivity extends AppCompatActivity {
    String group_id = null;
    String photo_key = null;

    private ImageView addImage, backBtn;
    private Button uploadBtn;
    private ProgressDialog mProgress;

    private DatabaseReference mDatabaseUsers, mDatabaseGroupChats;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;

    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        Window window = ViewPhotoActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ViewPhotoActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        group_id = getIntent().getStringExtra("group_id");
        photo_key = getIntent().getStringExtra("photo_key");

        addImage = (ImageView) findViewById(R.id.addImage);
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseGroupChats = FirebaseDatabase.getInstance().getReference().child("Group_chats");
        mDatabaseGroupChats.child(group_id).child(photo_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String photo = dataSnapshot.child("photo").getValue().toString();

                Glide.with(ViewPhotoActivity.this)
                        .load(photo)
                        .placeholder(R.drawable.placeholder_image)
                        .into(addImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
