package com.brainy.erevu.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.ChatAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Chat;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscussForumActivity extends AppCompatActivity {

    String  personName = "";
    String  personEmail = "";
    String  personId = "";
    Uri personPhoto = null;

    private static final String TAG = "DiscussForum";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;

    private RecyclerView mAnsList;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseDiscussForum, mDatabaseForumParticipants;
    private FirebaseAuth auth;
    String QuizKey = null;
    private ImageView backBtn;

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

        QuizKey = getIntent().getExtras().getString("question_id");

        Window window = DiscussForumActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( DiscussForumActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiscussForumActivity.this.finish();
            }
        });

        // Database channels
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseDiscussForum = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");
        mDatabaseForumParticipants = FirebaseDatabase.getInstance().getReference().child("Users_forums");

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        auth = FirebaseAuth.getInstance();

        // SYNC DATABASE
        mDatabaseQuestions.keepSynced(true);
        mDatabaseForumParticipants.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseDiscussForum.keepSynced(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth.getCurrentUser() != null) {
                    final Context context = DiscussForumActivity.this;

                    Intent openRead = new Intent(DiscussForumActivity.this, PostAnswerActivity.class);
                    openRead.putExtra("question_id", QuizKey );
                    startActivity(openRead);

                    /*// custom dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.chat_dialog);
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
                                final DatabaseReference newPost2 = mDatabaseForumParticipants.child(auth.getCurrentUser().getUid()).child(QuizKey).push();

                                mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String name = dataSnapshot.child("name").getValue().toString();
                                   *//* final String image = dataSnapshot.child("image").getValue().toString();*//*
                                        // getting user uid

                                       *//* Map<String, Object> map = new HashMap<>();
                                        map.put("message", questionBodyTag);
                                        map.put("sender_uid", auth.getCurrentUser().getUid());
                                        map.put("sender_name", dataSnapshot.child("name").getValue());
                                        map.put("sender_image", dataSnapshot.child("user_image").getValue());
                                        map.put("posted_date", stringDate2);
                                        map.put("post_id", newPost.getKey());
                                        newPost.setValue(map);

                                        Map<String, Object> map_participants = new HashMap<>();
                                        map_participants.put("message", questionBodyTag);
                                        map_participants.put("sender_uid", auth.getCurrentUser().getUid());
                                        map_participants.put("sender_name", dataSnapshot.child("name").getValue());
                                        map_participants.put("sender_image", dataSnapshot.child("user_image").getValue());
                                        map_participants.put("posted_date", stringDate2);
                                        map_participants.put("post_id", newPost.getKey());
                                        newPost2.setValue(map_participants);*//*

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

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                    });
*/                }else {
                    Snackbar snackbar = Snackbar
                            .make(view, "You need to sign in first to be able to post a question!", Snackbar.LENGTH_LONG)
                            .setAction("SIGN IN", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showSignInDialog();
                                }
                            });

                    snackbar.show();
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

    @Override
    protected void onResume() {
        super.onResume();
        chatList.clear();
        LoadMessage();
    }

    private void showSignInDialog() {

        final Context context = DiscussForumActivity.this;

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.sign_in_dialog);
        dialog.setTitle("Let's get started...");
        dialog.show();

        Button signBtn = (Button) dialog.findViewById(R.id.sign_in);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openRead = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(openRead);
                dialog.dismiss();
            }
        });

        RelativeLayout googleBtn = (RelativeLayout) dialog.findViewById(R.id.googleBtn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initGoogleSignIn();
                dialog.dismiss();
            }
        });


    }

    private void initGoogleSignIn() {

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(DiscussForumActivity.this)
                .enableAutoManage(DiscussForumActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(DiscussForumActivity.this, "Failed to connect to Google, check your internet connection.",
                                Toast.LENGTH_LONG).show();
                    }
                })

                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Configure Google Sign In
        progressBar.setVisibility(View.VISIBLE);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        progressBar.setVisibility(View.GONE);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressBar.setVisibility(View.VISIBLE);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                personName = account.getDisplayName();
                personEmail = account.getEmail();
                personId = account.getId();
                personPhoto = account.getPhotoUrl();

                postUserInfoToDB();
                Toast.makeText(DiscussForumActivity.this, "Sign in success!.",
                        Toast.LENGTH_LONG).show();

            } else {
                // Google Sign In failed, update UI appropriately
                // ...

                progressBar.setVisibility(View.GONE);
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(DiscussForumActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(DiscussForumActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            postUserInfoToDB();
                            postUserInfoToDB();
                            Toast.makeText(DiscussForumActivity.this, "Sign in success!.",
                                    Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }

    private void postUserInfoToDB() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        final DatabaseReference newPost = mDatabaseUsers;

        newPost.child(auth.getCurrentUser().getUid()).child("name").setValue(personName);
        newPost.child(auth.getCurrentUser().getUid()).child("status").setValue("");
        newPost.child(auth.getCurrentUser().getUid()).child("user_image").setValue(personPhoto.toString());
        newPost.child(auth.getCurrentUser().getUid()).child("joined_date").setValue(stringDate);
        newPost.child(auth.getCurrentUser().getUid()).child("personId").setValue(personId);
        newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
        newPost.child(auth.getCurrentUser().getUid()).child("user_gmail").setValue(personEmail);
        newPost.child(auth.getCurrentUser().getUid()).child("sign_in_type").setValue("google_signIn");
        newPost.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
        newPost.child(auth.getCurrentUser().getUid()).child("points_earned").setValue(10);
        newPost.child(auth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

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