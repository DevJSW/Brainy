package com.brainy.brainy.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.brainy.brainy.data.Answer;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class AnswersActivity extends AppCompatActivity {

    private RecyclerView mAnsList;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseUsersInbox;
    private FirebaseAuth auth;
    String QuizKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        QuizKey = getIntent().getExtras().getString("question_id");

        // Database channels
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseUsersInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mAnsList = (RecyclerView) findViewById(R.id.mAnsList);
        mAnsList.setLayoutManager(layoutManager);
        mAnsList.setHasFixedSize(true);

        auth = FirebaseAuth.getInstance();

        // SYNC DATABASE
        mDatabaseQuestions.keepSynced(true);
        mDatabaseUsers.keepSynced(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Context context = AnswersActivity.this;

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

                            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String name = dataSnapshot.child("name").getValue().toString();
                                   /* final String image = dataSnapshot.child("image").getValue().toString();*/
                                    // getting user uid
                                    newPost.child("posted_answer").setValue(questionBodyTag);
                                    newPost.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                                    newPost.child("sender_name").setValue(dataSnapshot.child("name").getValue());
                                    newPost.child("sender_image").setValue(dataSnapshot.child("user_image").getValue());
                                    newPost.child("posted_date").setValue(stringDate2);

                                    //SEND MESSAGE TO QUIZ OWNER'S INBOX
                                    mDatabaseQuestions.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            final String sender_uid = dataSnapshot.child("sender_uid").getValue().toString();

                                            final DatabaseReference newPost2 = mDatabaseUsersInbox.child(sender_uid).push();
                                            newPost2.child("posted_answer").setValue(questionBodyTag);
                                            newPost2.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                                            newPost2.child("sender_name").setValue(name);
                                           /* newPost2.child("sender_image").setValue(image);*/
                                            newPost2.child("posted_date").setValue(stringDate2);

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

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        final TextView mNoAnsTxt = (TextView) findViewById(R.id.noAnsTxt);
        mDatabaseQuestions.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {
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

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Answer, ReadQuestionActivity.AnswerViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Answer, ReadQuestionActivity.AnswerViewHolder>(

                Answer.class,
                R.layout.answer_item,
                ReadQuestionActivity.AnswerViewHolder.class,
                mDatabaseQuestions.child(QuizKey).child("Answers")


        ) {
            @Override
            protected void populateViewHolder(final ReadQuestionActivity.AnswerViewHolder viewHolder, final Answer model, final int position) {

                final String answer_key = getRef(position).getKey();

                viewHolder.setSender_name(model.getSender_name());
                viewHolder.setPosted_date(model.getPosted_date());
               /* viewHolder.setPosted_answer(model.getPosted_answer());
                viewHolder.setSender_image(getApplicationContext(), model.getSender_image());*/

            }
        };

        mAnsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AnswerViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseUnread;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mAuth = FirebaseAuth.getInstance();

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

            Picasso.with(ctx).load(sender_image).networkPolicy(NetworkPolicy.OFFLINE).into(civ, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {


                    Picasso.with(ctx).load(sender_image).into(civ);
                }
            });
        }


    }
}