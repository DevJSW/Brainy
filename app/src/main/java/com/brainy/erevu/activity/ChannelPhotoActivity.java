package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class ChannelPhotoActivity extends AppCompatActivity {

    private ImageView addImage, backBtn;
    private ProgressDialog mProgress;

    private Boolean isNetworkAvailable = false;

    String group_id = null;
    String group_name = null;
    String name = null;
    String group_image = null;

    String currentuser_image = null;
    String currentuser_name = null;

    private DatabaseReference mDatabaseUsers, mDatabaseGroupChats, mDatabaseGroupChatlist;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth auth;
    private StorageReference mStorage;

    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Window window = ChannelPhotoActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ChannelPhotoActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        group_id = getIntent().getStringExtra("group_id");
        group_name = getIntent().getStringExtra("group_name");
        group_image = getIntent().getStringExtra("group_image");
        name = getIntent().getStringExtra("name");

        addImage = (ImageView) findViewById(R.id.addImage);
        mProgress = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        mDatabaseGroupChats = FirebaseDatabase.getInstance().getReference().child("Group_chats");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentuser_name = dataSnapshot.child("username").getValue().toString();
                currentuser_image = dataSnapshot.child("user_image").getValue().toString();
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

        selectPhoto();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.uploadBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intSave();
            }
        });
    }

    private void selectPhoto() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void intSave() {

        mProgress.setMessage("Uploading photo, please wait...");
        mProgress.setCancelable(false);
        mProgress.show();

        StorageReference filepath = mStorage.child("question_images").child(resultUri.getLastPathSegment());

        filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                final DatabaseReference newPost =  mDatabaseGroupChats.child(group_id).push();
                final DatabaseReference newPost2 =  mDatabaseGroupChatlist.child(group_id);

                //MINE
                Map<String, Object> map = new HashMap<>();
                map.put("photo", downloadUrl.toString());
                map.put("sender_uid", "#"+auth.getCurrentUser().getUid());
                map.put("message_type", "PHOTO");
                map.put("sender_name", group_name);
                map.put("sender_image", group_image);
                map.put("read", true);
                map.put("group_id", group_id);
                map.put("posted_date", System.currentTimeMillis());
                map.put("post_id", newPost.getKey());
                newPost.setValue(map);

                /*//THERE'S
                Map<String, Object> map2 = new HashMap<>();
                map2.put("message", "Photo");
                map2.put("group_image", group_image);
                map2.put("message_type", "PHOTO");
                map2.put("group_name", group_name);
                map2.put("group_id", group_id);
                map2.put("read", false);
                map2.put("posted_date", System.currentTimeMillis());
                map2.put("post_id", newPost2.getKey());
                newPost2.setValue(map2);*/

                ChannelPhotoActivity.this.finish();

                Toast.makeText(ChannelPhotoActivity.this, "Photo sent successfully!", Toast.LENGTH_LONG).show();
                finish();

            }


        });
    }


    @Override
    protected void onResume() {

        /*if (resultUri == null) {
            finish();
        }*/

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setAspectRatio(100, 100)
                    .start(ChannelPhotoActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                addImage.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}

