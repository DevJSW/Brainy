package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.ChatAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.data.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatroomActivity extends AppCompatActivity {

    private static final String TAG = ChatroomActivity.class.getSimpleName();
    private ImageView mSendBtn;

    String name = null;
    private LinearLayoutManager mLayoutManager;
    ImageView emojiImageView;
    View rootView;
    private StorageReference mStorage;

    private DatabaseReference mDatabaseDiscussForum, mDatabaseUsers, mDatabaseForumParticipants, mDatabaseForumNotifications, mDatabaseQuestions;
    private EditText mMessageInput;
    private Button mSubmit;
    private FirebaseAuth auth;
    String QuizKey = null;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatList;
    ChatAdapter chatAdapter;
    private final List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;


    /** Called when the activity is first created. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.mCustomToolbarHash);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //keep layout on top of keyboard

        Window window = ChatroomActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ChatroomActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // Font path
        final Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        mStorage = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(my_toolbar);
        rootView = findViewById(R.id.root_view);
        QuizKey = getIntent().getStringExtra("question_id");
        auth = FirebaseAuth.getInstance();
        mDatabaseDiscussForum = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");
        mDatabaseForumNotifications = FirebaseDatabase.getInstance().getReference().child("Forum_notifications");
        mDatabaseForumParticipants = FirebaseDatabase.getInstance().getReference().child("Users_forums");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mMessageInput = (EditText) findViewById(R.id.input_message);

        mSendBtn = (ImageView) findViewById(R.id.sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (auth.getCurrentUser() == null) {
                    Toast.makeText(ChatroomActivity.this, "You need to be signed in first!",Toast.LENGTH_LONG).show();
                } else {
                    startPosting();
                }
            }
        });

        final TextView mNoAnsTxt = (TextView) findViewById(R.id.noAnsTxt);
        mDatabaseDiscussForum.child(QuizKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {

                    mNoAnsTxt.setVisibility(View.VISIBLE);
                } else {
                    mNoAnsTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        chatAdapter = new ChatAdapter(ChatroomActivity.this,chatList);

        mChatList = (RecyclerView) findViewById(R.id.Chat_list);
        mLinearlayout = new LinearLayoutManager(ChatroomActivity.this);
        //mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(mLinearlayout);
        mChatList.setAdapter(chatAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        currentPage++;
                        chatList.clear();
                        LoadMessage();


                    }
                },3000);

            }
        });

        chatList.clear();
        LoadMessage();

        //checkTyping();

    }

    private void LoadMessage() {

        Query quizQuery = mDatabaseDiscussForum.child(QuizKey);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat message = dataSnapshot.getValue(Chat.class);

                chatList.add(message);
                chatAdapter.notifyDataSetChanged();
                chatAdapter.notifyItemInserted(0);
                mLinearlayout.setStackFromEnd(true);
                mSwipeRefreshLayout.setRefreshing(false);
                mChatList.scrollToPosition(chatList.size()-1);

               /* mChatList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, final int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        mChatList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mChatList.smoothScrollToPosition(bottom);
                            }
                        }, 0);
                    }
                });
*/

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

    void refreshItems() {
        // Load items
        // ...

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void startPosting() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);
        final String stringDate2 = DateFormat.getDateInstance().format(date);

        final String questionBodyTag = mMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(questionBodyTag)) {


        } else {

            final DatabaseReference newPost = mDatabaseDiscussForum.child(QuizKey).push();
            final DatabaseReference newPost2 = mDatabaseForumParticipants.child(auth.getCurrentUser().getUid()).child(QuizKey).push();

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    name = dataSnapshot.child("name").getValue().toString();
                                   /* final String image = dataSnapshot.child("image").getValue().toString();*/
                    // getting user uid

                    newPost.child("message").setValue(questionBodyTag);
                    newPost.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                    newPost.child("sender_name").setValue(dataSnapshot.child("name").getValue());
                    newPost.child("sender_image").setValue(dataSnapshot.child("user_image").getValue());
                    newPost.child("posted_date").setValue(System.currentTimeMillis());
                    newPost.child("post_id").setValue(newPost.getKey());

                    newPost2.child("message").setValue(questionBodyTag);
                    newPost2.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                    newPost2.child("sender_name").setValue(dataSnapshot.child("name").getValue());
                    newPost2.child("sender_image").setValue(dataSnapshot.child("user_image").getValue());
                    newPost2.child("posted_date").setValue(System.currentTimeMillis());
                    newPost2.child("post_id").setValue(newPost.getKey());

                    mMessageInput.getText().clear();
                    Toast.makeText(ChatroomActivity.this, "Message posted successfully",Toast.LENGTH_LONG).show();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

           /* //POST TO FORUM NOTIFICATIONS
            mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String quiz_sender_uid = dataSnapshot.child("sender_uid").getValue().toString();

                    final DatabaseReference post_noty = mDatabaseForumNotifications.child(quiz_sender_uid).child(QuizKey);

                    post_noty.child("sender_name").setValue(name);
                    post_noty.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                    post_noty.child("question_key").setValue(QuizKey);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
*/
        }
    }

    /*@Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Chat, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, CommentViewHolder>(

                Chat.class,
                R.layout.hash_row,
                CommentViewHolder.class,
                mDatabase

        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Chat model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setMessage(model.getMessage());
                viewHolder.setDate(model.getDate());
                viewHolder.setName(model.getName());
                *//*viewHolder.setCity(model.getCity());*//*
                viewHolder.setAddress(model.getAddress());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);


                mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.ReyLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(mProcessLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {


                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                        mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        mProcessLike = false;

                                    }else {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());

                                        mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        mProcessLike = false;

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });


                mDatabaseComment.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String group_uid = (String) dataSnapshot.child("this_is_a_group").getValue();

                        mDatabaseHashtag.child(mPostKey).child("Chats").child(post_key).addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String post_photo = (String) dataSnapshot.child("photo").getValue();
                                final String chat_icon = (String) dataSnapshot.child("change_chat_icon").getValue();

                                if (post_photo != null) {

                                    viewHolder.setPhoto(getApplicationContext(), model.getPhoto());
                                    viewHolder.mCardPhoto.setVisibility(View.VISIBLE);
                                    viewHolder.min_lay.setVisibility(View.GONE);


                                    // if card has my uid, then change chat balloon shape
                                } else {

                                }




                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mDatabaseComment.child(mCurrentUser.getUid()).child(mPostKey).child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();
                        final String chat_icon = (String) dataSnapshot.child("change_chat_icon").getValue();

                        if (post_photo != null) {

                            viewHolder.setPhoto(getApplicationContext(), model.getPhoto());
                            viewHolder.mCardPhoto.setVisibility(View.VISIBLE);

                            // if card has my uid, then change chat balloon shape
                        } else {

                            viewHolder.mCardPhoto.setVisibility(View.GONE);
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        final String current_user_uid = (String) snapshot.child("uid").getValue();

                        mDatabaseComment.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot snapshot) {

                                final String reciever_uid = (String) snapshot.child("uid").getValue();


                                if (snapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                    //viewHolder.rely.setVisibility(View.VISIBLE);
                                    //viewHolder.liny.setVisibility(View.GONE);

                                } else {

                                    // viewHolder.rely.setVisibility(View.GONE);
                                    // viewHolder.liny.setVisibility(View.VISIBLE);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.UserImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mDatabaseHashtag.child(mPostKey).child("Chats").child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String uid = dataSnapshot.child("uid").getValue().toString();

                                Intent cardonClick = new Intent(HashChatroomActivity.this, ViewProfileActivity.class);
                                cardonClick.putExtra("heartraise_id", uid );
                                startActivity(cardonClick);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });


            }
        };
        final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mCommentList.setAdapter(firebaseRecyclerAdapter);
        // mLinearLayoutManager.setReverseLayout(false);
        //mLinearLayoutManager.setStackFromEnd(true);

        mLayoutManager = new LinearLayoutManager(HashChatroomActivity.this);
        // mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(true);

        // Now set the layout manager and the adapter to the RecyclerView
        mCommentList.setLayoutManager(mLayoutManager);

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mLayoutManager.setStackFromEnd(true);
                    mCommentList.scrollToPosition(positionStart);
                }
            }
        });


        final long delay = 1000; // 1 seconds after user stops typing
        final long[] last_text_edit = {0};
        final Handler handler = new Handler();

        final Runnable input_finish_checker = new Runnable() {
            public void run() {
                if (System.currentTimeMillis() > (last_text_edit[0])) {
                    // ............
                    // ............

                }
            }
        };

        //checking if a user is typing
        EditText editText = (EditText) findViewById(R.id.emojicon_edit_text);
        editText.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged (CharSequence s,int start, int count,
                                                                           int after){
                                            }
                                            @Override
                                            public void onTextChanged ( final CharSequence s, int start, int before,
                                                                        int count){
                                                //You need to remove this to run only once
                                                handler.removeCallbacks(input_finish_checker);

                                            }
                                            @Override
                                            public void afterTextChanged ( final Editable s){
                                                //avoid triggering event when text is empty
                                                if (s.length() > 0) {
                                                    last_text_edit[0] = System.currentTimeMillis();
                                                    handler.postDelayed(input_finish_checker, delay);

                                                    // HIDE AUDIO BUTTON WHILE USER IS TYPING
                                                    ImageView photo = (ImageView) findViewById(R.id.cameraShot);
                                                    photo.setVisibility(View.GONE);

                                                    ImageView camera = (ImageView) findViewById(R.id.quickShot);
                                                    camera.setVisibility(View.GONE);

                                                    ImageView sendy = (ImageView) findViewById(R.id.sendBtn);
                                                    sendy.setVisibility(View.VISIBLE);


                                                } else {

                                                    Date date = new Date();
                                                    final String stringDate = DateFormat.getDateTimeInstance().format(date);


                                                    // SHOW AUDIO BUTTON WHILE USER IS TYPING
                                                    ImageView photo = (ImageView) findViewById(R.id.cameraShot);
                                                    photo.setVisibility(View.VISIBLE);

                                                    ImageView camera = (ImageView) findViewById(R.id.quickShot);
                                                    camera.setVisibility(View.VISIBLE);

                                                    ImageView sendy = (ImageView) findViewById(R.id.sendBtn);
                                                    sendy.setVisibility(View.GONE);

                                                }
                                            }
                                        }

        );



        // if recyclerview is at the bottom, clear any unread messages
        final boolean[] loading = {true};
        final int[] pastVisiblesItems = new int[1];
        final int[] visibleItemCount = new int[1];
        final int[] totalItemCount = new int[1];

        mCommentList.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount[0] = mLayoutManager.getChildCount();
                    totalItemCount[0] = mLayoutManager.getItemCount();
                    pastVisiblesItems[0] = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading[0])
                    {
                        if ( (visibleItemCount[0] + pastVisiblesItems[0]) >= totalItemCount[0])
                        {
                            loading[0] = false;
                            Log.v("...", "Last Item Wow !");
                            mDatabaseUnread.child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                        }
                    }
                }
            }
        });

    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:


                return super.onOptionsItemSelected(item);
        }
    }


}
