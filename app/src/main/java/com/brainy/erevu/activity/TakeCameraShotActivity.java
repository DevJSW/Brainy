package com.brainy.erevu.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brainy.erevu.R.layout.spinner_item;


public class TakeCameraShotActivity extends AppCompatActivity {

    private ImageView mAddPhoto;
    private EditText questionInput;
    private RecyclerView mCommentList;
    private DatabaseReference mDatabaseQuestions;
    private DatabaseReference mDatabaseUsersQuestions;
    private DatabaseReference mDatabaseComment;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabasePostComments;
    private Query mQueryPostComment;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    Spinner spinner, spinner2;

    String selectedTopic = null;
    View spinnerDivider;
    String selectedSubTopic;
    List<String> subTopicList;
    String[] sub_topic;

    Uri imageUri;
    Uri selectedImageUri = null;
    final int TAKE_PICTURE = 115;

    // Initializing a String Array
    String[] topics = new String[]{
            "Tag your question...",
            "Select a topic...",
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
            "Others"
    };

    final List<String> topicList = new ArrayList<>(Arrays.asList(topics));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_camera_shot);
        Window window = TakeCameraShotActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( TakeCameraShotActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        questionInput = (EditText) findViewById(R.id.questionTitleInput);

        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mProgress = new ProgressDialog(this);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        spinnerDivider = (View) findViewById(R.id.spinnerDivider);
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                TakeCameraShotActivity.this, R.layout.spinner_dialog_item,topicList){
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

                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    selectedTopic = (String) parent.getItemAtPosition(position);
                    resetSpinner2();
                    checkIfTopicSelected();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        mAuth = FirebaseAuth.getInstance();

        mAddPhoto = (ImageView) findViewById(R.id.addPhoto);
        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsersQuestions = FirebaseDatabase.getInstance().getReference().child("Users_questions").child(mAuth.getCurrentUser().getUid());
        mDatabaseQuestions.keepSynced(true);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyData();

            }

        });

        takePhoto();
    }

    private void verifyData() {
        final String questionTitlTag = questionInput.getText().toString().trim();
        if (TextUtils.isEmpty(questionTitlTag)) {

            Toast.makeText(TakeCameraShotActivity.this, "Question title CANNOT be empty!", Toast.LENGTH_LONG).show();

        } else if (selectedTopic == null) {

            Toast.makeText(TakeCameraShotActivity.this, "Please TAG your question!", Toast.LENGTH_LONG).show();
        } else if (selectedSubTopic == null) {

            Toast.makeText(TakeCameraShotActivity.this, "Please SUB-TAG your question!", Toast.LENGTH_LONG).show();

        } else {

            startPosting();
        }
    }

    private void startPosting() {

        mProgress.setMessage("Posting question, please wait...");
        mProgress.setCancelable(false);

        final String questionTitlTag = questionInput.getText().toString().trim();

        final String user_id = mAuth.getCurrentUser().getUid();
        final String uid = user_id.substring(0, Math.min(user_id.length(), 4));

        if (imageUri != null) {

            mProgress.show();

            StorageReference filepath = mStorage.child("question_images").child(imageUri.getLastPathSegment());


            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPost = mDatabaseQuestions.push();
                    final DatabaseReference newPost2 = mDatabaseUsersQuestions.push();

                    mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("question_title", questionTitlTag);
                            map.put("question_photo", downloadUrl.toString());
                            map.put("sender_uid", mAuth.getCurrentUser().getUid());
                            map.put("sender_name", dataSnapshot.child("name").getValue());
                            map.put("Unanswered", true);
                            map.put("sender_image", dataSnapshot.child("user_image").getValue());
                            map.put("posted_date", System.currentTimeMillis());
                            map.put("tag", selectedTopic);
                            map.put("sub_tag", selectedSubTopic);
                            map.put("post_id", newPost.getKey());
                            newPost.setValue(map);

                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("question_title", questionTitlTag);
                            map2.put("question_photo", downloadUrl.toString());
                            map2.put("sender_uid", mAuth.getCurrentUser().getUid());
                            map2.put("sender_name", dataSnapshot.child("name").getValue());
                            map2.put("Unanswered", true);
                            map2.put("sender_image", dataSnapshot.child("user_image").getValue());
                            map2.put("posted_date", System.currentTimeMillis());
                            map2.put("tag", selectedTopic);
                            map2.put("sub_tag", selectedSubTopic);
                            map2.put("post_id", newPost.getKey());
                            newPost2.setValue(map2);

                            if (mAuth.getCurrentUser() != null) {
                                mDatabaseQuestions.child(newPost.getKey()).child("views").child(mAuth.getCurrentUser().getUid()).setValue("iView");
                            }
                            Intent openRead = new Intent(TakeCameraShotActivity.this, ReadQuestionActivity.class);
                            openRead.putExtra("question_id", newPost.getKey() );
                            startActivity(openRead);

                            Toast.makeText(TakeCameraShotActivity.this, "Your question is posted successfully!",Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    mProgress.dismiss();

                    Toast.makeText(TakeCameraShotActivity.this, "Question posted successfully!", Toast.LENGTH_LONG).show();
                    finish();

                }


            });

        }
    }

    private void takePhoto() {

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photoFile = new File(Environment.getExternalStorageDirectory(),  "Photo.png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photoFile));
        imageUri = Uri.fromFile(photoFile);
        startActivityForResult(intent, TAKE_PICTURE);


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = imageUri;
                    mAddPhoto.setImageURI(selectedImageUri);

                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }

    private void resetSpinner2() {

        //DISABLE THE FIRST SPINNER
        // Initializing an ArrayAdapter2
        initSpinner2();
    }


    private void checkIfTopicSelected() {

        if (selectedTopic != null) {

            initSpinner2();
            spinner2.setVisibility(View.VISIBLE);
            spinnerDivider.setVisibility(View.VISIBLE);
        } else {
            spinner2.setVisibility(View.GONE);
            spinnerDivider.setVisibility(View.GONE);
        }
    }

    private void initSpinner2() {


        if ("Law".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
                    "Accidents & Injuries",
                    "Bankruptcy & Debt",
                    "Car & Motor accidents",
                    "Criminal law",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if ("Math".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Agriculture".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Medical & Health Science".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Physics & Electronics".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Geography & Geology".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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
                    "Quaternary science",
                    "Landscape ecology",
                    "OTHERS"
            };

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Computer science & ICT".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Business & Economics".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
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

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        if ("Chemistry & Chemical science".equals(selectedTopic)) {
            sub_topic = new String[]{
                    " Sub-tag",
                    "Measurement",
                    "Matter",
                    "The Periodic Table",
                    "Bonding Basics",
                    "Ionic Bonds",
                    "Covalent Bonds",
                    "Intermolecular Bonding",
                    "A Closer Look at The Atom",
                    "Bohr Model of the Atom",
                    "Quantum Mechanical Model of the Atom",
                    "Electron Configuration",
                    "Molecular Orbital Theory",
                    "Chemical Reactions",
                    "The Mole Concept",
                    "Stoichiometry",
                    "Phases of Matter",
                    "Gases",
                    "Solutions",
                    "Chemical Kinetics",
                    "Chemical Equilibrium",
                    "Acids and Bases",
                    "Reactions in Solution",
                    "Electrochemistry",
                    "Thermochemistry",
                    "Nuclear Chemistry",
                    "Organic Chemistry",
                    "OTHERS"
            };

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        if ("History & Government".equals(selectedTopic)) {
            sub_topic = new String[]{

            };

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }

        if ("OTHERS".equals(selectedTopic)) {
            sub_topic = new String[]{

            };

            subTopicList = new ArrayList<>(Arrays.asList(sub_topic));
        }


        // Initializing an ArrayAdapter2
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                this, R.layout.spinner_item, subTopicList) {

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
        spinner2.setAdapter(spinnerArrayAdapter2);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    selectedSubTopic = (String) parent.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

}
