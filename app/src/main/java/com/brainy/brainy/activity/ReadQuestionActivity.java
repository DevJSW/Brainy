package com.brainy.brainy.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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

    private RelativeLayout share, favourite, answer;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseUsersInbox, mDatabaseUsersPoints, mDatabaseUsersAns, mDatabaseNotifications;
    private RecyclerView mAnsList;
    Context mContext;
    private FirebaseAuth auth;
    String QuizKey = null;

    private ImageView quizVoteUp;
    private ImageView quizVoteDown;

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

        if (auth.getCurrentUser() != null) {
            favourite = (RelativeLayout) findViewById(R.id.favourite);
            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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
                }
            });
        } else {}

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
        } else {}
        answer = (RelativeLayout) findViewById(R.id.answer);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                        if ( TextUtils.isEmpty(questionBodyTag)) {

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
                                            newPost2.child("post_id").setValue(newPost2.getKey());

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


                        } if( !TextUtils.isEmpty(questionBodyTag)) {

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
                        public void onClick(View v) {

                            dialog.dismiss();
                            mProcessApproval = true;

                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mProcessApproval) {

                                        if (dataSnapshot.child("up_votes").hasChild(auth.getCurrentUser().getUid())) {

                                                   /* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*/
                                            Toast.makeText(ReadQuestionActivity.this, "You have already voted",Toast.LENGTH_LONG).show();

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

                                        } else{

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
                    Toast.makeText(ReadQuestionActivity.this, "You need to have an account for you to vote",Toast.LENGTH_LONG).show();
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
                        public void onClick(View v) {

                            dialog.dismiss();
                            mProcessApproval = true;

                            mDatabaseQuestions.child(QuizKey).child("Quiz_votes").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mProcessApproval) {

                                        if (dataSnapshot.child("down_votes").hasChild(auth.getCurrentUser().getUid())) {

                                                   /* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*/
                                            Toast.makeText(ReadQuestionActivity.this, "You have already voted",Toast.LENGTH_LONG).show();

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

                                        } else{

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

    /*@Override
    protected void onResume() {
        super.onResume();

        final FirebaseRecyclerAdapter<Answer, AnswerViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Answer, AnswerViewHolder>(

                Answer.class,
                R.layout.answer2_item,
                AnswerViewHolder.class,
                mDatabaseQuestions.child(QuizKey).child("Answers")


        ) {
            @Override
            protected void populateViewHolder(final AnswerViewHolder viewHolder, final Answer model, final int position) {

                final String answer_key = getRef(position).getKey();

                viewHolder.setSender_name(model.getSender_name());
                viewHolder.setPosted_date(model.getPosted_date());
                viewHolder.setPosted_answer(model.getPosted_answer());
                viewHolder.setSender_image(getApplicationContext(), model.getSender_image());

                mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.upVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if ((auth.getCurrentUser() != null))
                        {

                            final Context context = ReadQuestionActivity.this;

                            // custom dialog
                            final Dialog dialog = new Dialog(context);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.vote_dialog);
                            dialog.setCancelable(false);
                            *//*dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*//*
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

                                    mDatabaseQuestions.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (mProcessApproval) {

                                                if (dataSnapshot.child(answer_key).child("votes").hasChild(auth.getCurrentUser().getUid())) {

                                                   *//* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*//*
                                                    Toast.makeText(ReadQuestionActivity.this, "You cannot vote more than once",Toast.LENGTH_LONG).show();

                                                    //add user uid to points database
                                                    mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            viewHolder.voteCount.setText(dataSnapshot.getChildrenCount() + "");

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    mProcessApproval = false;

                                                } else{

                                                    mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).setValue("iVote");

                                                    // CHECK IF USER HAS VOTED AND ADD 10 POINTS TO THE USER WHO POSTED THE ANSWER.....................

                                                    mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                                            mDatabaseUsers.child(sender_uid).child("points_earned").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                                    Long user_points = (Long) dataSnapshot.getValue();
                                                                    user_points = user_points + 10;

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



                                                    mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            viewHolder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
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


                // DOWN VOTING AN ANSWER

                viewHolder.downVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mProcessApproval = true;

                        mDatabaseQuestions.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessApproval) {

                                    if (dataSnapshot.child(answer_key).child("down_votes").hasChild(auth.getCurrentUser().getUid())) {

                                                   *//* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*//*
                                        Toast.makeText(ReadQuestionActivity.this, "You have already down voted",Toast.LENGTH_LONG).show();

                                        //add user uid to points database
                                        mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("down_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.voteCount.setText(dataSnapshot.getChildrenCount() + "");

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                        mProcessApproval = false;

                                    } else{

                                        mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("down_votes").child(auth.getCurrentUser().getUid()).setValue("iVote");

                                        // CHECK IF USER HAS DOWN VOTED AND DEDUCT 2 POINTS TO THE USER WHO POSTED THE ANSWER.....................

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

                                        mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("down_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                        mProcessApproval = false;
                                        Toast.makeText(ReadQuestionActivity.this, "Down vote was successful",Toast.LENGTH_LONG).show();

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });


            }
        };

        mAnsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AnswerViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseApproval;
        TextView  voteCount;
        ImageView upVote, downVote;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mAuth = FirebaseAuth.getInstance();
            voteCount = (TextView) mView.findViewById(R.id.voteCount);
            upVote = (ImageView) mView.findViewById(R.id.approve);
            downVote = (ImageView) mView.findViewById(R.id.downvote);
        }


        public void setPosted_date(String posted_date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(posted_date);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setPosted_answer(String posted_answer) {

            TextView post_answer = (TextView) mView.findViewById(R.id.posted_answer);
            post_answer.setText(posted_answer);
        }


        public void setSender_image(final Context ctx, final String sender_image) {

            final ImageView civ = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx).load(sender_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.placeholder_image)
                    .into(civ, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {


                    Picasso.with(ctx)
                            .load(sender_image)
                            .placeholder(R.drawable.placeholder_image)
                            .into(civ);
                }
            });
        }

    }

*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read, menu);
       /* this.menu = menu;*/
        return true;
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
