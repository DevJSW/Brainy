package com.brainy.brainy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.brainy.brainy.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileEditActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView userImg;
    private EditText inputName, inputBio;
    private Button saveBtn;
    private ProgressDialog mprogress;
    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        Window window = ProfileEditActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ProfileEditActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        userImg = (ImageView) findViewById(R.id.userimg);
        inputName = (EditText) findViewById(R.id.input_name);
        inputBio = (EditText) findViewById(R.id.input_bio);
        saveBtn = (Button) findViewById(R.id.save_btn);
        mprogress = new ProgressDialog(this);
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               /* if (dataSnapshot.hasChild("status") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {*/

               if (dataSnapshot.hasChild("bio")) {
                   String bio = dataSnapshot.child("bio").getValue().toString();
                   inputBio.setText(bio);
               }
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                inputName.setText(name);

               /* Glide.with(getApplicationContext())
                        .load(image)
                        .placeholder(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(userImg);*/

                Glide.with(getApplicationContext())
                        .load(image).asBitmap()
                        .placeholder(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .centerCrop()
                        .into(new BitmapImageViewTarget(userImg) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                userImg.setImageDrawable(circularBitmapDrawable);
                            }
                        });

              /*  } else {}*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSave();
            }
        });

    }

    private void initSave() {

        if (inputName == null) {

            Toast.makeText(ProfileEditActivity.this, "Please Enter your name", Toast.LENGTH_LONG);

        } else {

            mprogress.setMessage("Saving, please wait...");
            mprogress.show();

            final String name = inputName.getText().toString();
            final String bio = inputBio.getText().toString();

            if (name.equals("")) {
                Toast.makeText(ProfileEditActivity.this, "Enter name!", Toast.LENGTH_LONG).show();
            }
            else if (resultUri != null && resultUri.getLastPathSegment() != null) {
                StorageReference filepath = mStorage.child("Profile_images").child(resultUri.getLastPathSegment());

                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("user_image").setValue(downloadUrl.toString());

                        mprogress.dismiss();

                        Toast.makeText(ProfileEditActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
                    }
                });

                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
                if (bio != null)
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("bio").setValue(bio);

            } else if (bio != null){

                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("bio").setValue(bio);
                //   mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("user_image").setValue(downloadUrl.toString());

                mprogress.dismiss();

                Toast.makeText(ProfileEditActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(ProfileEditActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                userImg.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
