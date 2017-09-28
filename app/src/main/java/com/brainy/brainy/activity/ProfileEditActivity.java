package com.brainy.brainy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.brainy.brainy.R;
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

public class ProfileEditActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView userImg;
    private EditText inputName, inputBio;
    private Button saveBtn;
    private ProgressDialog mprogress;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

                String bio = dataSnapshot.child("bio").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                    Picasso.with(ProfileEditActivity.this)
                        .load(image)
                        .placeholder(R.drawable.placeholder_image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(userImg);

                inputName.setText(name);
                inputBio.setText(bio);


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

            if (mImageUri.getLastPathSegment() != null) {
                StorageReference filepath = mStorage.child("Profile_images").child(mImageUri.getLastPathSegment());

                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("user_image").setValue(downloadUrl.toString());

                        mprogress.dismiss();

                        Toast.makeText(ProfileEditActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
                    }
                });

            } else {

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
            userImg.setImageURI(mImageUri);

        }
    }
}
