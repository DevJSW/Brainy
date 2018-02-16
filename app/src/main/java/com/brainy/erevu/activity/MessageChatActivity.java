package com.brainy.erevu.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.MessageListAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Chat;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MessageChatActivity extends AppCompatActivity {

    String user_id = null;
    String user_name = null;
    String name = null;
    String user_image = null;
    String currentuser_image = null;
    String currentuser_username = null;
    String currentuser_name = null;
    private FirebaseAuth auth;
    private TextView mNoNotification, currName, currUsername, typingIndicator;
    private DatabaseReference mDatabaseUsers, mDatabaseChats, mDatabaseMessages;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatList;
    MessageListAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;
    private LinearLayout background;

    private ImageView sendBtn, backBtn, userImg, photo, addImage;
    private EditText inputMessage;

    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

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

       /* background = (LinearLayout) findViewById(R.id.root_view);
        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("chat_wallpaper")) {
                    String wallpaper = dataSnapshot.child("chat_wallpaper").getValue().toString();
                    //background.setBackgroundResource();
                    //Glide.with(getApplicationContext()).load(wallpaper).into(background);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


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

        typingIndicator = (TextView) findViewById(R.id.toolbar_typingIndicator);

        inputMessage = (EditText) findViewById(R.id.input_message);
        inputMessage.addTextChangedListener(new TextWatcher() {

            boolean isTyping = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            private Timer timer = new Timer();
            private final long DELAY = 3000;  // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                Log.d("", "");
                if(!isTyping) {
                   // Log.d(TAG, "started typing");
                    // Send notification for start typing event
                    mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("Typing").setValue(true);

                    isTyping = true;
                }
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                isTyping = false;
                               // Log.d(TAG, "stopped typing");
                                //send notification for stopped typing event
                                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("Typing").removeValue();
                            }
                        },
                        DELAY
                );

            }
        });

        sendBtn = (ImageView) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        chatAdapter = new MessageListAdapter(getApplicationContext(), chatList);

        mChatList = (RecyclerView) findViewById(R.id.Chat_list);
        mLinearlayout = new LinearLayoutManager(this);
        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(mLinearlayout);
        mLinearlayout.setStackFromEnd(true);
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

        photo = (ImageView) findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(MessageChatActivity.this, ChatPhotoActivity.class);
                openRead.putExtra("user_id", user_id );
                openRead.putExtra("user_name", user_name);
                openRead.putExtra("user_image", user_image );
                openRead.putExtra("name", name );
                startActivity(openRead);
            }
        });

        chatList.clear();
        isTyping();
        LoadMessage();
    }

    private void isTyping() {
        mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Typing")) {
                    typingIndicator.setVisibility(View.VISIBLE);
                    typingIndicator.setText("is typing...");
                } else {
                    typingIndicator.setVisibility(View.GONE);
                    typingIndicator.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void LoadMessage() {

        Query quizQuery = mDatabaseChats.child(auth.getCurrentUser().getUid()).child(user_id);

        quizQuery.addChildEventListener(new ChildEventListener() {
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
            Toast.makeText(MessageChatActivity.this, "Type something!",Toast.LENGTH_LONG).show();

        } else {

            final DatabaseReference newPost = mDatabaseChats.child(auth.getCurrentUser().getUid()).child(user_id).push();
            final DatabaseReference newPost2 = mDatabaseChats.child(user_id).child(auth.getCurrentUser().getUid()).push();
            final DatabaseReference newPost3 = mDatabaseMessages.child(user_id).child(auth.getCurrentUser().getUid());
            final DatabaseReference newPost4 = mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(user_id);

            //MINE
            Map<String, Object> map = new HashMap<>();
            map.put("message", questionBodyTag);
            map.put("sender_uid", auth.getCurrentUser().getUid());
            map.put("receiver_uid", auth.getCurrentUser().getUid());
            map.put("message_type", "MESSAGE");
            map.put("sender_name", currentuser_name);
            map.put("sender_image", currentuser_image);
            map.put("receiver", user_id);
            map.put("posted_date", System.currentTimeMillis());
            map.put("post_id", newPost.getKey());
            newPost.setValue(map);

            //THERES
            Map<String, Object> map2 = new HashMap<>();
            map2.put("message", questionBodyTag);
            map2.put("sender_uid", auth.getCurrentUser().getUid());
            map2.put("receiver_uid", user_id);
            map2.put("message_type", "MESSAGE");
            map2.put("sender_name", currentuser_name);
            map2.put("sender_image", currentuser_image);
            map2.put("receiver", auth.getCurrentUser().getUid());
            map2.put("read", false);
            map2.put("posted_date", System.currentTimeMillis());
            map2.put("post_id", newPost2.getKey());
            newPost2.setValue(map2);

           //THERE'S
            Map<String, Object> map3 = new HashMap<>();
            map3.put("message", questionBodyTag);
            map3.put("sender_uid", auth.getCurrentUser().getUid());
            map3.put("receiver_uid", auth.getCurrentUser().getUid());
            map3.put("sender_name", currentuser_name);
            map3.put("sender_username", currentuser_username);
            map3.put("sender_image", currentuser_image);
            map3.put("receiver", user_id);
            map3.put("read", false);
            map3.put("posted_date", System.currentTimeMillis());
            map3.put("post_id", newPost3.getKey());
            newPost3.setValue(map3);

            //MINE
            Map<String, Object> map4 = new HashMap<>();
            map4.put("message", questionBodyTag);
            map4.put("sender_uid", user_id);
            map4.put("sender_name", name);
            map4.put("sender_username", user_name);
            map4.put("sender_image", user_image);
            map4.put("read", true);
            map4.put("receiver", auth.getCurrentUser().getUid());
            map4.put("posted_date", System.currentTimeMillis());
            map4.put("post_id", newPost4.getKey());
            newPost4.setValue(map4);

            inputMessage.getText().clear();
            // Toast.makeText(ChatroomActivity.this, "Message posted successfully",Toast.LENGTH_LONG).show();

        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_wallpaper) {
            Intent openRead = new Intent(MessageChatActivity.this, EditWallpaperActivity.class);
            openRead.putExtra("user_id", user_id );
            startActivity(openRead);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
