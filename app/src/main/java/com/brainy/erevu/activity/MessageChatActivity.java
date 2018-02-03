package com.brainy.erevu.activity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.MessageListAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageChatActivity extends AppCompatActivity {

    String user_id = null;
    String user_name = null;
    String name = null;
    String user_image = null;
    String currentuser_image = null;
    String currentuser_username = null;
    String currentuser_name = null;
    private FirebaseAuth auth;
    private TextView mNoNotification, currName, currUsername;
    private DatabaseReference mDatabaseUsers, mDatabaseChats, mDatabaseMessages;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatList;
    MessageListAdapter chatAdapter;
    private final List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;

    private ImageView sendBtn, backBtn, userImg, quickShort;
    private EditText inputMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(my_toolbar);

        Window window = MessageChatActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( MessageChatActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        user_id = getIntent().getStringExtra("user_id");
        user_name = getIntent().getStringExtra("username");
        user_image = getIntent().getStringExtra("user_image");
        name = getIntent().getStringExtra("name");

        mDatabaseChats = FirebaseDatabase.getInstance().getReference().child("User_chats");
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference().child("Users_messages");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseChats.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseMessages.keepSynced(true);

        mNoNotification = (TextView) findViewById(R.id.noNotyTxt);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mDatabaseChats.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
                MessageChatActivity.this.finish();
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

        chatAdapter = new MessageListAdapter(MessageChatActivity.this,chatList);

        mChatList = (RecyclerView) findViewById(R.id.Chat_list);
        mLinearlayout = new LinearLayoutManager(MessageChatActivity.this);
        mLinearlayout.setStackFromEnd(true);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(mLinearlayout);
        mChatList.setAdapter(chatAdapter);

        currName = (TextView) findViewById(R.id.toolbar_name);
        currUsername = (TextView) findViewById(R.id.toolbar_username);

        currName.setText(name);
        if (user_name != null)
        currUsername.setText(user_name);

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

        chatList.clear();
        LoadMessage();
    }

    private void LoadMessage() {

        Query quizQuery = mDatabaseChats.child(auth.getCurrentUser().getUid()).child(user_id);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat message = dataSnapshot.getValue(Chat.class);

                chatList.add(message);
                chatAdapter.notifyItemInserted(0);
                chatAdapter.notifyItemRangeChanged(0,chatList.size());
                chatAdapter.notifyItemInserted(-1);
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
            Toast.makeText(MessageChatActivity.this, "Type something!",Toast.LENGTH_LONG).show();

        } else {

            final DatabaseReference newPost = mDatabaseChats.child(auth.getCurrentUser().getUid()).child(user_id).push();
            final DatabaseReference newPost2 = mDatabaseChats.child(user_id).child(auth.getCurrentUser().getUid()).push();
            final DatabaseReference newPost3 = mDatabaseMessages.child(user_id).child(auth.getCurrentUser().getUid());
            final DatabaseReference newPost4 = mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(user_id);

            //MINE
            newPost.child("message").setValue(questionBodyTag);
            newPost.child("sender_uid").setValue(auth.getCurrentUser().getUid());
            newPost.child("receiver_uid").setValue(auth.getCurrentUser().getUid());
            newPost.child("message_type").setValue("MESSAGE");
            newPost.child("sender_image").setValue(currentuser_image);
            newPost.child("sender_name").setValue(currentuser_name);
            newPost.child("receiver").setValue(user_id);
            newPost.child("posted_date").setValue(System.currentTimeMillis());
            newPost.child("post_id").setValue(newPost.getKey());

            // THERES
            newPost2.child("message").setValue(questionBodyTag);
            newPost2.child("sender_uid").setValue(auth.getCurrentUser().getUid());
            newPost2.child("receiver_uid").setValue(user_id);
            newPost2.child("message_type").setValue("MESSAGE");
            newPost2.child("sender_uid").setValue(auth.getCurrentUser().getUid());
            newPost2.child("sender_image").setValue(currentuser_image);
            newPost2.child("sender_name").setValue(currentuser_name);
            newPost2.child("receiver").setValue(auth.getCurrentUser().getUid());
            newPost2.child("read").setValue(false);
            newPost2.child("posted_date").setValue(System.currentTimeMillis());
            newPost2.child("post_id").setValue(newPost2.getKey());

            //THERE'S
            newPost3.child("message").setValue(questionBodyTag);
            newPost3.child("sender_image").setValue(currentuser_image);
            newPost3.child("sender_name").setValue(currentuser_name);
            newPost3.child("sender_username").setValue(currentuser_username);
            newPost3.child("sender_uid").setValue(auth.getCurrentUser().getUid());
            newPost3.child("receiver").setValue(user_id);
            newPost3.child("read").setValue(false);
            newPost3.child("posted_date").setValue(System.currentTimeMillis());
            newPost3.child("post_id").setValue(newPost3.getKey());

            //YOU SCREEN ON CHATLIST
            newPost4.child("message").setValue(questionBodyTag);
            newPost4.child("sender_image").setValue(user_image);
            newPost4.child("sender_name").setValue(name);
            newPost4.child("sender_username").setValue(user_name);
            newPost4.child("sender_uid").setValue(user_id);
            newPost4.child("receiver").setValue(auth.getCurrentUser().getUid());
            newPost4.child("posted_date").setValue(System.currentTimeMillis());
            newPost4.child("post_id").setValue(newPost4.getKey());

            inputMessage.getText().clear();
            // Toast.makeText(ChatroomActivity.this, "Message posted successfully",Toast.LENGTH_LONG).show();

        }
    }


}
