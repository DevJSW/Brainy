package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddChannelActivity extends AppCompatActivity {

    String founder_name = "";
    String founder_image = "";
    String user_name = "";
    String group_id = "";
    private EditText inputName;
    private ImageView backBtn;
    private DatabaseReference mDatabaseUserGroups, mDatabaseChannels, mDatabaseUsers, mDatabaseChannelMessages, mDatabaseGroupChatlist, mDatabaseChannelsNames;
    private ProgressBar progressBar;
    private Menu menu;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;
    private StorageReference mStorage;

    CollapsingToolbarLayout collapsingToolbarLayout;
    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST = 1;

    private CircleImageView userAvator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_channel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        Window window = AddChannelActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(AddChannelActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        userAvator = (CircleImageView) findViewById(R.id.user_avator);
        userAvator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        auth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUserGroups = FirebaseDatabase.getInstance().getReference().child("Users_groups");
        mDatabaseChannels = FirebaseDatabase.getInstance().getReference().child("Channels");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        mDatabaseChannelMessages = FirebaseDatabase.getInstance().getReference().child("Group_chats");
        mDatabaseChannelsNames = FirebaseDatabase.getInstance().getReference().child("Channel_names");

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                founder_name = dataSnapshot.child("username").getValue().toString();
                founder_image = dataSnapshot.child("user_image").getValue().toString();
                user_name = dataSnapshot.child("name").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddChannelActivity.this.finish();
            }
        });

        mprogress = new ProgressDialog(this);

        inputName = (EditText) findViewById(R.id.name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        FloatingActionButton next = (FloatingActionButton) findViewById(R.id.fab);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIfChannelNameExists();
            }
        });
    }

    private void checkIfChannelNameExists() {

        mDatabaseChannelsNames.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = inputName.getText().toString().trim();
                if (dataSnapshot.hasChild(name.toUpperCase())) {
                    // use "username" already exists
                    // Let the user know he needs to pick another username.
                    inputName.setError("Channel name already exists");
                } else if (name.contains(" ")){
                    inputName.setError("Name should be one word!");
                } else if (name.matches(".*[!@#$%^&*+=?-].*")) {
                    inputName.setError("Name shouldn't contain special characters");

                } else {
                    initSave();
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initSave() {

        final String name = inputName.getText().toString().trim();

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);

        //final String user_id = auth.getCurrentUser().getUid();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Enter group name!", Toast.LENGTH_SHORT).show();

        } else if (name.length() < 3 || name.length() > 30) {
            System.out.println("Name too short or too long");
            Toast.makeText(getApplicationContext(), "Channel name too short or too long!", Toast.LENGTH_SHORT).show();
        } else {

            //progressBar.setVisibility(View.VISIBLE);
            //create group
            mprogress.setMessage("Creating channel, please wait...");
            mprogress.setCancelable(false);
            mprogress.show();

            if (resultUri != null && resultUri.getLastPathSegment() != null) {
                StorageReference filepath = mStorage.child("Group_images").child(resultUri.getLastPathSegment());
                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        final DatabaseReference newPost = mDatabaseUserGroups.child(auth.getCurrentUser().getUid()).push();
                        final DatabaseReference newPost2 = mDatabaseChannels;

                        /*newPost.child("group_name").setValue(name);
                        newPost.child("group_id").setValue(newPost.getKey());
                        newPost.child("group_image").setValue(downloadUrl.toString());
                        newPost.child("group_type").setValue("CHANNEL");
                        newPost.child("founder_name").setValue(founder_name);
                        newPost.child("founder_id").setValue(auth.getCurrentUser().getUid());
                        newPost.child("created_date").setValue(stringDate);*/

                        Map<String, Object> map5 = new HashMap<>();
                        map5.put("group_name", name);
                        map5.put("group_id", newPost.getKey());
                        map5.put("group_image", downloadUrl.toString());
                        map5.put("group_type", "CHANNEL");
                        map5.put("founder_name", founder_name);
                        map5.put("founder_id", auth.getCurrentUser().getUid());
                        map5.put("created_date", stringDate);
                        newPost.setValue(map5);

                        newPost2.child(newPost.getKey()).child("group_name").setValue(name);
                        newPost2.child(newPost.getKey()).child("group_id").setValue(newPost.getKey());
                        newPost2.child(newPost.getKey()).child("founder_name").setValue(founder_name);
                        newPost2.child(newPost.getKey()).child("group_type").setValue("CHANNEL");
                        newPost2.child(newPost.getKey()).child("group_image").setValue(downloadUrl.toString());
                        newPost2.child(newPost.getKey()).child("founder_id").setValue(auth.getCurrentUser().getUid());
                        newPost2.child(newPost.getKey()).child("created_date").setValue(stringDate);

                        //ADD THIS USER TO PARTICIPANTS IN GROUP
                        /*newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("username").setValue(founder_name);
                        newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("name").setValue(user_name);
                        newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("user_image").setValue(founder_image);
                        newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());*/

                        //SEND ALERT MESSAGE
                        final DatabaseReference newChannelMsgs =  mDatabaseChannelMessages.child(newPost.getKey()).push();
                        //final DatabaseReference newGroupChatlist =  mDatabaseGroupChatlist.child(newPost.getKey());
                        Date date = new Date();
                        final String stringDate = DateFormat.getDateTimeInstance().format(date);
                        final String stringDate2 = DateFormat.getDateInstance().format(date);



//                        Map<String, Object> map2 = new HashMap<>();
//                        map2.put("message", user_name+ " left on "+stringDate2);
//                        map2.put("sender_uid", auth.getCurrentUser().getUid());
//                        map2.put("message_type", "NOTIFICATION");
//                        map2.put("sender_name", user_name);
//                        map2.put("posted_date", System.currentTimeMillis());
//                        map2.put("post_id", newGroupChatlist.getKey());
//                        newGroupChatlist.setValue(map2);


                        mprogress.dismiss();

                        Toast.makeText(AddChannelActivity.this, name + " created successfully!",
                                Toast.LENGTH_LONG).show();
                        AddChannelActivity.this.finish();

                       /* Intent openRead = new Intent(AddGroupActivity.this, AddParticipantsActivity.class);
                        openRead.putExtra("group_id", group_id );
                        openRead.putExtra("group_name", name);
                        openRead.putExtra("group_image", downloadUrl.toString() );
                        openRead.putExtra("created_date", stringDate );
                        openRead.putExtra("group_founder", user_name );
                        startActivity(openRead);*/

                    }
                });
            } else {
                final DatabaseReference newPost = mDatabaseUserGroups.child(auth.getCurrentUser().getUid()).push();
                final DatabaseReference newPost2 = mDatabaseChannels;

                Map<String, Object> map3 = new HashMap<>();
                map3.put("group_name", name);
                map3.put("group_id", newPost.getKey());
                map3.put("group_type", "CHANNEL");
                map3.put("founder_name", founder_name);
                map3.put("group_image", "");
                map3.put("created_date", stringDate);
                map3.put("founder_id", auth.getCurrentUser().getUid());
                newPost.setValue(map3);

                newPost2.child(newPost.getKey()).child("group_name").setValue(name);
                newPost2.child(newPost.getKey()).child("group_id").setValue(newPost.getKey());
                newPost2.child(newPost.getKey()).child("group_image").setValue("");
                newPost2.child(newPost.getKey()).child("founder_name").setValue(founder_name);
                newPost2.child(newPost.getKey()).child("founder_id").setValue(auth.getCurrentUser().getUid());
                newPost2.child(newPost.getKey()).child("created_date").setValue(stringDate);

                //ADD THIS USER TO PARTICIPANTS IN GROUP
                /*newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("username").setValue(founder_name);
                newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("name").setValue(user_name);
                newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("user_image").setValue(founder_image);
                newPost2.child(newPost.getKey()).child("Participants").child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());*/

                //SEND ALERT MESSAGE
                final DatabaseReference newChannelMsgs =  mDatabaseChannelMessages.child(newPost.getKey()).push();
                //final DatabaseReference newGroupChatlist =  mDatabaseGroupChatlist.child(newPost.getKey());
                Date date2 = new Date();
                //final String stringDate = DateFormat.getDateTimeInstance().format(date);
                final String stringDate2 = DateFormat.getDateInstance().format(date2);

                Map<String, Object> map = new HashMap<>();
                map.put("message", user_name+ " created this channel on "+stringDate2);
                map.put("sender_uid", auth.getCurrentUser().getUid());
                map.put("message_type", "NOTIFICATION");
                map.put("sender_name", user_name);
                map.put("posted_date", System.currentTimeMillis());
                map.put("post_id", newChannelMsgs.getKey());
                newChannelMsgs.setValue(map);


//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("message", user_name+ " left on "+stringDate2);
//                map2.put("sender_uid", auth.getCurrentUser().getUid());
//                map2.put("message_type", "NOTIFICATION");
//                map2.put("sender_name", user_name);
//                map2.put("posted_date", System.currentTimeMillis());
//                map2.put("post_id", newGroupChatlist.getKey());
//                newGroupChatlist.setValue(map2);

                mprogress.dismiss();

                Toast.makeText(AddChannelActivity.this, name + " created successfully!",
                        Toast.LENGTH_LONG).show();
                AddChannelActivity.this.finish();

               /* Intent openRead = new Intent(AddGroupActivity.this, AddParticipantsActivity.class);
                openRead.putExtra("group_id", group_id );
                openRead.putExtra("group_name", name);
                openRead.putExtra("group_image", "" );
                openRead.putExtra("created_date", stringDate );
                openRead.putExtra("group_founder", user_name );
                startActivity(openRead);
*/
            }
             //ADD CHANNEL NAME TO DB
            final DatabaseReference newPostChannelName = mDatabaseChannelsNames;
            newPostChannelName.child(name.toUpperCase()).setValue(name);
        }

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
                    .start(AddChannelActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                userAvator.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
