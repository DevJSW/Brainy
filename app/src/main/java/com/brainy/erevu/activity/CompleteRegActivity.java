package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Date;

public class CompleteRegActivity extends AppCompatActivity {

    String name = null;
    String username = null;
    String userImage = "";
    private EditText inputName,inputUsername;
    private Button btnSignIn;
    private DatabaseReference mDatabaseUsers, mDatabaseUsernames;
    private ImageView userImg;
    private ProgressBar progressBar;
    private Menu menu;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_reg);

        Window window = CompleteRegActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( CompleteRegActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsernames = FirebaseDatabase.getInstance().getReference().child("Usernames");
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (auth.getCurrentUser() != null) {
                    if (!dataSnapshot.hasChild(auth.getCurrentUser().getUid())) {

                        Intent cardonClick = new Intent(CompleteRegActivity.this, MainActivity.class);
                        cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(cardonClick);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mStorage = FirebaseStorage.getInstance().getReference();
        mprogress = new ProgressDialog(this);
        btnSignIn = (Button) findViewById(R.id.btn_login);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSave();
            }
        });

        inputName = (EditText) findViewById(R.id.name);
        inputUsername = (EditText) findViewById(R.id.username);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        userImg = (ImageView) findViewById(R.id.user_image);

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("name")) {
                    name = dataSnapshot.child("name").getValue().toString();
                    inputName.setText(name);
                }

                if (dataSnapshot.hasChild("user_image")) {
                    userImage = dataSnapshot.child("user_image").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(userImage)
                            .placeholder(R.drawable.placeholder_image)
                            .into(userImg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void initSave() {


        final String name = inputName.getText().toString().trim();
        username = inputUsername.getText().toString().trim();

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);

        //final String user_id = auth.getCurrentUser().getUid();

        // if you don't care why it fails and only want to know if valid or not


        if (TextUtils.isEmpty(name)) {
            inputName.setError("Enter your name!");
            //Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(username)) {
            inputUsername.setError("Enter your username!");
        }
        else if (username.length() < 3 || username.length() >15 ){
            System.out.println("Name too short or too long");
            inputUsername.setError("Name too short or too long");
        }
        else if (username.contains(" ")) {
            inputUsername.setError("Username should be one word e.g JohnDoe instead of John Doe.");
            //Toast.makeText(getApplicationContext(), "Username should be one word e.g JohnDoe instead of John Doe!", Toast.LENGTH_SHORT).show();
        }
        else if (username.matches(".*[!@#$%^&*+=?-].*")) {
            inputUsername.setError("Username should not contain special chars e.g [! @ # $ % ^ & * + = ? -].");
            //Toast.makeText(getApplicationContext(), "Username should be one word e.g JohnDoe instead of John Doe!", Toast.LENGTH_SHORT).show();
        }
        else {

            //CHECK IF USERNAME EXISTS
            mDatabaseUsernames.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(username.toLowerCase())) {
                            // use "username" already exists
                            // Let the user know he needs to pick another username.
                            inputUsername.setError("Username already exists");
                        } else {
                            //inputUsername.setError("cool");
                            mprogress.setMessage("Loading account, please wait...");
                            mprogress.show();

                            final DatabaseReference newPost = mDatabaseUsers;
                            final DatabaseReference newPostUserName = mDatabaseUsernames;
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            newPost.child(auth.getCurrentUser().getUid()).child("date").setValue(stringDate);
                            newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());

                            newPost.child(auth.getCurrentUser().getUid()).child("name").setValue(name);
                            newPost.child(auth.getCurrentUser().getUid()).child("username").setValue("@"+username);
                            newPost.child(auth.getCurrentUser().getUid()).child("user_image").setValue(userImage);
                            newPost.child(auth.getCurrentUser().getUid()).child("joined_date").setValue(stringDate);
                            newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
                            newPost.child(auth.getCurrentUser().getUid()).child("user_email").setValue(auth.getCurrentUser().getEmail());

                            newPost.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
                            newPost.child(auth.getCurrentUser().getUid()).child("points_earned").setValue(10);
                            newPost.child(auth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

                            //ADD USERNAME TO DB
                            newPostUserName.child(username.toLowerCase()).setValue(username);

                            Toast.makeText(CompleteRegActivity.this, "Account setup finished successfully!",
                                    Toast.LENGTH_LONG).show();

                            CompleteRegActivity.this.finish();
                            Intent cardonClick = new Intent(CompleteRegActivity.this, MainActivity.class);
                            cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(cardonClick);
                        }
                    }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


    }


}
