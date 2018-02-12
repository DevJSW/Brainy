package com.brainy.erevu.tabs;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.MessagesAdapter;
import com.brainy.erevu.Pojos.Chat;
import com.brainy.erevu.Pojos.Question;
import com.brainy.erevu.R;
import com.brainy.erevu.activity.MainActivity;
import com.brainy.erevu.activity.MessageChatActivity;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.activity.SearchActivity;
import com.brainy.erevu.activity.SigninActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.common.api.GoogleApiClient;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * A fragment with a Google +1 button.
 */
public class tabMessages extends Fragment {

    String sender_username = "";
    String sender_name = "";
    String sender_image = "";
    private Button signIn;
    private DatabaseReference mDatabaseMessages, mDatabase, mDatabaseChats;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth auth;
    private TextView mNoInbox;
    private RecyclerView mMessageList;
    public RelativeLayout counter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    MessagesAdapter chatAdapter;
    private final List<Chat> chatList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab2_inbox, container, false);

        // Database channels
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference().child("Users_messages");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseChats = FirebaseDatabase.getInstance().getReference().child("User_chats");
        counter = (RelativeLayout) view.findViewById(R.id.counter);
        // SYNC DATABASE
        mDatabaseMessages.keepSynced(true);
        mDatabaseUsers.keepSynced(true);


        //auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            TextView noPostTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout inbox_layout = (RelativeLayout) view.findViewById(R.id.inbox_layout);

            inbox_layout.setVisibility(View.VISIBLE);
            noPostTxt.setVisibility(View.GONE);
            signinBtn.setVisibility(View.GONE);
        } else {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            TextView noPostTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout inbox_layout = (RelativeLayout) view.findViewById(R.id.inbox_layout);

            inbox_layout.setVisibility(View.GONE);
            noPostTxt.setVisibility(View.VISIBLE);
            signinBtn.setVisibility(View.VISIBLE);
        }

        mNoInbox = (TextView) view.findViewById(R.id.noInboxTxt);
        chatAdapter = new MessagesAdapter(getActivity(), chatList);

        mMessageList = (RecyclerView) view.findViewById(R.id.Inbox_list);
        mLinearlayout = new LinearLayoutManager(getActivity());
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearlayout);
        mMessageList.setAdapter(chatAdapter);


        if (auth.getCurrentUser() != null) {
            mDatabaseMessages.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoInbox.setVisibility(View.VISIBLE);
                        mNoInbox.setText("You have no Messages.");
                    } else {
                        mNoInbox.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {}

        signIn = (Button) view.findViewById(R.id.signIn);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        //Get Firebase auth instance

        initSignIn();
        return view;
    }

    private void initSignIn() {

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openRead = new Intent(getActivity(), SigninActivity.class);
                startActivity(openRead);
                //showSignInDialog();
            }
        });
    }

   /* private void LoadMessage() {
        Query quizQuery = mDatabaseMessages.child(auth.getCurrentUser().getUid());

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat message = dataSnapshot.getValue(Chat.class);
                chatList.add(message);
                chatAdapter.notifyDataSetChanged();
                chatAdapter.notifyItemInserted(0);

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
*/
    private void loadMessage() {

        FirebaseRecyclerAdapter<Chat, tabMessages.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat,  tabMessages.UsersViewHolder>(

                Chat.class,
                R.layout.chatlist_item,
                tabMessages.UsersViewHolder.class,
                mDatabaseMessages.child(auth.getCurrentUser().getUid())

        ) {
            @Override
            protected void populateViewHolder(final tabMessages.UsersViewHolder viewHolder, final Chat model, int position) {

                mDatabaseUsers.child(model.getPost_id() ).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        sender_username = dataSnapshot.child("username").getValue().toString();
                        sender_name = dataSnapshot.child("name").getValue().toString();
                        sender_image = dataSnapshot.child("user_image").getValue().toString();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setDetails(getActivity(), model.getMessage(), model.getSender_image(), model.getSender_name(), model.getSender_username(), model.getPosted_date());

                mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(model.getSender_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("read")) {
                            Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();
                            if (read_status != null) {
                                if (read_status.equals(false)) {
                                    viewHolder.counter.setVisibility(View.VISIBLE);

                                } else {

                                    viewHolder.counter.setVisibility(View.GONE);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //MARK MESSAGE AS READ ON DB

                        mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(model.getSender_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild("read")) {
                                    Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();

                                    if (read_status != null) {
                                        if (read_status.equals(false)) {
                                            mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(model.getSender_uid()).child("read").setValue(true);
                                            viewHolder.counter.setVisibility(View.VISIBLE);

                                        } else {

                                            viewHolder.counter.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        Intent openActivity = new Intent(getActivity(), MessageChatActivity.class);
                        openActivity.putExtra("user_id", model.getSender_uid() );
                        openActivity.putExtra("name", model.getSender_name() );
                        openActivity.putExtra("username", model.getSender_username());
                        openActivity.putExtra("user_image", model.getSender_image() );
                        startActivity(openActivity);


                    }
                });

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        AlertDialog diaBox = AskOption();
                        diaBox.show();
                        return false;
                    }

                    private AlertDialog AskOption() {
                        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                                //set message, title, and icon
                                .setMessage("Remove this chat")

                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //your deleting code

                                        mDatabaseMessages.child(auth.getCurrentUser().getUid()).child(model.getSender_uid()).removeValue();
                                        mDatabaseChats.child(auth.getCurrentUser().getUid()).child(model.getSender_uid()).removeValue();
                                        dialog.dismiss();
                                        Toast.makeText(getActivity(), "chat deleted!",Toast.LENGTH_SHORT).show();
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

    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        RelativeLayout counter;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            counter = (RelativeLayout) mView.findViewById(R.id.counter);
        }

        public void setDetails(final Context ctx, String message, final String senderImage, String senderName, String senderUname, Long messageDate){

            TextView messageText = (TextView) mView.findViewById(R.id.post_message);
            final CircleImageView sender_image = (CircleImageView) mView.findViewById(R.id.post_image);
            TextView sender_username = (TextView) mView.findViewById(R.id.post_username);
            TextView sender_name = (TextView) mView.findViewById(R.id.post_name);
            RelativeTimeTextView date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);

            messageText.setText(message);
            sender_name.setText(senderName);
            sender_username.setText(senderUname);
            messageText.setText(message);
            date.setReferenceTime(messageDate);

            Glide.with(ctx)
                    .load(senderImage).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(sender_image) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            sender_image.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }


    }

    @Override
    public void onStart() {
        super.onStart();
        chatList.clear();
        if (auth.getCurrentUser() != null) {
            loadMessage();
        } else {}
    }
}
