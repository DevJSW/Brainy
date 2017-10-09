package com.brainy.brainy.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.brainy.Adapters.AnswersAdapter;
import com.brainy.brainy.Adapters.SolutionsAdapter;
import com.brainy.brainy.R;
import com.brainy.brainy.data.Answer;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReadQuestionActivity extends AppCompatActivity {

    String  personName = null;
    String  personEmail = null;
    String  personId = null;
    Uri personPhoto = null;
    private Button signIn;
    private DatabaseReference mDatabaseUserInbox;
    private static final String TAG = "tab2Inbox";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;

    private RelativeLayout share, favourite, answer;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseUsersInbox, mDatabaseUsersPoints, mDatabaseUsersAns, mDatabaseNotifications;
    private RecyclerView mAnsList;
    Context mContext;
    private FirebaseAuth auth;
    String QuizKey = null;

    private ImageView quizVoteUp;
    private ImageView quizVoteDown;
    private View parent_view;
    private Boolean mProcessApproval = false;
    private Boolean mProcessFavourite = false;

    private Boolean mProcessPoints = false;
    private DatabaseReference mDatabaseFavourite, mDatabaseUsersFavourite;

    SwipeRefreshLayout mSwipeRefreshLayout;
    SolutionsAdapter answersAdapter;
    private final List<Answer> answerList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_question);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //QuizKey = getIntent().getExtras().getString("question_id");
        QuizKey = getIntent().getStringExtra("question_id");

        // Database channels
        mDatabaseFavourite = FirebaseDatabase.getInstance().getReference().child("Questions").child(QuizKey).child("favourite");
        mDatabaseUsersFavourite = FirebaseDatabase.getInstance().getReference().child("Users_favourite");
        mDatabaseUsersInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseUsersPoints = FirebaseDatabase.getInstance().getReference().child("Users_points");
        mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");

        parent_view = findViewById(android.R.id.content);
        quizVoteDown = (ImageView) findViewById(R.id.quiz_vote_down);
        quizVoteUp = (ImageView) findViewById(R.id.quiz_vote_up);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

       /* mAnsList = (RecyclerView) findViewById(R.id.mAnsList);
        mAnsList.setLayoutManager(layoutManager);
        mAnsList.setHasFixedSize(true);
*/
        auth = FirebaseAuth.getInstance();


        answersAdapter = new SolutionsAdapter(ReadQuestionActivity.this,answerList);
        mAnsList = (RecyclerView) findViewById(R.id.mAnsList);
        mLinearlayout = new LinearLayoutManager(ReadQuestionActivity.this);

        mAnsList.setHasFixedSize(true);
        mAnsList.setLayoutManager(new LinearLayoutManager(ReadQuestionActivity.this));
        mAnsList.setAdapter(answersAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        currentPage++;
                        answerList.clear();
                        LoadMessage();


                    }
                },3000);

            }
        });

        answerList.clear();
        LoadMessage();


        // SYNC DATABASE
        mDatabaseQuestions.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        Window window = ReadQuestionActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ReadQuestionActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

            favourite = (RelativeLayout) findViewById(R.id.favourite);
            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (auth.getCurrentUser() != null) {
                        mProcessFavourite = true;

                        mDatabaseFavourite.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessFavourite) {

                                    if (dataSnapshot.hasChild(auth.getCurrentUser().getUid())) {

                                        ImageView starred = (ImageView) findViewById(R.id.starredImg);
                                        starred.setImageResource(R.drawable.star_img);
                                        mDatabaseFavourite.child(auth.getCurrentUser().getUid()).removeValue();
                                        mDatabaseUsersFavourite.child(auth.getCurrentUser().getUid()).child(QuizKey).removeValue();

                                        mProcessFavourite = false;

                                    } else {

                                        ImageView starred = (ImageView) findViewById(R.id.starredImg);
                                        starred.setImageResource(R.drawable.star_yellow);
                                        mDatabaseFavourite.child(auth.getCurrentUser().getUid()).setValue("isFavourite");

                                        //ADD QUESTION TO USER_FAVOURITE IN DATABASE
                                        mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String question_title = dataSnapshot.child("question_title").getValue().toString();
                                                String question_body = dataSnapshot.child("question_body").getValue().toString();
                                                String sender_name = dataSnapshot.child("sender_name").getValue().toString();
                                                String sender_image = dataSnapshot.child("sender_image").getValue().toString();
                                                String posted_date = dataSnapshot.child("posted_date").getValue().toString();

                                                final DatabaseReference newPost = mDatabaseUsersFavourite.child(auth.getCurrentUser().getUid()).child(QuizKey);

                                                Map<String, Object> map = new HashMap<>();
                                                map.put("question_title", question_title);
                                                map.put("question_body", question_body);
                                                map.put("sender_uid", auth.getCurrentUser().getUid());
                                                map.put("sender_name", sender_name);
                                                map.put("sender_image", sender_image);
                                                map.put("posted_date", posted_date);
                                                map.put("post_id", newPost.getKey());
                                                newPost.setValue(map);

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        mProcessFavourite = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(parent_view, "You need to be signed in for you to favourite this question", Snackbar.LENGTH_LONG)
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



        if (auth.getCurrentUser() != null) {
            // always check if question is users favourite
            mDatabaseFavourite.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(auth.getCurrentUser().getUid())) {

                        ImageView starred = (ImageView) findViewById(R.id.starredImg);
                        starred.setImageResource(R.drawable.star_yellow);
                    } else {

                        ImageView starred = (ImageView) findViewById(R.id.starredImg);
                        starred.setImageResource(R.drawable.star_img);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {

        }
        answer = (RelativeLayout) findViewById(R.id.answer);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser() != null) {

                    final Context context = ReadQuestionActivity.this;

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

                                final DatabaseReference newPost = mDatabaseQuestions.child(QuizKey).child("Answers").push();
                                final DatabaseReference newPost2 = mDatabaseUsersAns.child(auth.getCurrentUser().getUid()).push();


                                mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String name = dataSnapshot.child("name").getValue().toString();
                                        final String image = dataSnapshot.child("user_image").getValue().toString();

                                        mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String question_title = dataSnapshot.child("question_title").getValue().toString();

                                                //DELETE UNANSERED CHILD FROM DATABASE
                                                if (dataSnapshot.hasChild("Unanswered")) {
                                                    mDatabaseQuestions.child(QuizKey).child("Unanswered").removeValue();
                                                }

                                                // getting user uid
                                                newPost.child("posted_answer").setValue(questionBodyTag);
                                                newPost.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                                                newPost.child("question_title").setValue(question_title);
                                                newPost.child("sender_name").setValue(name);
                                                newPost.child("sender_image").setValue(image);
                                                newPost.child("post_id").setValue(newPost.getKey());
                                                newPost.child("posted_date").setValue(stringDate2);
                                                newPost.child("question_key").setValue(QuizKey);
                                                newPost.child("post_id").setValue(newPost.getKey());


                                                //SEND ANSWER TO USEERS ANSWERS IN DATABASE
                                                newPost2.child("posted_answer").setValue(questionBodyTag);
                                                newPost2.child("sender_image").setValue(dataSnapshot.child("user_image").getValue());
                                                newPost2.child("posted_date").setValue(stringDate2);
                                                newPost2.child("question_key").setValue(QuizKey);
                                                newPost2.child("posted_quiz_title").setValue(question_title);
                                                newPost2.child("post_id").setValue(newPost.getKey());

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


                            }
                            if (!TextUtils.isEmpty(questionBodyTag)) {

                                mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String name = dataSnapshot.child("name").getValue().toString();
                                        final String image = dataSnapshot.child("user_image").getValue().toString();
                                        final DatabaseReference newPost3 = mDatabaseUsersInbox.child(auth.getCurrentUser().getUid()).push();
                                        //SEND MESSAGE TO QUIZ OWNER'S INBOX
                                        mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                Map<String, Object> map = new HashMap<>();
                                                map.put("posted_reason", "answered your question");
                                                map.put("posted_answer", questionBodyTag);
                                                map.put("sender_uid", auth.getCurrentUser().getUid());
                                                map.put("sender_name", name);
                                                map.put("read", false);
                                                map.put("question_key", QuizKey);
                                                map.put("sender_image", image);
                                                map.put("posted_date", stringDate2);
                                                map.put("post_id", newPost3.getKey());
                                                newPost3.setValue(map);

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

                            }
                        }
                    });
                } else {
                    Snackbar snackbar = Snackbar
                            .make(parent_view, "You need to have an account for you to vote", Snackbar.LENGTH_LONG)
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

        mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String userimg = (String) dataSnapshot.child("sender_image").getValue();
                final String username = (String) dataSnapshot.child("sender_name").getValue();
                final String date = (String) dataSnapshot.child("posted_date").getValue();
                final String question_topic = (String) dataSnapshot.child("question_title").getValue();
                final String question_body = (String) dataSnapshot.child("question_body").getValue();
                final ImageView civ = (ImageView) findViewById(R.id.post_image);
                final TextView name = (TextView) findViewById(R.id.post_name);

                // load image on toolbar
                CircleImageView userImgToolbar = (CircleImageView) findViewById(R.id.toolbarImg);
                Picasso.with(ReadQuestionActivity.this)
                        .load(userimg)
                        .placeholder(R.drawable.placeholder_image)
                        .into(userImgToolbar);

                // set username on toolbar
                TextView toolbar_username = (TextView) findViewById(R.id.toolbar_username);
                toolbar_username.setText(username);

                TextView toolbar_date = (TextView) findViewById(R.id.toolbar_last_seen_date);
                toolbar_date.setText(date);

                TextView question_read_topic= (TextView) findViewById(R.id.quizTopic);
                question_read_topic.setText(question_topic);

                TextView question_read_body= (TextView) findViewById(R.id.quizBody);
                question_read_body.setText(question_body);

                share = (RelativeLayout) findViewById(R.id.share);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent myIntent = new Intent(Intent.ACTION_SEND);
                        myIntent.setType("text/plain");
                        String shareBody =question_body;
                        String shareSub = question_topic;
                        myIntent.putExtra(Intent.EXTRA_SUBJECT,shareSub);
                        myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                        startActivity(Intent.createChooser(myIntent,"Share this question"));
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final TextView mNoAnsTxt = (TextView) findViewById(R.id.noAnsTxt);
        mDatabaseQuestions.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null){

                    mNoAnsTxt.setVisibility(View.VISIBLE);
                } else {
                    mNoAnsTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of answers
        mDatabaseQuestions.child(QuizKey).child("Answers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView answersCounter = (TextView) findViewById(R.id.answersCounter);
                answersCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //DISPLAY NUMBER OF UP VOTES A QUIZ HAS.....
        mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("up_votes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView voteUpCount = (TextView) findViewById(R.id.vote_up_counter);
                voteUpCount.setText(dataSnapshot.getChildrenCount() + "");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        intQuizVoting();
        initDiscussForum();

    }

    private void LoadMessage() {

        Query quizQuery = mDatabaseQuestions.child(QuizKey).child("Answers").limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        if (quizQuery == null) {

         /*   noAns.setVisibility(View.VISIBLE);*/

        } else {

           /* noAns.setVisibility(View.GONE);*/
        }

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Answer message = dataSnapshot.getValue(Answer.class);

                answerList.add(message);
                answersAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

              /*  mQuestionsList.scrollToPosition(questionList.size()-1);*/

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

    private void initDiscussForum() {

        RelativeLayout discuss_forum = (RelativeLayout) findViewById(R.id.forum);
        discuss_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(ReadQuestionActivity.this, DiscussForumActivity.class);
                openRead.putExtra("question_id", QuizKey );
                startActivity(openRead);
            }
        });
    }

    private void intQuizVoting() {

        quizVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((auth.getCurrentUser() != null))
                {

                    final Context context = ReadQuestionActivity.this;

                    // custom dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.vote_dialog);
                    dialog.setCancelable(false);
                            /*dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*/
                    dialog.show();

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
                        public void onClick(final View v) {

                            dialog.dismiss();
                            mProcessApproval = true;

                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mProcessApproval) {

                                        if (dataSnapshot.child("up_votes").hasChild(auth.getCurrentUser().getUid())) {

                                                   /* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*/
                                           /* Toast.makeText(ReadQuestionActivity.this, "You have already voted for this question",Toast.LENGTH_LONG).show();*/
                                            Snackbar snackbar = Snackbar
                                                    .make(parent_view, "You have already voted for this question", Snackbar.LENGTH_LONG);
                                                   /* .setAction("UNDO", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Snackbar snackbar1 = Snackbar.make(parent_view, "Your vote has been reversed!", Snackbar.LENGTH_SHORT);
                                                            snackbar1.show();
                                                        }
                                                    });*/

                                            snackbar.show();

                                            //add user uid to points database
                                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("up_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    TextView voteUpCount = (TextView) findViewById(R.id.vote_up_counter);
                                                    voteUpCount.setText(dataSnapshot.getChildrenCount() + "");

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            mProcessApproval = false;

                                        } else {

                                                    mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("up_votes").child(auth.getCurrentUser().getUid()).setValue("iVote");

                                                    // CHECK IF USER HAS VOTED AND ADD 5 POINTS TO THE USER WHO POSTED THE QUESTION.....................

                                                    mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                                            mDatabaseUsers.child(sender_uid).child("points_earned").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                                    Long user_points = (Long) dataSnapshot.getValue();
                                                                    user_points = user_points + 5;

                                                                    mDatabaseUsers.child(sender_uid).child("points_earned").setValue(user_points);

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

                                                    mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("up_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            TextView voteUpCount = (TextView) findViewById(R.id.vote_up_counter);
                                                            voteUpCount.setText(dataSnapshot.getChildrenCount() + "");
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    mProcessApproval = false;
                                                    Toast.makeText(ReadQuestionActivity.this, "Vote was successful",Toast.LENGTH_LONG).show();



                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    });

                } else
                {
                    Toast.makeText(ReadQuestionActivity.this, "You need to be signed in for you to vote",Toast.LENGTH_LONG).show();
                    Snackbar snackbar = Snackbar
                            .make(parent_view, "You need to be signed in for you to vote", Snackbar.LENGTH_LONG)
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

        quizVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((auth.getCurrentUser() != null))
                {

                    final Context context = ReadQuestionActivity.this;

                    // custom dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.down_vote_dialog);
                    dialog.setCancelable(false);
                            /*dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*/
                    dialog.show();

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

                            dialog.dismiss();
                            mProcessApproval = true;

                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mProcessApproval) {

                                        if (dataSnapshot.child("down_votes").hasChild(auth.getCurrentUser().getUid())) {

                                                   /* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*/
                                            Snackbar snackbar = Snackbar
                                                    .make(parent_view, "You have already down-voted this question", Snackbar.LENGTH_LONG);
                                                   /* .setAction("UNDO", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Snackbar snackbar1 = Snackbar.make(parent_view, "Your vote has been reversed!", Snackbar.LENGTH_SHORT);
                                                            snackbar1.show();
                                                        }
                                                    });*/

                                            snackbar.show();

                                            //add user uid to points database
                                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("down_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    /*TextView voteUpCount = (TextView) findViewById(R.id.vote_up_counter);
                                                    voteUpCount.setText(dataSnapshot.getChildrenCount() + "");*/
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            mProcessApproval = false;

                                        } else {

                                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("down_votes").child(auth.getCurrentUser().getUid()).setValue("iVote");

                                            // CHECK IF USER HAS VOTED AND ADD 5 POINTS TO THE USER WHO POSTED THE QUESTION.....................

                                            mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                                    mDatabaseUsers.child(sender_uid).child("points_earned").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            Long user_points = (Long) dataSnapshot.getValue();
                                                            user_points = user_points - 2;

                                                            mDatabaseUsers.child(sender_uid).child("points_earned").setValue(user_points);

                                                            //ALSO DEDUCT 1 POINT FROM THIS USER.....

                                                            Long current_user_points = (Long) dataSnapshot.getValue();
                                                            current_user_points = current_user_points - 1;

                                                            mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("points_earned").setValue(current_user_points);

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

                                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").child("down_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    /*TextView voteUpCount = (TextView) findViewById(R.id.vote_up_counter);
                                                    voteUpCount.setText(dataSnapshot.getChildrenCount() + "");*/
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            mProcessApproval = false;
                                            Toast.makeText(ReadQuestionActivity.this, "Vote was successful",Toast.LENGTH_LONG).show();

                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    });

                } else
                {
                    Toast.makeText(ReadQuestionActivity.this, "You need to have an account for you to vote",Toast.LENGTH_LONG).show();
                }
            }

        });


    }


    private void showSignInDialog() {

        final Context context = ReadQuestionActivity.this;

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.sign_in_dialog);
        dialog.setTitle("Let's get started...");
        dialog.show();

        Button googleBtn = (Button) dialog.findViewById(R.id.googleBtn);
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

        mGoogleApiClient = new GoogleApiClient.Builder(ReadQuestionActivity.this)
                .enableAutoManage(ReadQuestionActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(ReadQuestionActivity.this, "Failed to connect to Google, check your internet connection.",
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

                Toast.makeText(ReadQuestionActivity.this, "Sign in success!.",
                        Toast.LENGTH_LONG).show();
                this.recreate();

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
                .addOnCompleteListener(ReadQuestionActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(ReadQuestionActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            postUserInfoToDB();
                            Intent openProfileEdit = new Intent(ReadQuestionActivity.this, ProfileEditActivity.class);
                           /* openRead.putExtra("question_id", quiz_key );*/
                            startActivity(openProfileEdit);

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



   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read, menu);
       *//* this.menu = menu;*//*
        return true;
    }
*/

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
