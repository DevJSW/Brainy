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

public class ChatPhotoActivity extends AppCompatActivity {

    private ImageView addImage, backBtn;
    private ProgressDialog mProgress;

    private Boolean isNetworkAvailable = false;

    String user_id = null;
    String user_name = null;
    String name = null;
    String user_image = null;

    String currentuser_image = null;
    String currentuser_name = null;

    private DatabaseReference mDatabaseUsers, mDatabaseChats, mDatabaseMessages;
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

        Window window = ChatPhotoActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ChatPhotoActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        user_id = getIntent().getStringExtra("user_id");
        user_image = getIntent().getStringExtra("user_image");
        user_name = getIntent().getStringExtra("user_name");
        name = getIntent().getStringExtra("name");

        addImage = (ImageView) findViewById(R.id.addImage);
        mProgress = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseChats = FirebaseDatabase.getInstance().getReference().child("User_chats");
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference().child("Users_messages");
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

                final DatabaseReference newPost = mDatabaseChats.child(auth.getCurrentUser().getUid()).child(user_id).push();
                final DatabaseReference newPost2 = mDatabaseChats.child(user_id).child(auth.getCurrentUser().getUid()).push();
                final DatabaseReference newPost3 = mDatabaseMessages.child(user_id).child(auth.getCurrentUser().getUid());
                final DatabaseReference newPost4 = mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(user_id);

                //MINE
                newPost.child("photo").setValue(downloadUrl.toString());
                newPost.child("message_type").setValue("PHOTO");
                newPost.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                newPost.child("receiver_uid").setValue(auth.getCurrentUser().getUid());
                newPost.child("sender_image").setValue(currentuser_image);
                newPost.child("sender_name").setValue(currentuser_name);
                newPost.child("receiver").setValue(user_id);
                newPost.child("posted_date").setValue(System.currentTimeMillis());
                newPost.child("post_id").setValue(newPost.getKey());

                // THERES
                newPost2.child("photo").setValue(downloadUrl.toString());
                newPost2.child("message_type").setValue("PHOTO");
                newPost2.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                newPost2.child("receiver_uid").setValue(user_id);
                newPost2.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                newPost2.child("sender_image").setValue(currentuser_image);
                newPost2.child("sender_name").setValue(currentuser_name);
                newPost2.child("receiver").setValue(auth.getCurrentUser().getUid());
                newPost2.child("read").setValue(false);
                newPost2.child("posted_date").setValue(System.currentTimeMillis());
                newPost2.child("post_id").setValue(newPost2.getKey());

                //THERE'S
                newPost3.child("message").setValue("Photo");
                newPost3.child("message_type").setValue("PHOTO");
                newPost3.child("sender_image").setValue(currentuser_image);
                newPost3.child("sender_name").setValue(currentuser_name);
                newPost3.child("sender_username").setValue(currentuser_name);
                newPost3.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                newPost3.child("receiver").setValue(user_id);
                newPost3.child("read").setValue(false);
                newPost3.child("posted_date").setValue(System.currentTimeMillis());
                newPost3.child("post_id").setValue(newPost3.getKey());

                //YOU SCREEN ON CHATLIST
                newPost4.child("message").setValue("Photo");
                newPost4.child("message_type").setValue("PHOTO");
                newPost4.child("sender_image").setValue(user_image);
                newPost4.child("sender_name").setValue(name);
                newPost4.child("sender_username").setValue(user_name);
                newPost4.child("sender_uid").setValue(user_id);
                newPost4.child("receiver").setValue(auth.getCurrentUser().getUid());
                newPost4.child("posted_date").setValue(System.currentTimeMillis());
                newPost4.child("post_id").setValue(newPost4.getKey());

                ChatPhotoActivity.this.finish();

                Toast.makeText(ChatPhotoActivity.this, "Photo sent successfully!", Toast.LENGTH_LONG).show();
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
                    .setAspectRatio(10, 10)
                    .start(ChatPhotoActivity.this);
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
