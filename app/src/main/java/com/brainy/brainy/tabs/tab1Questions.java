package com.brainy.brainy.tabs;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.brainy.Adapters.QuestionAdapter;
import com.brainy.brainy.activity.AnswersActivity;
import com.brainy.brainy.activity.MainActivity;
import com.brainy.brainy.activity.ReadQuestionActivity;
import com.brainy.brainy.data.CustomOnItemSelectedListener;
import com.brainy.brainy.data.Question;
import com.brainy.brainy.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.srx.widget.PullToLoadView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.brainy.brainy.R.layout.spinner_item;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab1Questions extends Fragment implements SearchView.OnQueryTextListener {

    String myCurrentChats = null;
    String selectedTopic = null;
    String selectedOption = null;
    private TextView mNoPostTxt;
    private ImageView mNoPostImg;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseFavourite;
    private DatabaseReference mDatabaseChatroom,  mDatabaseViews, mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView mQuestionsList;
    private ProgressBar mProgressBar;
    private Spinner spinner1;


    private ViewPager mViewPager;
    PullToLoadView pullToLoadView;

    QuestionAdapter questionAdapter;
    private final List<Question> questionList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage =1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tab1_questions, container, false);

        // Get reference of widgets from XML layout
        final Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        final Spinner spinner2 = (Spinner) v.findViewById(R.id.spinner2);

        // Initializing a String Array
        String[] topics = new String[]{
                "Select a topic...",
                "Math",
                "Computer science",
                "Economics",
                "Languages",
                "Physics",
                "Chemistry",
                "Aviation",
                "Health Science"
        };

        // Initializing a String Array
        String[] options = new String[]{
                "Active",
                "Unanswered"
        };

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);

        final List<String> topicList = new ArrayList<>(Arrays.asList(topics));
        final List<String> optionList = new ArrayList<>(Arrays.asList(options));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item,topicList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(spinner_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTopic = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                   /* Toast.makeText
                            (getActivity(), "Selected : " + selectedTopic, Toast.LENGTH_SHORT)
                            .show();*/

                    mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
                    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    currentPage++;
                                    questionList.clear();
                                    topicFilterMessage();


                                }
                            },3000);

                        }
                    });

                    questionList.clear();
                    topicFilterMessage();


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        // Initializing an ArrayAdapter2
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item,optionList){

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                tv.setTextColor(Color.BLACK);
                return view;
            }
        };
        spinnerArrayAdapter2.setDropDownViewResource(spinner_item);
        spinner2.setAdapter(spinnerArrayAdapter2);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                    // Notify the selected item text

                    //LOAD LATEST UNANSWERED QUESTIONS
                    if (selectedOption == "Unanswered") {

                        //DISABLE THE FIRST SPINNER
                        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                getActivity(), R.layout.spinner_item,topicList){
                            @Override
                            public boolean isEnabled(int position){
                                if(position == 0)
                                {
                                    // Disable the first item from Spinner
                                    // First item will be use for hint
                                    return false;
                                }
                                else
                                {
                                    return true;
                                }
                            }
                            @Override
                            public View getDropDownView(int position, View convertView,
                                                        ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView tv = (TextView) view;
                                if(position == 0){
                                    // Set the hint text color gray
                                    tv.setTextColor(Color.GRAY);
                                }
                                else {
                                    tv.setTextColor(Color.BLACK);
                                }
                                return view;
                            }
                        };

                        spinnerArrayAdapter.setDropDownViewResource(spinner_item);
                        spinner.setAdapter(spinnerArrayAdapter);

                        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
                        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        currentPage++;
                                        questionList.clear();
                                        unansweredFilterMessage();


                                    }
                                }, 3000);

                            }
                        });

                        questionList.clear();
                        unansweredFilterMessage();

                        //LOAD LATEST QUESTIONS
                    } else if (selectedOption == "Active") {

                        //DISABLE THE FIRST SPINNER
                        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                getActivity(), R.layout.spinner_item,topicList){
                            @Override
                            public boolean isEnabled(int position){
                                if(position == 0)
                                {
                                    // Disable the first item from Spinner
                                    // First item will be use for hint
                                    return false;
                                }
                                else
                                {
                                    return true;
                                }
                            }
                            @Override
                            public View getDropDownView(int position, View convertView,
                                                        ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView tv = (TextView) view;
                                if(position == 0){
                                    // Set the hint text color gray
                                    tv.setTextColor(Color.GRAY);
                                }
                                else {
                                    tv.setTextColor(Color.BLACK);
                                }
                                return view;
                            }
                        };

                        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
                        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        currentPage++;
                                        questionList.clear();
                                        LoadMessage();


                                    }
                                },3000);

                            }
                        });

                        questionList.clear();
                        LoadMessage();

                    }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // AUTH
        mAuth = FirebaseAuth.getInstance();

        // database channels
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);

        mViewPager = (ViewPager) v.findViewById(R.id.container);

       // mNoPostImg = (ImageView) v.findViewById(R.id.noPostChat);
        mNoPostTxt = (TextView) v.findViewById(R.id.noPostTxt);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        /*mQuestionsList = (RecyclerView) v.findViewById(R.id.Questions_list);
        mQuestionsList.setHasFixedSize(true);
        mQuestionsList.setLayoutManager(new LinearLayoutManager(getActivity()));
*/


        pullToLoadView = (PullToLoadView) v.findViewById(R.id.pullToLoadView);
        mQuestionsList = pullToLoadView.getRecyclerView();
        pullToLoadView.isLoadMoreEnabled(true);

        questionAdapter = new QuestionAdapter(getActivity(),questionList);

        mQuestionsList = (RecyclerView) v.findViewById(R.id.Questions_list);
        mLinearlayout = new LinearLayoutManager(getActivity());

        mQuestionsList.setHasFixedSize(true);
        mQuestionsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mQuestionsList.setAdapter(questionAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                            currentPage++;
                            questionList.clear();
                            LoadMessage();


                    }
                },3000);

            }
        });

        questionList.clear();
        LoadMessage();

        return v;

    }

    private void LoadUnViewedMessage() {

        Query filterQuery = mDatabase.orderByChild("views").limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        filterQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Question message = dataSnapshot.getValue(Question.class);
                if (dataSnapshot.getChildrenCount()  == 0) {

                    questionList.add(message);
                    questionAdapter.notifyDataSetChanged();

                    mSwipeRefreshLayout.setRefreshing(false);
                } else {


                }

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

    private void unansweredFilterMessage() {

        Query filterQuery = mDatabase.orderByChild(selectedOption).limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        filterQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Question message = dataSnapshot.getValue(Question.class);

                questionList.add(message);
                questionAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

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

    private void topicFilterMessage() {

        Query filterQuery = mDatabase.orderByChild("tag").equalTo(selectedTopic).limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        filterQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Question message = dataSnapshot.getValue(Question.class);

                questionList.add(message);
                questionAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);


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

    private void LoadMessage() {

        Query quizQuery = mDatabase.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Question message = dataSnapshot.getValue(Question.class);

                questionList.add(message);
                questionAdapter.notifyDataSetChanged();

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


    public void setPullToLoadView(PullToLoadView pullToLoadView) {
        this.pullToLoadView = pullToLoadView;
    }



    @Override
    public void onStart() {
        super.onStart();

       /* LoadMessage();*/

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                FirebaseRecyclerAdapter<Question, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Question, LetterViewHolder>(

                        Question.class,
                        R.layout.question_row,
                        LetterViewHolder.class,
                        mDatabase.orderByChild("question_title").startAt(newText.toUpperCase())


                ) {
                    @Override
                    protected void populateViewHolder(final LetterViewHolder viewHolder, final Question model, int position) {

                        final String quiz_key = getRef(position).getKey();
                        final String PostKey = getRef(position).getKey();

                        viewHolder.setSender_name(model.getSender_name());
                        viewHolder.setPosted_date(model.getPosted_date());
                        viewHolder.setQuestion_body(model.getQuestion_body());
                        viewHolder.setQuestion_title(model.getQuestion_title());
                        viewHolder.setSender_image(getContext(), model.getSender_image());


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (mAuth.getCurrentUser() != null) {
                                    mDatabase.child(quiz_key).child("views").child(mAuth.getCurrentUser().getUid()).setValue("iView");
                                }
                                Intent openRead = new Intent(getActivity(), ReadQuestionActivity.class);
                                openRead.putExtra("question_id", quiz_key );
                                startActivity(openRead);
                            }
                        });

                        viewHolder.answer_rely.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent openRead = new Intent(getActivity(), AnswersActivity.class);
                                openRead.putExtra("question_id", quiz_key );
                                startActivity(openRead);
                            }
                        });

                        // count number of views in a hashtag
                        mDatabase.child(quiz_key).child("views").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                viewHolder.viewCounter.setText(dataSnapshot.getChildrenCount() + "");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        // count number of answers
                        mDatabase.child(quiz_key).child("Answers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                viewHolder.answersCounter.setText(dataSnapshot.getChildrenCount() + "");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        // count number of favourites
                        mDatabase.child(quiz_key).child("favourite").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                viewHolder.favouritesCounter.setText(dataSnapshot.getChildrenCount() + "");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }


                };

                mQuestionsList.setAdapter(firebaseRecyclerAdapter);
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {

                                          }
                                      }
        );
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        FirebaseRecyclerAdapter<Question, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Question, LetterViewHolder>(

                Question.class,
                R.layout.question_row,
                LetterViewHolder.class,
                mDatabase.orderByChild("question_title").startAt(s.toUpperCase())


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final Question model, int position) {

                final String quiz_key = getRef(position).getKey();
                final String PostKey = getRef(position).getKey();

                viewHolder.setSender_name(model.getSender_name());
                viewHolder.setPosted_date(model.getPosted_date());
                viewHolder.setQuestion_body(model.getQuestion_body());
                viewHolder.setQuestion_title(model.getQuestion_title());
                viewHolder.setSender_image(getContext(), model.getSender_image());


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mAuth.getCurrentUser() != null) {
                            mDatabase.child(quiz_key).child("views").child(mAuth.getCurrentUser().getUid()).setValue("iView");
                        }
                        Intent openRead = new Intent(getActivity(), ReadQuestionActivity.class);
                        openRead.putExtra("question_id", quiz_key );
                        startActivity(openRead);
                    }
                });

                viewHolder.answer_rely.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openRead = new Intent(getActivity(), AnswersActivity.class);
                        openRead.putExtra("question_id", quiz_key );
                        startActivity(openRead);
                    }
                });

                // count number of views in a hashtag
                mDatabase.child(quiz_key).child("views").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        viewHolder.viewCounter.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // count number of answers
                mDatabase.child(quiz_key).child("Answers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        viewHolder.answersCounter.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // count number of favourites
                mDatabase.child(quiz_key).child("favourite").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        viewHolder.favouritesCounter.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }


        };

        mQuestionsList.setAdapter(firebaseRecyclerAdapter);
        return false;
    }


    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView viewCounter, answersCounter, favouritesCounter;
        RelativeLayout answer_rely;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);

            answer_rely = (RelativeLayout) mView.findViewById(R.id.anser_rely);
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
