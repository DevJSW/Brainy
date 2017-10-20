package com.brainy.brainy.activity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brainy.brainy.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditQuestionActivity extends AppCompatActivity {

    String QuizKey = null;
    String QuizTitle = null;
    String QuizBody = null;
    private Button saveBtn;
    private FirebaseAuth auth;
    private DatabaseReference mDatabaseUsersQuiz, mDatabaseQuestions;
    private ProgressDialog mprogress;
    private EditText mTitle, mBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question);

        Window window = EditQuestionActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( EditQuestionActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        QuizKey = getIntent().getStringExtra("question_id");
        mDatabaseUsersQuiz = FirebaseDatabase.getInstance().getReference().child("Users_questions");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mprogress = new ProgressDialog(this);
        mBody = (EditText) findViewById(R.id.quiz_body);
        mTitle = (EditText) findViewById(R.id.quiz_title);
        auth = FirebaseAuth.getInstance();
        saveBtn = (Button) findViewById(R.id.save_btn);

        mDatabaseUsersQuiz.child(auth.getCurrentUser().getUid()).child(QuizKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                QuizBody = dataSnapshot.child("question_body").getValue().toString();
                QuizTitle = dataSnapshot.child("question_title").getValue().toString();

                mTitle.setText(QuizTitle);

                if (QuizBody == null) {

                    mBody.setVisibility(View.GONE);

                } else {

                    mBody.setText(QuizBody);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSave();
            }
        });
    }

    private void initSave() {


            mprogress.setMessage("Saving, please wait...");
            mprogress.show();

            final String new_body = mBody.getText().toString();
            final String new_title = mTitle.getText().toString();

            mDatabaseUsersQuiz.child(auth.getCurrentUser().getUid()).child(QuizKey).child("question_title").setValue(new_title);
            mDatabaseUsersQuiz.child(auth.getCurrentUser().getUid()).child(QuizKey).child("question_body").setValue(new_body);

        mDatabaseUsersQuiz.child(auth.getCurrentUser().getUid()).child(QuizKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String Question_key = dataSnapshot.child("question_key").getValue().toString();

                mDatabaseQuestions.child(Question_key).child("question_title").setValue(new_title);
                mDatabaseQuestions.child(Question_key).child("question_body").setValue(new_body);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
                mprogress.dismiss();

                Toast.makeText(EditQuestionActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();

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
