package com.brainy.brainy;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab1Questions extends Fragment {

    String myCurrentChats = null;
    private TextView mNoPostTxt;
    private ImageView mNoPostImg;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseUnread, mDatabaseNotification, mDatabaseViews;
    private DatabaseReference mDatabaseChatroom, mDatabaseHashtag, mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView mQuestionsList;
    private ProgressBar mProgressBar;

    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab1_questions, container, false);

        mViewPager = (ViewPager) v.findViewById(R.id.container);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

       // mNoPostImg = (ImageView) v.findViewById(R.id.noPostChat);
        mNoPostTxt = (TextView) v.findViewById(R.id.noPostTxt);
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mQuestionsList = (RecyclerView) v.findViewById(R.id.Questions_list);
        mQuestionsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mQuestionsList.setHasFixedSize(true);
        mDatabaseChatroom.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabase.keepSynced(true);
        return v;

    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Question, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Question, LetterViewHolder>(

                Question.class,
                R.layout.question_row,
                LetterViewHolder.class,
                mDatabase


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final Question model, int position) {

                final String post_key = getRef(position).getKey();
                final String PostKey = getRef(position).getKey();

                viewHolder.setSender_name(model.getSender_name());
                viewHolder.setPosted_date(model.getPosted_date());
                viewHolder.setQuestion_body(model.getQuestion_body());
                viewHolder.setQuestion_title(model.getQuestion_title());
                viewHolder.setSender_image(getContext(), model.getSender_image());

/*
                //IF USER HAS NO UNREAD MESSAGE, MAKE COUNTER GONE
                mDatabaseUnread.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            viewHolder.counterTxt.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.counterTxt.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
*/


            }


        };

        mQuestionsList.setAdapter(firebaseRecyclerAdapter);

    }

    void refreshItems() {
        // Load items
        // ...

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }



    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        CircleImageView mPostImg;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setPosted_date(String posted_date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(posted_date);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setQuestion_body(String question_body) {

            TextView post_body = (TextView) mView.findViewById(R.id.post_quiz_body);
            post_body.setText(question_body);
        }

        public void setQuestion_title(String question_title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            post_title.setText(question_title);
        }

        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

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
