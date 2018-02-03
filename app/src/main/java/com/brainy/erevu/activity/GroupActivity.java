package com.brainy.erevu.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.Adapters.GroupAdapter;
import com.brainy.erevu.Adapters.MessageListAdapter;
import com.brainy.erevu.Pojos.Chat;
import com.brainy.erevu.Pojos.Group;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Users;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupActivity extends AppCompatActivity {

    String group_id = "";
    private ImageView backBtn, writeMessage;
    private FirebaseAuth auth;
    private TextView mNoNotification;
    private DatabaseReference mDatabaseUsers, mDatabaseGroups, mDatabaseGroupChatlist;
    private RecyclerView mGroupList;
    GroupAdapter chatAdapter;
    private final List<Group> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // QuizKey = getIntent().getExtras().getString("question_id");

        Window window = GroupActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( GroupActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupActivity.this, AddGroupActivity.class));
            }
        });

        mDatabaseGroups = FirebaseDatabase.getInstance().getReference().child("Users_groups");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");

        // SYNC DATABASE
        mDatabaseGroups.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        mNoNotification = (TextView) findViewById(R.id.noGrouptv);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mDatabaseGroups.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoNotification.setVisibility(View.VISIBLE);
                        mNoNotification.setText("Your group list is empty.");
                    } else {
                        mNoNotification.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {

        }

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupActivity.this.finish();
            }
        });

        mGroupList = (RecyclerView) findViewById(R.id.group_list);
        mGroupList.setHasFixedSize(true);
        mGroupList.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new GroupAdapter(this,chatList);
        mGroupList.setAdapter(chatAdapter);

        loadGroups();
        //LoadMessage();
    }

    private void LoadMessage() {

        mDatabaseGroupChatlist.child(group_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Group message = dataSnapshot.getValue(Group.class);

                chatList.add(message);
                chatAdapter.notifyDataSetChanged();



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadGroups() {

        FirebaseRecyclerAdapter<Group, GroupActivity.GroupsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, GroupActivity.GroupsViewHolder>(

                Group.class,
                R.layout.group_item,
                GroupActivity.GroupsViewHolder.class,
                mDatabaseGroups.child(auth.getCurrentUser().getUid())

        ) {
            @Override
            protected void populateViewHolder(GroupActivity.GroupsViewHolder viewHolder, final Group model, int position) {

                group_id = model.getGroup_id();

                viewHolder.setDetails(getApplicationContext(), model.getGroup_name(), model.getMessage(), model.getGroup_image());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openRead = new Intent(GroupActivity.this, GroupChatroomActivity.class);
                        openRead.putExtra("group_id", model.getGroup_id() );
                        openRead.putExtra("group_name", model.getGroup_name());
                        openRead.putExtra("group_image", model.getGroup_image() );
                        startActivity(openRead);
                    }
                });
            }
        };

        mGroupList.setAdapter(firebaseRecyclerAdapter);

    }

    // View Holder Class

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        DatabaseReference mDatabaseGroupChatlist;

        public GroupsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        }

        public void setDetails(Context ctx, String groupName, String groupMessage, String groupImage){

            TextView group_name = (TextView) mView.findViewById(R.id.group_name);
            TextView group_message = (TextView) mView.findViewById(R.id.post_message);
            CircleImageView user_image = (CircleImageView) mView.findViewById(R.id.post_image);

            group_name.setText(groupName);
            group_message.setText(groupMessage);

            Glide.with(ctx)
                    .load(groupImage)
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(user_image);

        }


    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);
        //this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_newgroup) {
            startActivity(new Intent(GroupActivity.this, AddGroupActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
