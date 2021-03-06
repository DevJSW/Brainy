package com.brainy.erevu.tabs;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.brainy.erevu.Adapters.QuestionAdapter;
import com.brainy.erevu.activity.FavouriteActivity;
import com.brainy.erevu.activity.FilterResultsActivity;
import com.brainy.erevu.Pojos.Question;
import com.brainy.erevu.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.srx.widget.PullToLoadView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.brainy.erevu.R.layout.spinner_item;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab1Questions extends Fragment {

    String myCurrentChats = null;
    String selectedTopic = null;
    String selectedOption = null;
    private TextView mNoPostTxt;
    private ImageView mNoPostImg;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseFavourite;
    private DatabaseReference mDatabaseChatroom, mDatabaseViews, mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView mQuestionsList;
    private RecyclerView mQuestionsList2;
    private ProgressBar mProgressBar;
    private Spinner spinner1;
    private ViewPager mViewPager;
    PullToLoadView pullToLoadView;
    private Boolean isLoading = false;
    private Boolean hasLoadedAll = false;
    private int nextPage;

    QuestionAdapter questionAdapter;
    //MainAdapter mainAdapter;
    private ArrayList<Question> questionList;
    private final ArrayList<Object> objects = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final String TAG = "tab1Question";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;

    //PAGINATION
    private static int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;
    private int previousTotal = 9;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mFirstKey = "";
    private String mPrevKey = "";

    private String newestPostId;
    private String oldestPostId;
    private int startAt = 0;

    private int visibleThreshold = 5;
    private int visibleItemCount, totalItemCount, firstVisibleItem, lastVisibleItem;

    List<String> topicList;
    List<String> optionList;
    Spinner spinner;
    Spinner spinner2;

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tab1_questions, container, false);

        // Get reference of widgets from XML layout
        spinner = (Spinner) v.findViewById(R.id.spinner);
        spinner2 = (Spinner) v.findViewById(R.id.spinner2);
        questionList = new ArrayList<>();
        //auth
        pullToLoadView= (PullToLoadView) v.findViewById(R.id.pullToLoadView);

        // Initializing a String Array
        String[] topics = new String[]{
                "All Topics",
                "Math",
                "Agriculture",
                "Computer science & ICT",
                "Business & Economics",
                "Law",
                "Languages",
                "Geography & Geology",
                "History & Government",
                "Physics & Electronics",
                "Chemistry & Chemical science",
                "Medical & Health Science",
                "OTHERS"
        };

        // Initializing a String Array
        String[] options = new String[]{
                "Active",
                "Favourites",
                "Unanswered"
        };

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);

        topicList = new ArrayList<>(Arrays.asList(topics));
        optionList = new ArrayList<>(Arrays.asList(options));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item, topicList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
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
                if (position > 0) {
                    // Notify the selected item text
                   /* Toast.makeText
                            (getActivity(), "Selected : " + selectedTopic, Toast.LENGTH_SHORT)
                            .show();*/
                    Bundle bundle = new Bundle();
                    bundle.putString("selected_topic", selectedTopic );

                    Intent openRead = new Intent(getActivity(), FilterResultsActivity.class);
                    openRead.putExtras(bundle);
                    startActivity(openRead);

                   /* mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
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
                            }, 3000);

                        }
                    });
                    questionList.clear();
                    topicFilterMessage();*/

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        // Initializing an ArrayAdapter2
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item, optionList) {

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
                            getActivity(), R.layout.spinner_item, topicList) {
                        @Override
                        public boolean isEnabled(int position) {
                            if (position == 0) {
                                // Disable the first item from Spinner
                                // First item will be use for hint
                                return false;
                            } else {
                                return true;
                            }
                        }

                        @Override
                        public View getDropDownView(int position, View convertView,
                                                    ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView tv = (TextView) view;
                            if (position == 0) {
                                // Set the hint text color gray
                                tv.setTextColor(Color.GRAY);
                            } else {
                                tv.setTextColor(Color.BLACK);
                            }
                            return view;
                        }
                    };

                    spinnerArrayAdapter.setDropDownViewResource(spinner_item);
                    spinner.setAdapter(spinnerArrayAdapter);

                   // startActivity(new Intent(getContext(), UnansweredActivity.class));

                    //LOAD LATEST QUESTIONS
                } else if (selectedOption == "Active") {

                    //DISABLE THE FIRST SPINNER
                    final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                            getActivity(), R.layout.spinner_item, topicList) {
                        @Override
                        public boolean isEnabled(int position) {
                            if (position == 0) {
                                // Disable the first item from Spinner
                                // First item will be use for hint
                                return false;
                            } else {
                                return true;
                            }
                        }

                        @Override
                        public View getDropDownView(int position, View convertView,
                                                    ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView tv = (TextView) view;
                            if (position == 0) {
                                // Set the hint text color gray
                                tv.setTextColor(Color.GRAY);
                            } else {
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
                            }, 3000);

                        }
                    });

                    questionList.clear();
                    LoadMessage();

                } else if (selectedOption == "Favourites") {
                    startActivity(new Intent(getActivity(), FavouriteActivity.class));
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

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        mViewPager = (ViewPager) v.findViewById(R.id.container);
        mNoPostTxt = (TextView) v.findViewById(R.id.noPostTxt);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        pullToLoadView = (PullToLoadView) v.findViewById(R.id.pullToLoadView);
       /* mQuestionsList2 = pullToLoadView.getRecyclerView();
        mQuestionsList2.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        /*pu*//*llToLoadView.isLoadMoreEnabled(true);*/
       // questionAdapter = new QuestionAdapter(getActivity(), new ArrayList<Question>());

        questionAdapter = new QuestionAdapter(getActivity(), questionList);
       // mainAdapter = new MainAdapter(getActivity(), (ArrayList<Object>) getObject());

        mQuestionsList = (RecyclerView) v.findViewById(R.id.Questions_list);
        mLinearlayout = new LinearLayoutManager(getActivity());
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mQuestionsList.setHasFixedSize(true);
        mQuestionsList.setLayoutManager(mLinearlayout);
        mQuestionsList.setAdapter(questionAdapter);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        itemPos++;
                        currentPage++;
                        questionList.clear();
                        LoadLatestMessage();


                    }
                }, 3000);

            }
        });

        mQuestionsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {

                    progressBar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            currentPage++;
                            itemPos = 0;
                            LoadMoreMessage();
                            progressBar.setVisibility(View.GONE);

                        }
                    }, 3000);


                } else {

                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        return v;

    }

  /*  private List<Object> getObject() {

        objects.add(getVerticalData());
        objects.add(getHorizontalData());
        return objects;
    }

    public static ArrayList<Question> getVerticalData() {
        ArrayList<Question> questionList = new ArrayList<>();
        return questionList;
    }

    public static ArrayList<Question> getHorizontalData() {
        ArrayList<Question> questionList = new ArrayList<>();
        return questionList;
    }*/

    private void LoadLatestMessage() {

        Query quizQuery = mDatabase.orderByKey().startAt(mFirstKey).limitToLast(10);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //questionList.clear();
                Question message = dataSnapshot.getValue(Question.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    questionList.add(itemPos++,message);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {

                    mLastKey = messageKey;
                }
                questionList.add(itemPos++,message);
                questionAdapter.notifyDataSetChanged();
                questionAdapter.notifyItemInserted(0);

                mSwipeRefreshLayout.setRefreshing(false);

                mLinearlayout.scrollToPositionWithOffset(10, 0);
                mQuestionsList.scrollToPosition(questionList.size()-1);

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

        questionList.clear();
        LoadMessage();
    }


   /* @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        initSpinner();
    }

    private void initSpinner() {

        // RESET FIRST SPINNER
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item, topicList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // RESET SECOND SPINNER
       /* final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item, optionList) {

            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

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
        spinner2.setAdapter(spinnerArrayAdapter2);*/

        spinnerArrayAdapter.setDropDownViewResource(spinner_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    private void LoadMessage() {

        isLoading = true;

        Query quizQuery = mDatabase.limitToLast(TOTAL_ITEMS_TO_LOAD);

        quizQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // load initial data set

                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    Question message = child.getValue(Question.class);

                    itemPos++;
                    if (itemPos == 1) {

                        String messageKey = child.getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;

                    }

                    if (itemPos == 0) {
                        String messageKey = child.getKey();
                        mFirstKey = messageKey;
                    }

                    questionList.add(message);
                    questionAdapter.notifyDataSetChanged();
                    questionAdapter.notifyItemInserted(0);

                    mSwipeRefreshLayout.setRefreshing(false);

                    mQuestionsList.scrollToPosition(questionList.size()-1);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private void LoadMoreMessage() {

        Query quizQuery = mDatabase.orderByKey().endAt(mLastKey).limitToLast(10);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //questionList.clear();
                Question message = dataSnapshot.getValue(Question.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    questionList.add(itemPos++,message);

                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;

                }
                questionAdapter.notifyDataSetChanged();
                questionAdapter.notifyItemInserted(0);

                mSwipeRefreshLayout.setRefreshing(false);

                mLinearlayout.scrollToPositionWithOffset(10, 0);
                mQuestionsList.scrollToPosition(10);

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


    private void LoadSearchMessage() {

        Query filterQuery = mDatabase.orderByChild("views").limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        filterQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Question message = dataSnapshot.getValue(Question.class);
                if (dataSnapshot.getChildrenCount() == 0) {

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

    public void setPullToLoadView(PullToLoadView pullToLoadView) {
        this.pullToLoadView = pullToLoadView;
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
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);

        super.onCreateOptionsMenu(menu, inflater);
    }


}