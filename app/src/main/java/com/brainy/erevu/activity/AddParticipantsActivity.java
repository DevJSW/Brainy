package com.brainy.erevu.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.ParticipantsAdapter;
import com.brainy.erevu.Pojos.Users;
import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddParticipantsActivity extends AppCompatActivity {

    String group_id = "";
    String group_name = "";
    String group_image = "";
    String group_founder = "";
    String created_date = "";
    String currentuser_name = "";

    private EditText mSearchInput;
    private ImageView backBtn;
    private RecyclerView mResultList, mParticipantsList;
    private FirebaseAuth auth;

    private DatabaseReference mUserDatabase, mDatabaseGroups, mDatabaseUsersGroups, mDatabaseGroupChats, mDatabaseGroupChatlist;

    ParticipantsAdapter participantsAdapter;
    private final List<Users> usersList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);
        Window window = AddParticipantsActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( AddParticipantsActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        group_id = getIntent().getStringExtra("group_id");
        group_image = getIntent().getStringExtra("group_image");
        group_name = getIntent().getStringExtra("group_name");
        group_founder = getIntent().getStringExtra("group_founder");
        created_date = getIntent().getStringExtra("created_date");
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        mDatabaseGroupChats = FirebaseDatabase.getInstance().getReference().child("Group_chats");
        mDatabaseUsersGroups = FirebaseDatabase.getInstance().getReference("Users_groups");
        if (group_id != null) {
            mDatabaseGroups = FirebaseDatabase.getInstance().getReference("Groups").child(group_id);
        } else {
            AddParticipantsActivity.this.finish();
        }

        mSearchInput = (EditText) findViewById(R.id.search_edit);
        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mParticipantsList = (RecyclerView) findViewById(R.id.participants_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = mSearchInput.getText().toString();
                if (searchText.length() > 1) {
                    firebaseUserSearch(searchText);
                } else {

                }
            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddParticipantsActivity.this.finish();
            }
        });

        participantsAdapter = new ParticipantsAdapter(getApplicationContext(), usersList);
        participantsAdapter = new ParticipantsAdapter(getApplicationContext(), usersList);

        if (group_id != null)
        loadParticipants();

        final LinearLayoutManager usersLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mParticipantsList.setLayoutManager(usersLayoutManager);

        mUserDatabase.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentuser_name = dataSnapshot.child("username").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadParticipants() {

        FirebaseRecyclerAdapter<Users, AddParticipantsActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, AddParticipantsActivity.UsersViewHolder>(

                Users.class,
                R.layout.users_grid,
                AddParticipantsActivity.UsersViewHolder.class,
                mDatabaseGroups.child("Participants")

        ) {
            @Override
            protected void populateViewHolder(AddParticipantsActivity.UsersViewHolder viewHolder, final Users model, int position) {
                final String user_id = getRef(position).getKey();

                viewHolder.setDetails(getApplicationContext(), model.getName(), model.getUsername(), model.getUser_image());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        };

        mParticipantsList.setAdapter(firebaseRecyclerAdapter);

    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = mUserDatabase.orderByChild("username").startAt("@"+searchText).endAt("@"+searchText + "\uf8ff");

        FirebaseRecyclerAdapter<Users, AddParticipantsActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, AddParticipantsActivity.UsersViewHolder>(

                Users.class,
                R.layout.user_search_item,
                AddParticipantsActivity.UsersViewHolder.class,
                firebaseSearchQuery

        ) {
            @Override
            protected void populateViewHolder(AddParticipantsActivity.UsersViewHolder viewHolder, final Users model, int position) {
                final String user_id = getRef(position).getKey();

                viewHolder.setDetails(getApplicationContext(), model.getName(), model.getUsername(), model.getUser_image());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //ADD USER TO PARTICIPANTS LISTS
                        final Context context = AddParticipantsActivity.this;

                        // custom dialog
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.group_dialog);
                        dialog.setTitle("Options");
                        dialog.show();

                        RelativeLayout addUserToGroup = (RelativeLayout) dialog.findViewById(R.id.addUserToGroup);
                        addUserToGroup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //CHECK IF USER IF ALREADY A PARTICIPANT
                                mDatabaseGroups.child("Participants").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(user_id)) {
                                            Toast.makeText(AddParticipantsActivity.this, " This user is already a participant!",
                                                    Toast.LENGTH_LONG).show();
                                        } else if (!dataSnapshot.hasChild(user_id)){
                                            mUserDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String username = dataSnapshot.child("username").getValue().toString();
                                                    String name = dataSnapshot.child("name").getValue().toString();
                                                    String user_image = dataSnapshot.child("user_image").getValue().toString();

                                                    DatabaseReference newPost = mDatabaseGroups.child("Participants").child(user_id);
                                                    DatabaseReference newPost2 = mDatabaseUsersGroups.child(user_id).child(group_id);

                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("username", username);
                                                    map.put("name", name);
                                                    map.put("user_image", user_image);
                                                    map.put("uid", user_id);
                                                    newPost.setValue(map);


                                                    Map<String, Object> map2 = new HashMap<>();
                                                    map2.put("group_name", group_name);
                                                    map2.put("group_id", group_id);
                                                    map2.put("group_image", group_image);
                                                    map2.put("founder_name", group_founder);
                                                    map2.put("created_date", created_date);
                                                    newPost2.setValue(map2);

                                                    //SEND ALERT MESSAGE
                                                    final DatabaseReference newGroupChats =  mDatabaseGroupChats.child(group_id).push();
                                                    final DatabaseReference newGroupChatlist =  mDatabaseGroupChatlist.child(group_id);
                                                    Date date = new Date();
                                                    final String stringDate = DateFormat.getDateTimeInstance().format(date);
                                                    final String stringDate2 = DateFormat.getDateInstance().format(date);


                                                    Map<String, Object> map3 = new HashMap<>();
                                                    map3.put("message", currentuser_name+ " added "+username +" on "+stringDate2);
                                                    map3.put("sender_uid", auth.getCurrentUser().getUid());
                                                    map3.put("message_type", "NOTIFICATION");
                                                    map3.put("sender_name", currentuser_name);
                                                    map3.put("posted_date", System.currentTimeMillis());
                                                    map3.put("post_id", newGroupChats.getKey());
                                                    newGroupChats.setValue(map3);

                                                    /*Map<String, Object> map4 = new HashMap<>();
                                                    map4.put("message", currentuser_name+ " added "+username +" on "+stringDate2);
                                                    map4.put("sender_uid", auth.getCurrentUser().getUid());
                                                    map4.put("message_type", "NOTIFICATION");
                                                    map4.put("sender_name", currentuser_name);
                                                    map4.put("posted_date", System.currentTimeMillis());
                                                    map4.put("post_id", newGroupChatlist.getKey());
                                                    newGroupChatlist.setValue(map4);*/

                                                    Toast.makeText(AddParticipantsActivity.this, username + " added successfully!",
                                                            Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                dialog.dismiss();
                            }
                        });

                    }
                });

                viewHolder.user_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openRead = new Intent(AddParticipantsActivity.this, ViewUserProfileActivity.class);
                        openRead.putExtra("user_id", user_id );
                        startActivity(openRead);
                    }
                });
            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }

    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView user_image;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            user_image = (CircleImageView) mView.findViewById(R.id.post_image);
        }

        public void setDetails(Context ctx, String userName, String userStatus, String userImage){

            TextView user_name = (TextView) mView.findViewById(R.id.post_name);
            TextView user_status = (TextView) mView.findViewById(R.id.post_username);

            user_name.setText(userName);
            user_status.setText(userStatus);

            Glide.with(ctx).load(userImage).placeholder(R.drawable.placeholder_image).into(user_image);


        }

    }
}
