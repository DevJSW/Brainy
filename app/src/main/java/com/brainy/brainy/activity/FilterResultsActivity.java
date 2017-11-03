package com.brainy.brainy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.brainy.brainy.Adapters.QuestionAdapter;
import com.brainy.brainy.R;
import com.brainy.brainy.data.Question;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.srx.widget.PullToLoadView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.brainy.brainy.R.layout.spinner_item;

public class FilterResultsActivity extends AppCompatActivity {

    String myCurrentChats = null;
    String selectedTopic = null;
    String selectedOption = null;
    private TextView mNoPostTxt, tvFilterTopic;
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
    private final List<Question> questionList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;


    private static final String TAG = "tab1Question";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;

    //PAGINATION
    private static int TOTAL_ITEMS_TO_LOAD = 20;
    private int currentPage = 1;
    private int previousTotal = 9;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mFirstKey = "";
    private String mPrevKey = "";
    String[] sub_topic;
    List<String> topicList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_results);

        Window window = FilterResultsActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( FilterResultsActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);

        // AUTH
        mAuth = FirebaseAuth.getInstance();

        selectedTopic = getIntent().getStringExtra("selected_topic");


        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Law")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Accidents & Injuries",
                    "Bankruptcy & Debt",
                    "Car & Motor accidents",
                    "Civil rights",
                    "Dangerous products",
                    "Divorce & family law",
                    "Employees' rights",
                    "Estate & probate",
                    "Immigration law",
                    "Intellectual property",
                    "Real estate",
                    "Small business",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////



        if (selectedTopic.equals("Math")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Algebra",
                    "Calculus & Analysis",
                    "Geometry & Topology",
                    "Combinatorics",
                    "Logic",
                    "Number theory",
                    "Dynamical systems & differential equations",
                    "Mathematical physics",
                    "Computation",
                    "Information theory & signal processing",
                    "Probability & statistics",
                    "Game theory",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Agriculture")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Soil science",
                    "Animal science & Animal production",
                    "Animal breeding & Genetics",
                    "Agricultural extension & Rural development",
                    "Agricultural Economics",
                    "Agric Metereology & Water management",
                    "Agric Business & Financial management",
                    "Plant science & Crop production",
                    "Horticulture",
                    "Home economics",
                    "Forestry & Environmental management",
                    "Forestry & Wood technology",
                    "Fisheries & Aquaculture",
                    "Farm management",
                    "Eco-tourism & wildlife management",
                    "Information theory & signal processing",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Medical & Health Science")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Atrial Fibrillation",
                    "Bipolar Disorder",
                    "Breast Cancer",
                    "Chronic Dry Eye Relief",
                    "Colorectal Cancer",
                    "Conquering Crohn’s Disease & Crohn's Disease",
                    "Diabetes Mine",
                    "Diagnosing Carcinoid Syndrome",
                    "Eating Disorders",
                    "Emergency Contraception",
                    "Exocrine Pancreatic Insufficiency",
                    "Eye Health",
                    "FDA",
                    "Fertility",
                    "First Aid",
                    "Fitness & Exercise",
                    "Food & Nutrition",
                    "Headache",
                    "Healthy Sex",
                    "Hereditary Angioedema",
                    "High Cholesterol",
                    "HIV Prevention",
                    "HIV/AIDS",
                    "Hypothyroidism",
                    "Insomnia",
                    "Irritable Bowel Syndrome",
                    "Kidney Health",
                    "Lice",
                    "Lung Cancer",
                    "Myeloma",
                    "Osteoporosis",
                    "Oral Cancer",
                    "Probiotics and Digestive Health",
                    "Psoriatic Arthritis",
                    "Pulmonary Arterial Hypertension",
                    "Rheumatoid Arthritis",
                    "Skin Cancer",
                    "Smoking Cessation",
                    "Ulcerative Colitis",
                    "Vaccinations",
                    "Vaginal Health",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Physics & Electronics")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Atomic, Nuclear and Particle Physics",
                    "Laws of nature",
                    "Units & dimensions",
                    "Metric system",
                    "Scientic notation",
                    "Trigonometry",
                    "Vectors",
                    "Motion - Mechanics/Kinematics/Dynamics",
                    "Work, Power, and Energy",
                    "Gravity",
                    "Angular Velocity",
                    "Centripetal acceleration",
                    "Center of mass",
                    "Torque and Angular Acceleration",
                    "Moment of Inertia",
                    "Hydrostatics: Fluids at Rest",
                    "Hydrodynamics: Fluids in Motion",
                    "Thermodynamics",
                    "Electricity and Magnetism",
                    "Oscillations and Waves",
                    "Hydrodynamics: Fluids in Motion",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Geography & Geology")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Physical geography ",
                    "Geomorphology",
                    "Hydrology",
                    "Glaciology",
                    "Oceanography",
                    "Biogeography",
                    "Climatology",
                    "Meteorology",
                    "Pedology",
                    "Palaeogeography",
                    "Coastal geography",
                    "Quaternary science ",
                    "Landscape ecology",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Computer science & ICT")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Machine learning",
                    "Principles and techniques",
                    "Visual recognition",
                    "Mining massive data sets",
                    "Cryptography",
                    "Social information & network analysis",
                    "Deep learning for natural language processing",
                    "Computer vision",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (selectedTopic.equals("Business & Economics")) {
            sub_topic = new String[]{
                    "Choose sub-topic...",
                    "Business Ethics",
                    "Types of organizations",
                    "Areas of management application",
                    "Business strategy",
                    "Business management education",
                    "Management Theory",
                    "Advertising Issues",
                    "Human Resource Issues",
                    "International Business",
                    "Consumer Behavior",
                    "Marketing",
                    "Technical Writing Samples",
                    "World economy ",
                    "Economic stimulus approaches",
                    "Econometrics",
                    "Currency manipulation",
                    "Interest rates",
                    "Consumerism",
                    "OTHERS"
            };

            topicList = new ArrayList<>(Arrays.asList(sub_topic));
        }



        if (!selectedTopic.equals("")) {
            // Initializing an ArrayAdapter
            final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    FilterResultsActivity.this, R.layout.spinner_item, topicList) {
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
        } else {

            //HIDE FILTER
            LinearLayout rl = (LinearLayout) findViewById(R.id.rl);
            rl.setVisibility(View.GONE);
        }
        // database channels
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);
        tvFilterTopic = (TextView) findViewById(R.id.filter_title);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        tvFilterTopic.setText(selectedTopic);

        pullToLoadView = (PullToLoadView) findViewById(R.id.pullToLoadView);
       /* mQuestionsList2 = pullToLoadView.getRecyclerView();
        mQuestionsList2.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        /*pu*//*llToLoadView.isLoadMoreEnabled(true);*/
        // questionAdapter = new QuestionAdapter(getActivity(), new ArrayList<Question>());

        questionAdapter = new QuestionAdapter(this, questionList);

        mQuestionsList = (RecyclerView) findViewById(R.id.Questions_list);
        mLinearlayout = new LinearLayoutManager(this);
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mQuestionsList.setHasFixedSize(true);
        mQuestionsList.setLayoutManager(mLinearlayout);
        mQuestionsList.setAdapter(questionAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        itemPos++;
                        currentPage++;
                        questionList.clear();
                        LoadMessage();


                    }
                }, 3000);

            }
        });

     /*   mQuestionsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
*/

    }

    private void LoadLatestMessage() {

        Query quizQuery = mDatabase.orderByKey().startAt(mFirstKey).limitToLast(5);

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
    }


    @Override
    public void onResume() {
        super.onResume();
        questionList.clear();
        LoadMessage();
    }

    private void LoadMessage() {

        isLoading = true;

        Query filterQuery = mDatabase.orderByChild("tag").equalTo(selectedTopic).limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        filterQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

        Query quizQuery = mDatabase.orderByChild("tag").equalTo(selectedTopic)/*.orderByKey().endAt(mLastKey)*/.limitToLast(30);

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
                mQuestionsList.scrollToPosition(5);

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
