package com.brainy.erevu.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.MessagesAdapter;
import com.brainy.erevu.Pojos.Answer;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Chat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxActivity extends AppCompatActivity {

    private ImageView backBtn, writeMessage;
    private FirebaseAuth auth;
    private TextView mNoNotification;
    private DatabaseReference mDatabaseUsers, mDatabaseUserInbox, mDatabase;
    private RecyclerView mMessageList;

    MessagesAdapter chatAdapter;
    private final List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // QuizKey = getIntent().getExtras().getString("question_id");

        Window window = InboxActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( InboxActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // AUTH
        auth = FirebaseAuth.getInstance();

        // database channels
        mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        if (auth.getCurrentUser() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_inbox").child(auth.getCurrentUser().getUid());
            mDatabase.keepSynced(true);
        }

        // SYNC DATABASE
        mDatabaseUserInbox.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        mNoNotification = (TextView) findViewById(R.id.noNotyTxt);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoNotification.setVisibility(View.VISIBLE);
                        mNoNotification.setText("Your inbox is empty.");
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
            mNoNotification.setText("All your inbox will be collected here!");
        }

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InboxActivity.this.finish();
            }
        });


        chatAdapter = new MessagesAdapter(InboxActivity.this, chatList);
        mMessageList = (RecyclerView) findViewById(R.id.Messages_list);
        mLinearlayout = new LinearLayoutManager(InboxActivity.this);
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearlayout);
        mMessageList.setAdapter(chatAdapter);

        //LoadMessage();
    }

    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView post_reason, post_name, post_answer;
        private DatabaseReference mDatabase, mDatabaseUserFavourites, mDatabaseUserInbox;
        FirebaseAuth auth;
        Typeface custom_font;
        ProgressBar mProgressBar;
        Context context;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
            post_answer = (TextView) mView.findViewById(R.id.posted_answer);
            post_name = (TextView) mView.findViewById(R.id.post_name);
            post_reason = (TextView) mView.findViewById(R.id.posted_reason);
            auth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_inbox").child(auth.getCurrentUser().getUid());
            mDatabase.keepSynced(true);
        }

        public void setPosted_date(Long posted_date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            post_date.setReferenceTime(Long.parseLong(String.valueOf(posted_date)));
        }

        public void setPosted_reason(String posted_reason) {

            TextView post_reason = (TextView) mView.findViewById(R.id.posted_reason);
            post_reason.setText(posted_reason);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }


        public void setPosted_answer(String posted_answer) {

            TextView post_title = (TextView) mView.findViewById(R.id.posted_answer);
            post_title.setText(posted_answer);
        }

        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.sender_image);

            Glide.with(ctx)
                    .load(sender_image).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(civ) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            civ.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }

    }
    @Override
    public void onResume() {
        super.onResume();

        if (auth.getCurrentUser() != null) {
            FirebaseRecyclerAdapter<Answer, InboxActivity.LetterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Answer, InboxActivity.LetterViewHolder>(

                    Answer.class,
                    R.layout.inbox_item,
                    InboxActivity.LetterViewHolder.class,
                    mDatabaseUserInbox.child(auth.getCurrentUser().getUid())


            ) {
                @Override
                protected void populateViewHolder(final InboxActivity.LetterViewHolder viewHolder, final Answer model, int position) {

                    final String quiz_key = getRef(position).getKey();
                    final String PostKey = getRef(position).getKey();

                    viewHolder.setSender_name(model.getSender_name());
                    viewHolder.setPosted_date(model.getPosted_date());
                    viewHolder.setPosted_reason(model.getPosted_reason());
                    viewHolder.setPosted_answer(model.getPosted_answer());
                    viewHolder.setSender_image(InboxActivity.this,model.getSender_image());

                    mDatabase.child(quiz_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();

                            if (read_status.equals(true) ) {

                                viewHolder.post_answer.setTextColor(Color.parseColor("#ABABAB"));
                                viewHolder.post_reason.setTextColor(Color.parseColor("#FF75B4E9"));
                                viewHolder.post_name.setTextColor(Color.parseColor("#FFE99639"));

                            } else {

                                viewHolder.post_answer.setTextColor(Color.BLACK);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDatabase.child(quiz_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String Quiz_key = dataSnapshot.child("question_key").getValue().toString();

                                    //MARK MESSAGE AS READ ON DB
                                    mDatabase.child(quiz_key).child("read").setValue(true);

                                    Intent openRead = new Intent(InboxActivity.this, ReadQuestionActivity.class);
                                    openRead.putExtra("question_id", Quiz_key );
                                    startActivity(openRead);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            // custom dialog
                            final Dialog dialog = new Dialog(InboxActivity.this);
                            dialog.setContentView(R.layout.inbox_popup_dialog);
                            dialog.setTitle("Inbox Options");

                            LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                            deleteLiny.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    AlertDialog diaBox = AskOption();
                                    diaBox.show();
                                    dialog.dismiss();
                                }
                            });


                            // if button is clicked, close the custom dialog

                            dialog.show();
                            return false;
                        }

                        private AlertDialog AskOption() {
                            AlertDialog myQuittingDialogBox =new AlertDialog.Builder(InboxActivity.this)
                                    //set message, title, and icon
                                    .setTitle("Delete Alert!")
                                    .setMessage("Are you sure you want to remove this message from your inbox!")

                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //your deleting code

                                            mDatabase.child(quiz_key).removeValue();
                                            dialog.dismiss();
                                            Toast.makeText(InboxActivity.this, "Message deleted!",Toast.LENGTH_SHORT).show();
                                        }

                                    })


                                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();

                                        }
                                    })
                                    .create();
                            return myQuittingDialogBox;
                        }
                    });


                }


            };

            mMessageList.setAdapter(firebaseRecyclerAdapter);

        }

    }

}
