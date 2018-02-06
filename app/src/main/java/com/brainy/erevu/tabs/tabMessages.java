package com.brainy.erevu.tabs;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.erevu.Adapters.MessagesAdapter;
import com.brainy.erevu.Pojos.Chat;
import com.brainy.erevu.R;
import com.brainy.erevu.activity.SigninActivity;
import com.google.android.gms.common.api.GoogleApiClient;
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


/**
 * A fragment with a Google +1 button.
 */
public class tabMessages extends Fragment {

    private Button signIn;
    private DatabaseReference mDatabaseMessages, mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth auth;
    private TextView mNoInbox;
    private RecyclerView mMessageList;

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

    private void LoadMessage() {
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

    @Override
    public void onStart() {
        super.onStart();
        chatList.clear();
        if (auth.getCurrentUser() != null) {
            LoadMessage();
        } else {}
    }

   /* @Override
    public void onResume() {
        super.onResume();
        chatList.clear();
        if (auth.getCurrentUser() != null) {
            LoadMessage();
        } else {}
    }*/


}
