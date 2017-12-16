package com.brainy.erevu.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brainy.erevu.R.layout.spinner_item;

public class PostAnswerActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseDiscussForum, mDatabaseUsers, mDatabaseUsersInbox, mDatabaseQuestions, mDatabaseUsersAns, mDatabaseAnswers;
    private EditText answerInput;
    private Button mSubmit;
    private ImageView tagImg;
    private FirebaseAuth auth;
    String QuizKey = null;
    String questionBodyTag;
    String  personName = null;
    String sender_name = null;
    String sender_image = null;
    String  personEmail = null;
    String  personId = null;

    Uri personPhoto = null;
    String city = null;
    String state = null;
    String country = null;

    List<String> subTopicList;
    String[] sub_topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_chat);
        Window window = PostAnswerActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( PostAnswerActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        answerInput = (EditText) findViewById(R.id.questionTitleInput);


        QuizKey = getIntent().getExtras().getString("question_id");
        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("city")) {
                    city = dataSnapshot.child("city").getValue().toString();
                } else if (dataSnapshot.hasChild("state")) {
                    state = dataSnapshot.child("state").getValue().toString();
                } if (dataSnapshot.hasChild("country")) {
                    country = dataSnapshot.child("country").getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseDiscussForum = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");
        mDatabaseAnswers = FirebaseDatabase.getInstance().getReference().child("Answers");
        mDatabaseUsersInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");

        mSubmit = (Button) findViewById(R.id.send_btn);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }

        });
        mDatabaseUsers.keepSynced(true);

    }

    private void startPosting() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);
        final String stringDate2 = DateFormat.getDateInstance().format(date);


        final String answer = answerInput.getText().toString().trim();
        if (TextUtils.isEmpty(answer)) {

        } else {

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    sender_name = dataSnapshot.child("name").getValue().toString();
                    sender_image = dataSnapshot.child("user_image").getValue().toString();

                    mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String question_title = dataSnapshot.child("question_title").getValue().toString();
                            String sender_uid = dataSnapshot.child("sender_uid").getValue().toString();

                            final DatabaseReference newPostAns = mDatabaseAnswers.child(QuizKey).child(auth.getCurrentUser().getUid());
                            final DatabaseReference newPost2 = mDatabaseUsersAns.child(auth.getCurrentUser().getUid()).child(QuizKey);

                            //DELETE UNANSERED CHILD FROM DATABASE
                            if (dataSnapshot.hasChild("Unanswered")) {
                                mDatabaseAnswers.child(QuizKey).child("Unanswered").removeValue();
                            }

                            Map<String, Object> mapAns = new HashMap<>();
                            mapAns.put("question_title", question_title);
                            mapAns.put("posted_answer", answer);
                            mapAns.put("sender_uid", auth.getCurrentUser().getUid());
                            mapAns.put("sender_name", sender_name);
                            mapAns.put("question_key", QuizKey);
                            mapAns.put("sender_image", sender_image);
                            mapAns.put("posted_date", System.currentTimeMillis());
                            mapAns.put("post_id", newPostAns.getKey());
                            mapAns.put("city", city);
                            mapAns.put("state", state);
                            newPostAns.setValue(mapAns);

                            //SEND ANSWER TO USERS ANSWERS IN DATABASE
                            newPost2.child("posted_answer").setValue(answer);
                            newPost2.child("sender_image").setValue(dataSnapshot.child("user_image").getValue());
                            newPost2.child("posted_date").setValue(System.currentTimeMillis());
                            newPost2.child("question_key").setValue(QuizKey);
                            newPost2.child("posted_quiz_title").setValue(question_title);
                            newPost2.child("post_id").setValue(newPostAns.getKey());
                            newPost2.child("city").setValue(city);
                            newPost2.child("state").setValue(state);

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

            answerInput.getText().clear();
            Toast.makeText(PostAnswerActivity.this, "Answer posted successfully.",
                    Toast.LENGTH_LONG).show();
            finish();

        }
        if (!TextUtils.isEmpty(answer)) {

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("user_image").getValue().toString();

                    //SEND MESSAGE TO QUIZ OWNER'S INBOX
                    mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String sender_uid = dataSnapshot.child("sender_uid").getValue().toString();

                            final DatabaseReference newPost3 = mDatabaseUsersInbox.child(sender_uid).child(auth.getCurrentUser().getUid());

                            String PostID = newPost3.getKey();

                            Map<String, Object> map = new HashMap<>();
                            map.put("posted_reason", "answered your question");
                            map.put("posted_answer", answer);
                            map.put("sender_uid", auth.getCurrentUser().getUid());
                            map.put("sender_name", name);
                            map.put("read", false);
                            map.put("question_key", QuizKey);
                            map.put("sender_image", image);
                            map.put("posted_date", System.currentTimeMillis());
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

}
