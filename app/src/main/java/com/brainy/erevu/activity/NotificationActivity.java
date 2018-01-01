package com.brainy.erevu.activity;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.erevu.Adapters.InboxAdapter;
import com.brainy.erevu.Adapters.NotificationAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.data.Answer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    String QuizKey = null;

    NotificationAdapter inboxAdapter;
    private final List<Answer> questionList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private DatabaseReference mDatabaseUserInbox, mDatabase, mDatabaseNotifications;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth auth;
    private TextView mNoNotification;
    private RecyclerView mNotyList;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // QuizKey = getIntent().getExtras().getString("question_id");

        Window window = NotificationActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( NotificationActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // Database channels
        mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Users_notifications");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUserInbox.keepSynced(true);
        mDatabaseNotifications.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        mNoNotification = (TextView) findViewById(R.id.noNotyTxt);
        inboxAdapter = new NotificationAdapter(NotificationActivity.this, questionList);

        mNotyList = (RecyclerView) findViewById(R.id.Notification_list);
        mLinearlayout = new LinearLayoutManager(NotificationActivity.this);
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mNotyList.setHasFixedSize(true);
        mNotyList.setLayoutManager(mLinearlayout);
        mNotyList.setAdapter(inboxAdapter);

      /*  questionList.clear();
        if (auth.getCurrentUser() != null) {
            LoadMessage();
        } else {}
*/
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mDatabaseNotifications.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoNotification.setVisibility(View.VISIBLE);
                        mNoNotification.setText("You have no Notifications.");
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
            mNoNotification.setText("All your notifications will be collected here!");
        }

       // progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onResume() {
        super.onResume();
        questionList.clear();
        if (auth.getCurrentUser() != null) {
            LoadMessage();
        } else {}
    }

    private void LoadMessage() {
        Query quizQuery = mDatabaseNotifications.child(auth.getCurrentUser().getUid()).limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Answer message = dataSnapshot.getValue(Answer.class);
                questionList.add(message);
                inboxAdapter.notifyDataSetChanged();
                inboxAdapter.notifyItemInserted(0);


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
