package com.brainy.erevu.activity;

import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.MessageListAdapter;
import com.brainy.erevu.Pojos.Chat;
import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatroomActivity extends AppCompatActivity {

    String group_id = null;
    String group_name = null;
    String name = null;
    String group_image = null;
    String currentuser_image = null;
    String currentuser_username = null;
    String currentuser_name = null;
    private FirebaseAuth auth;
    private TextView mNoNotification, groupName;
    private DatabaseReference mDatabaseUsers, mDatabaseUsersGroups, mDatabaseGroups, mDatabaseGroupChats, mDatabaseGroupChatlist;
    private LinearLayout open_view;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatList;
    MessageListAdapter chatAdapter;
    private final List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;

    private ImageView sendBtn, backBtn, photo;
    private CircleImageView toolbar_groupimage;
    private EditText inputMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chatroom);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(my_toolbar);

        Window window = GroupChatroomActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( GroupChatroomActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        group_id = getIntent().getStringExtra("group_id");
        group_name = getIntent().getStringExtra("group_name");
        group_image = getIntent().getStringExtra("group_image");
        name = getIntent().getStringExtra("name");

        mDatabaseUsersGroups = FirebaseDatabase.getInstance().getReference().child("Users_Groups");
        mDatabaseGroups = FirebaseDatabase.getInstance().getReference().child("Groups");
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        mDatabaseGroupChats = FirebaseDatabase.getInstance().getReference().child("Group_chats");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUsersGroups.keepSynced(true);
        mDatabaseGroupChats.keepSynced(true);
        mDatabaseGroupChatlist.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseGroups.keepSynced(true);

        mNoNotification = (TextView) findViewById(R.id.noNotyTxt);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mDatabaseGroupChats.child(group_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoNotification.setVisibility(View.VISIBLE);
                        mNoNotification.setText("No Chats!");
                    } else {
                        mNoNotification.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            mNoNotification.setVisibility(View.VISIBLE);
            mNoNotification.setText("No Chats!");
        }

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupChatroomActivity.this.finish();
            }
        });

        inputMessage = (EditText) findViewById(R.id.input_message);

        sendBtn = (ImageView) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        chatAdapter = new MessageListAdapter(GroupChatroomActivity.this, (ArrayList<Chat>) chatList);

        mChatList = (RecyclerView) findViewById(R.id.Chat_list);
        mLinearlayout = new LinearLayoutManager(GroupChatroomActivity.this);
        mLinearlayout.setStackFromEnd(true);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(mLinearlayout);
        mChatList.setAdapter(chatAdapter);

        groupName = (TextView) findViewById(R.id.toolbar_groupname);

        if (groupName != null)
            groupName.setText(group_name);

        toolbar_groupimage = (CircleImageView) findViewById(R.id.toolbar_groupimage);
       /* Glide.with(getApplicationContext())
                .load(group_image)
                .placeholder(R.drawable.placeholder_image)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(toolbar_groupimage);*/

        Picasso.with(getApplicationContext()).load(group_image).placeholder(R.drawable.placeholder_image).into(toolbar_groupimage);

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentuser_name = dataSnapshot.child("name").getValue().toString();
                currentuser_image = dataSnapshot.child("user_image").getValue().toString();
                if (dataSnapshot.hasChild("username"))
                    currentuser_username = dataSnapshot.child("username").getValue().toString();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        open_view = (LinearLayout) findViewById(R.id.open_view);
        open_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(GroupChatroomActivity.this, GroupProfileActivity.class);
                openRead.putExtra("group_id", group_id );
                openRead.putExtra("group_name", group_name);
                openRead.putExtra("group_image", group_image );
                startActivity(openRead);
            }
        });

        photo = (ImageView) findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(GroupChatroomActivity.this, PhotoActivity.class);
                openRead.putExtra("group_id", group_id );
                openRead.putExtra("group_name", group_name);
                openRead.putExtra("group_image", group_image );
                startActivity(openRead);
            }
        });

        chatList.clear();
        LoadMessage();

    }

    private void LoadMessage() {

        mDatabaseGroupChats.child(group_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat message = dataSnapshot.getValue(Chat.class);

                chatList.add(message);
                chatAdapter.notifyDataSetChanged();
                mLinearlayout.setStackFromEnd(true);
                mSwipeRefreshLayout.setRefreshing(false);
                mChatList.scrollToPosition(chatList.size()-1);

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

    private void startPosting() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);
        final String stringDate2 = DateFormat.getDateInstance().format(date);

        final String questionBodyTag = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(questionBodyTag)) {
            Toast.makeText(GroupChatroomActivity.this, "Type something!",Toast.LENGTH_LONG).show();

        } else {

            final DatabaseReference newPost =  mDatabaseGroupChats.child(group_id).push();
            final DatabaseReference newPost2 =  mDatabaseGroupChatlist.child(group_id);

            Map<String, Object> map = new HashMap<>();
            map.put("message", questionBodyTag);
            map.put("sender_uid", auth.getCurrentUser().getUid());
            map.put("message_type", "MESSAGE");
            map.put("sender_name", currentuser_name);
            map.put("sender_image", currentuser_image);
            map.put("posted_date", System.currentTimeMillis());
            map.put("post_id", newPost.getKey());
            newPost.setValue(map);

           /* Map<String, Object> map2 = new HashMap<>();
            map2.put("message", questionBodyTag);
            map2.put("group_image", group_image);
            map2.put("message_type", "MESSAGE");
            map2.put("group_name", group_name);
            map2.put("group_id", group_id);
            map2.put("read", false);
            map2.put("posted_date", System.currentTimeMillis());
            map2.put("post_id", newPost2.getKey());
            newPost2.setValue(map2);
*/
            inputMessage.getText().clear();

        }
    }
}
