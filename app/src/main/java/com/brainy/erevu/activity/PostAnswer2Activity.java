package com.brainy.erevu.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brainy.erevu.R.layout.spinner_item;



public class PostAnswer2Activity extends AppCompatActivity {

    private DatabaseReference mDatabaseDiscussForum, mDatabaseUsers, mDatabaseUsersInbox, mDatabaseQuestions, mDatabaseUsersAns, mDatabaseAnswers;
    private EditText answerInput;
    private Button mSubmit, postBtn;
    private ImageView tagImg, addPhoto, ansImage;
    private FirebaseAuth auth;
    String QuizKey = null;
    String questionBodyTag;
    String  personName = null;
    String sender_name = null;
    String sender_image = null;
    Long users_points = null;
    String  personEmail = null;
    String  personId = null;

    Uri personPhoto = null;
    String city = null;
    String state = null;
    String country = null;

    private Uri resultUri = null;
   // private Uri downloadUrl = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    private StorageReference mStorage;
    private ProgressDialog mProgress;

    List<String> subTopicList;
    String[] sub_topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_answer2);
        Window window = PostAnswer2Activity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( PostAnswer2Activity.this, R.color.colorPrimaryDark));

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
        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                users_points = (Long) dataSnapshot.child("points_earned").getValue();


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

        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();

        ansImage = (ImageView) findViewById(R.id.ansImage);
        addPhoto = (ImageView) findViewById(R.id.addPhoto);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (users_points > 99 || users_points == 100) {
                    ansImage.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(PostAnswer2Activity.this, "Sorry! you don't have enough reputation to post a photo.", Toast.LENGTH_LONG).show();
                }
            }
        });

        ansImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        postBtn = (Button) findViewById(R.id.postBtn);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();

            }
        });*/
        mDatabaseUsers.keepSynced(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setAspectRatio(2, 2)
                    .start(PostAnswer2Activity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                ansImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void startPosting() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);
        final String stringDate2 = DateFormat.getDateInstance().format(date);


        final String answer = answerInput.getText().toString().trim();
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(PostAnswer2Activity.this, "Please type your answer.",
                    Toast.LENGTH_LONG).show();
        } else {

            if (resultUri == null) {
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

                                //DELETE UNANSWERED CHILD FROM DATABASE
                                if (dataSnapshot.hasChild("Unanswered")) {
                                    mDatabaseQuestions.child(QuizKey).child("Unanswered").removeValue();
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
                Toast.makeText(PostAnswer2Activity.this, "Answer posted successfully.",
                        Toast.LENGTH_LONG).show();
                finish();

            } else {

                mProgress.setMessage("Posting question, please wait...");
                mProgress.setCancelable(false);

                mProgress.show();

                StorageReference filepath = mStorage.child("answer_images").child(resultUri.getLastPathSegment());

                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

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

                                    //DELETE UNANSWERED CHILD FROM DATABASE
                                    if (dataSnapshot.hasChild("Unanswered")) {
                                        mDatabaseQuestions.child(QuizKey).child("Unanswered").removeValue();
                                    }

                                    Map<String, Object> mapAns = new HashMap<>();
                                    mapAns.put("question_title", question_title);
                                    mapAns.put("posted_answer", answer);
                                    mapAns.put("answer_photo", downloadUrl.toString());
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
                                    newPost2.child("answer_photo").setValue(downloadUrl.toString());
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

                    }

                });

                answerInput.getText().clear();
                Toast.makeText(PostAnswer2Activity.this, "Answer posted successfully.",
                        Toast.LENGTH_LONG).show();
                finish();

            }

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
                            map.put("inbox_type", "quiz_notification");
                            //map.put("answer_photo", downloadUrl.toString());
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
