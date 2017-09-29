package com.brainy.brainy.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brainy.brainy.Adapters.ChatAdapter;
import com.brainy.brainy.Adapters.QuestionAdapter;
import com.brainy.brainy.R;
import com.brainy.brainy.data.Chat;
import com.brainy.brainy.data.Question;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscussForumActivity extends AppCompatActivity {

    private RecyclerView mAnsList;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseDiscussForum;
    private FirebaseAuth auth;
    String QuizKey = null;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatList;
    ChatAdapter chatAdapter;
    private final List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        QuizKey = getIntent().getExtras().getString("question_id");

        Window window = DiscussForumActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( DiscussForumActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // Database channels
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseDiscussForum = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        auth = FirebaseAuth.getInstance();

        // SYNC DATABASE
        mDatabaseQuestions.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseDiscussForum.keepSynced(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Context context = DiscussForumActivity.this;

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.answer_dialog);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();

                final EditText questionBodyInput = (EditText) dialog.findViewById(R.id.questionBodyInput);
                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Button create = (Button) dialog.findViewById(R.id.create);
                create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startPosting();
                        dialog.dismiss();
                    }

                    private void startPosting() {

                        Date date = new Date();
                        final String stringDate = DateFormat.getDateTimeInstance().format(date);
                        final String stringDate2 = DateFormat.getDateInstance().format(date);

                        final String questionBodyTag = questionBodyInput.getText().toString().trim();
                        if (TextUtils.isEmpty(questionBodyTag)) {


                        } else {

                            final DatabaseReference newPost = mDatabaseDiscussForum.child(QuizKey).push();

                            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String name = dataSnapshot.child("name").getValue().toString();
                                   /* final String image = dataSnapshot.child("image").getValue().toString();*/
                                    // getting user uid

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("message", questionBodyTag);
                                    map.put("sender_uid", auth.getCurrentUser().getUid());
                                    map.put("sender_name", dataSnapshot.child("name").getValue());
                                    map.put("sender_image", dataSnapshot.child("user_image").getValue());
                                    map.put("posted_date", stringDate2);
                                    map.put("post_id", newPost.getKey());
                                    newPost.setValue(map);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }
                });

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
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

        chatAdapter = new ChatAdapter(DiscussForumActivity.this,chatList);

        mChatList = (RecyclerView) findViewById(R.id.Chat_list);
        mLinearlayout = new LinearLayoutManager(DiscussForumActivity.this);
        mLinearlayout.setReverseLayout(true);
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
    }

    private void LoadMessage() {

        Query quizQuery = mDatabaseDiscussForum.child(QuizKey).limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat message = dataSnapshot.getValue(Chat.class);

                chatList.add(message);
                chatAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

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