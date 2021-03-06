package com.brainy.erevu.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Date;

public class ChannelProfileActivity extends AppCompatActivity {

    String group_id = null;
    String founder_id = null;
    String group_name = null;
    String group_image = null;
    String created_date = null;
    String group_founder = null;
    String currentuser_name = null;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseGroups, mDatabaseUserGroups, mDatabaseGroupChats, mDatabaseGroupChatlist;
    private FloatingActionButton fabAlbum;
    private FirebaseAuth auth;
    private StorageReference mStorage;
    private ImageView userAvator, starred,backBtn;
    private LinearLayout exitGroup;
    private RecyclerView participantsList;
    private TextView subscriberCounter, invite, leave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Window window = ChannelProfileActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(ChannelProfileActivity.this, R.color.colorPrimary));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        group_id = getIntent().getStringExtra("group_id");
        founder_id = getIntent().getStringExtra("founder_id");
        group_name = getIntent().getStringExtra("group_name");
        group_image = getIntent().getStringExtra("group_image");
        //name = getIntent().getStringExtra("name");

        userAvator = (ImageView) findViewById(R.id.user_avator);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);
        auth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseGroups = FirebaseDatabase.getInstance().getReference().child("Channels");
        mDatabaseUserGroups = FirebaseDatabase.getInstance().getReference().child("Users_groups");
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        mDatabaseGroupChats = FirebaseDatabase.getInstance().getReference().child("Group_chats");

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentuser_name = dataSnapshot.child("username").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        subscriberCounter = (TextView) findViewById(R.id.subscribers);

        //COUNT NUMBER OF SUBSCRIBERS
        mDatabaseGroups.child(group_id).child("Participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                subscriberCounter.setText(dataSnapshot.getChildrenCount() + " Subscribers");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        invite= (TextView) findViewById(R.id.invite);
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody ="Erevu Android App";
                String shareSub = "Hey, this is an invitation to join this channel on Erevu "+group_name+" https://play.google.com/store/apps/details?id=com.erevu.erevu ";
                myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
                myIntent.putExtra(Intent.EXTRA_TEXT,shareSub);
                startActivity(Intent.createChooser(myIntent,"Invite to channel"));
            }
        });

        leave = (TextView) findViewById(R.id.leave);
        exitGroup = (LinearLayout) findViewById(R.id.exitLay);
        if (auth.getCurrentUser().getUid().equals(founder_id)) {
            leave.setText("Edit Channel");
        } else {
            leave.setText("Exit Channel");
        }
        exitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser().getUid().equals(founder_id)) {
                    Intent openActivity = new Intent(ChannelProfileActivity.this, EditChannelActivity.class);
                    openActivity.putExtra("group_id", group_id );
                    openActivity.putExtra("group_name", group_name );
                    openActivity.putExtra("group_founder", group_founder);
                    openActivity.putExtra("group_image", group_image );
                    startActivity(openActivity);
                } else {
                    //ADD USER TO PARTICIPANTS LISTS
                    final Context context = ChannelProfileActivity.this;

                    // custom dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.exit_dialog);
                    dialog.setTitle("Options");
                    dialog.show();

                    RelativeLayout addUserToGroup = (RelativeLayout) dialog.findViewById(R.id.addUserToGroup);
                    addUserToGroup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //exit form group
                            mDatabaseGroups.child(group_id).child("Participants").child(auth.getCurrentUser().getUid()).removeValue();
                            //exit from group users
                            mDatabaseUserGroups.child(auth.getCurrentUser().getUid()).child(group_id).removeValue();

                            //SEND ALERT MESSAGE
                            final DatabaseReference newGroupChats = mDatabaseGroupChats.child(group_id).push();
                            final DatabaseReference newGroupChatlist = mDatabaseGroupChatlist.child(group_id);
                            Date date = new Date();
                            final String stringDate = DateFormat.getDateTimeInstance().format(date);
                            final String stringDate2 = DateFormat.getDateInstance().format(date);

                       /* newGroupChats.child("message").setValue(currentuser_name+ " left on "+stringDate2);
                        newGroupChats.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                        newGroupChats.child("sender_name").setValue(currentuser_name);
                        newGroupChats.child("message_type").setValue("NOTIFICATION");
                        newGroupChats.child("posted_date").setValue(System.currentTimeMillis());
                        newGroupChats.child("post_id").setValue(newGroupChats.getKey());*/

                            ChannelProfileActivity.this.finish();
                            Toast.makeText(ChannelProfileActivity.this, "Exit successfully!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        mDatabaseUsers.keepSynced(true);
        mDatabaseGroups.keepSynced(true);

        Glide.with(ChannelProfileActivity.this).load(group_image).into(userAvator);

        collapsingToolbarLayout.setTitle(group_name);
        final Typeface aller_font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");
        collapsingToolbarLayout.setExpandedTitleTypeface(aller_font);

        FloatingActionButton call = (FloatingActionButton) findViewById(R.id.fab);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChannelProfileActivity.this.finish();

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
