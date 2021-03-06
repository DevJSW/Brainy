package com.brainy.erevu.tabs;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brainy.erevu.Adapters.AnswersAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Answer;
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
 * A simple {@link Fragment} subclass.
 */
public class AnsProfileTab extends Fragment {

    private RecyclerView mAnsList;
    private DatabaseReference mDatabaseUsers, mDatabaseAnswers, mDatabaseUsersInbox;
    private FirebaseAuth auth;
    private TextView noAns;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String QuizKey = null;

    AnswersAdapter answersAdapter;
    private final List<Answer> answerList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;


    public AnsProfileTab() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ans_profile_tab, container, false);

        // Database channels
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseAnswers = FirebaseDatabase.getInstance().getReference().child("Users_answers");
        mDatabaseUsersInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");

        auth = FirebaseAuth.getInstance();

        noAns = (TextView) v.findViewById(R.id.noAnsTxt);

        // SYNC DATABASE
        mDatabaseAnswers.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        answersAdapter = new AnswersAdapter(getActivity(),answerList);
        mAnsList = (RecyclerView) v.findViewById(R.id.mAnsList);
        mLinearlayout = new LinearLayoutManager(getActivity());
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mAnsList.setHasFixedSize(true);
        mAnsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAnsList.setAdapter(answersAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        currentPage++;
                        answerList.clear();
                        LoadMessage();


                    }
                },3000);

            }
        });

        answerList.clear();
        LoadMessage();

        return v;
    }

    private void LoadMessage() {

        Query quizQuery = mDatabaseAnswers.child(auth.getCurrentUser().getUid()).limitToFirst(50);
        quizQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null) {

                    noAns.setVisibility(View.VISIBLE);
                } else {

                    noAns.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Answer message = dataSnapshot.getValue(Answer.class);

                answerList.add(message);
                answersAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

              /*  mQuestionsList.scrollToPosition(questionList.size()-1);*/

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

}
