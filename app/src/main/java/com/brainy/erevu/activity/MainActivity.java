package com.brainy.erevu.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.brainy.erevu.Services.GPSTracker;
import com.brainy.erevu.data.CustomTypefaceSpan;
import com.brainy.erevu.tabs.tab1Questions;
import com.brainy.erevu.tabs.tabGroups;
import com.brainy.erevu.tabs.tabMessages;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.brainy.erevu.R.layout.spinner_item;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    String  personName = "";
    String  personEmail = "";
    Bitmap  LargIcon;
    String  personId = "";
    Uri personPhoto = null;
    Long users_points = null;
    String city = null;
    String state = null;
    String country = null;
    String stringDate2;
    String questionTitlTag;
    String questionBodyTag;

    String name = null;
    String image = null;
    String question_key = null;
    String sender_uid = null;
    String message = null;
    Boolean read_status = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    String selectedTopic = null;
    private ViewPager mViewPager;
    private FloatingActionButton fab, fabGroup, fabMessage;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseUsersQuestions, mDatabaseInboxUsers, mDatabaseUsersAns, mDatabase, mDatabaseForumNotifications, mDatabaseNotifications, mDatabaseMessages;
    Context mContext;
    private FirebaseAuth auth;
    private RelativeLayout noty_icon, messages;
    private TextView noty_count, message_count;
    private ImageView search_icon;

    private static final int REQUEST_CODE_PERMISSION = 1;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    Context context;
    View view;

    GPSTracker gps;
    Geocoder geocoder;
    List<Address> addresses;
    Spinner spinner2;
    View spinnerDivider;
    String selectedSubTopic;
    List<String> subTopicList;
    String[] sub_topic;

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;

    // Get reference of widgets from XML layout

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

        auth = FirebaseAuth.getInstance();

        //CHECK IF USER IS ADMIN
        if (auth.getCurrentUser() != null) {
            String admin_email = auth.getCurrentUser().getEmail();
            if (admin_email.equals("erevuapp@gmail.com")) {
                //OPEN ADMIN
                Intent cardonClick = new Intent(MainActivity.this, AdminpassActivity.class);
                cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cardonClick);
            }
        }

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference().child("Users_messages");

        checkIfAccIsCompt();

        mDatabaseForumNotifications = FirebaseDatabase.getInstance().getReference().child("Forum_notifications");

        setContentView(R.layout.activity_main);

        Window window = MainActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( MainActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mContext=this;

        // Set up the ViewPager with the sections adapter.

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseInboxUsers = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Users_notifications");
        mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");
        mDatabaseQuestions.keepSynced(true);
        mDatabaseInboxUsers.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseMessages.keepSynced(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser() != null) {
                    sendQuestion();
                    //startActivity(new Intent(new Intent(MainActivity.this, PostAnswerActivity.class)));
                } else {
                    Snackbar snackbar = Snackbar

                            .make(view, "You need to sign in first to be able to post a question!", Snackbar.LENGTH_LONG)
                            .setAction("SIGN IN", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //showSignInDialog();
                                }
                            });
                    snackbar.show();
                }
            }
        });

        fabMessage = (FloatingActionButton) findViewById(R.id.fabMessage);
        fabMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent openRead = new Intent(MainActivity.this, GroupActivity.class);
                startActivity(openRead);*/
                startActivity(new Intent(MainActivity.this, UserSearchActivity.class));
            }
        });

        fabGroup = (FloatingActionButton) findViewById(R.id.fabGroup);
        fabGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent openRead = new Intent(MainActivity.this, GroupActivity.class);
                startActivity(openRead);*/
                startActivity(new Intent(MainActivity.this, AddGroupActivity.class));
            }
        });

        noty_count = (TextView) findViewById(R.id.noty_count);
        message_count = (TextView) findViewById(R.id.message_count);
        noty_icon = (RelativeLayout) findViewById(R.id.noty_icon);
        noty_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(openRead);
            }
        });

        messages = (RelativeLayout) findViewById(R.id.post_message);
        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(MainActivity.this, InboxActivity.class);
                startActivity(openRead);
            }
        });

        search_icon = (ImageView) findViewById(R.id.search_icon);
        search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(openRead);
            }
        });

        //CHECK FOR NOTIFICATIONS
        if (auth.getCurrentUser() != null) {
            final Query quizQuery = mDatabaseNotifications.child(auth.getCurrentUser().getUid()).orderByChild("read").equalTo(false);
            mDatabaseNotifications.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        noty_count.setVisibility(View.GONE);
                    } else {

                        //COUNT THE NUMBER OF NOTIFICATIONS
                        // count number of answers
                        quizQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    noty_count.setVisibility(View.GONE);
                                } else {
                                    noty_count.setText(dataSnapshot.getChildrenCount() + "");
                                    noty_count.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //CHECK FOR MESSAGES
        if (auth.getCurrentUser() != null) {
            final Query quizQuery = mDatabaseMessages.child(auth.getCurrentUser().getUid()).orderByChild("read").equalTo(false);
            mDatabaseInboxUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        message_count.setVisibility(View.GONE);
                    } else {

                        //COUNT THE NUMBER OF NOTIFICATIONS
                        // count number of answers
                        quizQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    message_count.setVisibility(View.GONE);
                                } else {
                                    message_count.setText(dataSnapshot.getChildrenCount() + "");
                                    message_count.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        initPageChanger();
        checkUserLoggedIn();

      if (auth.getCurrentUser() != null) {
          mDatabaseUsersQuestions = FirebaseDatabase.getInstance().getReference().child("Users_questions").child(auth.getCurrentUser().getUid());

      }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu m = navigationView.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);
            applyFontToMenuItem(mi);

            //for applying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
        }

        if (auth.getCurrentUser() != null) {
            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String post_image = (String) dataSnapshot.child("user_image").getValue();
                    String post_name = (String) dataSnapshot.child("name").getValue();

                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    View hView = navigationView.getHeaderView(0);

                    final ImageView nav_user = (ImageView) hView.findViewById(R.id.nav_photo);
                    TextView nav_username = (TextView) hView.findViewById(R.id.nav_username);
                    TextView nav_email = (TextView) hView.findViewById(R.id.nav_email);

                    nav_username.setText(post_name);
                    nav_email.setText(auth.getCurrentUser().getEmail());

                    Glide.with(getApplicationContext())
                            .load(post_image).asBitmap()
                            .placeholder(R.drawable.placeholder_image)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .centerCrop()
                            .into(new BitmapImageViewTarget(nav_user) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                                    circularBitmapDrawable.setCircular(true);
                                    nav_user.setImageDrawable(circularBitmapDrawable);
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfAccIsCompt();
    }


    private void checkIfAccIsCompt() {
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (auth.getCurrentUser() != null) {
                    if (!dataSnapshot.hasChild(auth.getCurrentUser().getUid()) || !dataSnapshot.child(auth.getCurrentUser().getUid()).hasChild("username")) {

                        Intent cardonClick = new Intent(MainActivity.this, CompleteRegActivity.class);
                        cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(cardonClick);
                    } else {
                        checkForNotifications();
                        awardReputation();
                        getUserLocation();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void applyFontToMenuItem(MenuItem item) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");
        SpannableString mNewTitle = new SpannableString(item.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        item.setTitle(mNewTitle);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        applyFontToMenuItem(item);
        if (id == R.id.nav_profile) {
            if (auth.getCurrentUser() != null) {
                startActivity(new Intent(new Intent(MainActivity.this, EditProfileActivity.class)));
            } else {

                Snackbar snackbar = Snackbar
                        .make(getWindow().getDecorView().getRootView(), "You need to be sign in!", Snackbar.LENGTH_LONG)
                        .setAction("SIGN IN", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent openRead = new Intent(MainActivity.this, SigninActivity.class);
                                startActivity(openRead);
                                //showSignInDialog();
                            }
                        });

                snackbar.show();
            }
        } else if (id == R.id.nav_settings) {

            if (auth.getCurrentUser() != null) {
                Intent openRead = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(openRead);
            } else {

                Snackbar snackbar = Snackbar
                        .make(getWindow().getDecorView().getRootView(), "You need to be sign in!", Snackbar.LENGTH_LONG)
                        .setAction("SIGN IN", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent openRead = new Intent(MainActivity.this, SigninActivity.class);
                                startActivity(openRead);
                                //showSignInDialog();
                            }
                        });

                snackbar.show();
            }



        } else if (id == R.id.nav_favourites) {

            Intent cardonClick = new Intent(MainActivity.this, FavouriteActivity.class);
            cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(cardonClick);

        } else if (id == R.id.nav_slideshow) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.erevu.erevu&hl=en"));
            startActivity(browserIntent);

        } else if (id == R.id.nav_manage) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.erevu.erevu&hl=en"));
            startActivity(browserIntent);

        } else if (id == R.id.nav_share) {

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String shareBody ="Erevu Android App";
            String shareSub = "Hey, Download Erevu app today on playstore, it will help you out with your homework & class assignments  "+"https://play.google.com/store/apps/details?id=com.erevu.erevu ";
            myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
            myIntent.putExtra(Intent.EXTRA_TEXT,shareSub);
            startActivity(Intent.createChooser(myIntent,"Share this app"));

        } else if (id == R.id.nav_send) {

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String shareBody ="Erevu Android App";
            String shareSub = "Hey, Download Erevu app today on playstore, it will help you out with your homework & class assignments  "+"https://play.google.com/store/apps/details?id=com.erevu.erevu ";
            myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
            myIntent.putExtra(Intent.EXTRA_TEXT,shareSub);
            startActivity(Intent.createChooser(myIntent,"Invite a friend"));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showSignInDialog() {

        final Context context = MainActivity.this;

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.sign_in_dialog);
        dialog.setTitle("Let's get started...");
        dialog.show();

        Button signBtn = (Button) dialog.findViewById(R.id.sign_in);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openRead = new Intent(MainActivity.this, SigninActivity.class);
                startActivity(openRead);
                dialog.dismiss();
            }
        });

        RelativeLayout googleBtn = (RelativeLayout) dialog.findViewById(R.id.googleBtn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initGoogleSignIn();
                dialog.dismiss();
            }
        });


    }

    private void initGoogleSignIn() {

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .enableAutoManage(MainActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(MainActivity.this, "Failed to connect to Google, check your internet connection.",
                                Toast.LENGTH_LONG).show();
                    }
                })

                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Configure Google Sign In
        progressBar.setVisibility(View.VISIBLE);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        progressBar.setVisibility(View.GONE);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressBar.setVisibility(View.VISIBLE);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                personName = account.getDisplayName();
                personEmail = account.getEmail();
                personId = account.getId();
                personPhoto = account.getPhotoUrl();

                Toast.makeText(MainActivity.this, "Sign in success!.",
                        Toast.LENGTH_LONG).show();

                this.recreate();

            } else {
                // Google Sign In failed, update UI appropriately
                // ...

                progressBar.setVisibility(View.GONE);
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            postUserInfoToDB();
                            Toast.makeText(MainActivity.this, "Sign in success!.",
                                    Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }

    private void postUserInfoToDB() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        final DatabaseReference newPost = mDatabaseUsers;

        newPost.child(auth.getCurrentUser().getUid()).child("name").setValue(personName);
        newPost.child(auth.getCurrentUser().getUid()).child("status").setValue("");
        newPost.child(auth.getCurrentUser().getUid()).child("user_image").setValue(personPhoto.toString());
        newPost.child(auth.getCurrentUser().getUid()).child("joined_date").setValue(stringDate);
        newPost.child(auth.getCurrentUser().getUid()).child("personId").setValue(personId);
        newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
        newPost.child(auth.getCurrentUser().getUid()).child("user_gmail").setValue(personEmail);
        newPost.child(auth.getCurrentUser().getUid()).child("sign_in_type").setValue("google_signIn");
        newPost.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
        newPost.child(auth.getCurrentUser().getUid()).child("points_earned").setValue(10);
        newPost.child(auth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

    }

    private void awardReputation() {

        if (auth.getCurrentUser() != null) {

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    users_points = (Long) dataSnapshot.child("points_earned").getValue();
                    String users_pints = (String) dataSnapshot.child("bio").getValue();
//
//                    String user_reputation = dataSnapshot.child("reputation").getValue().toString();


                    if (users_points < 100) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
                        // PREVELAGE

                    } else if (users_points > 100 && users_points < 999) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Smart");

                    } else if (users_points > 1000 && users_points < 2999) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Intelligent");

                    }  else if (users_points > 3000 && users_points < 4999) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Erevu");

                    } else if (users_points > 10000 ) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Super Erevu");

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    private void checkForNotifications() {

        // inbox notifications
        mDatabaseInboxUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(auth.getCurrentUser().getUid())) {
                    mDatabaseInboxUsers.child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            //CHECK INBOX TYPE
                            String inbox_type = (String) dataSnapshot.child("inbox_type").getValue();

                            name = (String) dataSnapshot.child("sender_name").getValue();
                            image = (String) dataSnapshot.child("sender_image").getValue();
                            question_key = (String) dataSnapshot.child("question_key").getValue();
                            sender_uid = (String) dataSnapshot.child("sender_uid").getValue();
                            message = (String) dataSnapshot.child("posted_answer").getValue();
                            read_status = (Boolean) dataSnapshot.child("read").getValue();
                            String post_id = (String) dataSnapshot.child("post_id").getValue();


                                if (read_status.equals(false)) {
                                    // send notification to reciever

                                    Context context = getApplicationContext();
                                    Intent intent = new Intent(context, ReadQuestionActivity.class);
                                    intent.putExtra("question_id", question_key);
                                    PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                                    Notification noty = new Notification.Builder(MainActivity.this)
                                            .setContentTitle("Erevu")
                                            .setTicker("Inbox alert!")
                                            .setContentText("Hi, your post was voted up - " + message)
                                            .setSmallIcon(R.drawable.erevu_noty_icon)
                                            // .setLargeIcon(bitmap)
                                            .setContentIntent(pIntent).getNotification();


                                    noty.flags = Notification.FLAG_AUTO_CANCEL;
                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                    r.play();
                                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    nm.notify(0, noty);

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
                } else {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       /* //FORUM NOTIFICATION
        mDatabaseForumNotifications.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(auth.getCurrentUser().getUid())) {
                    mDatabaseForumNotifications.child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            String name = (String) dataSnapshot.child("sender_name").getValue();
                            String question_key = (String) dataSnapshot.child("question_key").getValue();
                            String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                Context context = getApplicationContext();
                                Intent intent = new Intent(context, ChatroomActivity.class);
                                //delete notification
                                mDatabaseForumNotifications.child(auth.getCurrentUser().getUid()).removeValue();
                                intent.putExtra("question_id", question_key);
                                PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                                Notification noty = new Notification.Builder(MainActivity.this)
                                        .setContentTitle("Erevu")
                                        .setTicker("Forum alert!")
                                        .setContentText(name + " posted on a forum you've participated!")
                                        .setSmallIcon(R.drawable.erevu_noty_icon)
                                        // .setLargeIcon(bitmap)
                                        .setContentIntent(pIntent).getNotification();


                                noty.flags = Notification.FLAG_AUTO_CANCEL;
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();
                                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                nm.notify(0, noty);



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
                } else {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


    }

    private void getUserLocation() {

        if(Build.VERSION.SDK_INT>= 23) {

            if (checkSelfPermission(mPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{mPermission,
                        },
                        REQUEST_CODE_PERMISSION);
                return;
            }

            else
            {
                geocoder = new Geocoder(this, Locale.getDefault());

                // create class object
                gps = new GPSTracker(MainActivity.this);
                // check if GPS enabled
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    // \n is for new line
                    // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("latitude").setValue(latitude);
                    mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("longitude").setValue(longitude);

                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);

                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        city = addresses.get(0).getLocality();
                        state = addresses.get(0).getAdminArea();
                        country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("address").setValue(address);
                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("city").setValue(city);
                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("state").setValue(state);
                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("country").setValue(country);
                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("postalCode").setValue(postalCode);
                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("knownName").setValue(knownName);

                    } catch (IOException e) {
                        e.printStackTrace();


                    }

                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    /*gps.showSettingsAlert();*/
                }
            }
        }

    }

    private void checkUserLoggedIn() {

    }

    private void sendQuestion() {

        final Context context = MainActivity.this;

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.question_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        final EditText questionTitleInput = (EditText) dialog.findViewById(R.id.questionTitleInput);
        final EditText questionBodyInput = (EditText) dialog.findViewById(R.id.questionBodyInput);

        final Button cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ImageView takeShot = (ImageView) dialog.findViewById(R.id.takeCamera);
        takeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // CHECK IF USER HAS ENOUGH POINTS
                if (users_points > 9 || users_points == 100) {
                    dialog.dismiss();
                    Intent openRead = new Intent(MainActivity.this, TakeCameraShotActivity.class);
                    startActivity(openRead);
                } else {
                    Toast.makeText(MainActivity.this, "Sorry! you don't have enough reputation to post a photo on Erevu.", Toast.LENGTH_LONG).show();
                }
            }
        });

        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        spinner2 = (Spinner) dialog.findViewById(R.id.spinner2);
        spinnerDivider = (View) dialog.findViewById(R.id.spinnerDivider);
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                MainActivity.this, R.layout.spinner_dialog_item,topicList){
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


        Button create = (Button) dialog.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();
            }

            private void startPosting() {

                Date date = new Date();
                final String stringDate = DateFormat.getDateTimeInstance().format(date);
                stringDate2 = DateFormat.getDateInstance().format(date);

                questionTitlTag = questionTitleInput.getText().toString().trim();
                questionBodyTag = questionBodyInput.getText().toString().trim();
                if (TextUtils.isEmpty(questionTitlTag)) {

                    Toast.makeText(MainActivity.this, "Question title CANNOT be empty!", Toast.LENGTH_LONG).show();

                } else if (selectedTopic == null) {

                    Toast.makeText(MainActivity.this, "Please TAG your question!", Toast.LENGTH_LONG).show();
                } else if (selectedSubTopic == null) {

                    Toast.makeText(MainActivity.this, "Please SUB-TAG your question!", Toast.LENGTH_LONG).show();

                } else {
                    dialog.dismiss();
                    completePosting();
                }

            }
        });
    }

    private void resetSpinner2() {

        //DISABLE THE FIRST SPINNER
        // Initializing an ArrayAdapter2
        initSpinner2();
    }

    private void completePosting() {

        final DatabaseReference newPost = mDatabaseQuestions.push();
        final DatabaseReference newPost2 = mDatabaseUsersQuestions.push();

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> map = new HashMap<>();
                map.put("question_title", questionTitlTag);
                map.put("question_body", questionBodyTag);
                map.put("sender_uid", auth.getCurrentUser().getUid());
                map.put("sender_name", dataSnapshot.child("name").getValue());
                map.put("Unanswered", true);
                map.put("sender_image", dataSnapshot.child("user_image").getValue());
                map.put("posted_date", System.currentTimeMillis());
                map.put("state", state);
                map.put("city", city);
                map.put("country", country);
                map.put("tag", selectedTopic);
                map.put("sub_tag", selectedSubTopic);
                map.put("post_id", newPost.getKey());
                newPost.setValue(map);

                Map<String, Object> map2 = new HashMap<>();
                map2.put("question_title", questionTitlTag);
                map2.put("question_body", questionBodyTag);
                map2.put("sender_uid", auth.getCurrentUser().getUid());
                map2.put("sender_name", dataSnapshot.child("name").getValue());
                map2.put("state", state);
                map2.put("city", city);
                map2.put("country", country);
                map2.put("Unanswered", true);
                map2.put("sender_image", dataSnapshot.child("user_image").getValue());
                map2.put("posted_date", System.currentTimeMillis());
                map2.put("tag", selectedTopic);
                map2.put("sub_tag", selectedSubTopic);
                map2.put("post_id", newPost.getKey());
                newPost2.setValue(map2);

                if (auth.getCurrentUser() != null) {
                    mDatabase.child(newPost.getKey()).child("views").child(auth.getCurrentUser().getUid()).setValue("iView");
                }
                Intent openRead = new Intent(MainActivity.this, ReadQuestionActivity.class);
                openRead.putExtra("question_id", newPost.getKey() );
                startActivity(openRead);

                Toast.makeText(MainActivity.this, "Your question is posted successfully!",Toast.LENGTH_LONG).show();
               /* final Context context = MainActivity.this;

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.success_dialog);
                dialog.setCancelable(false);
                            *//*dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*//*
                dialog.show();

                Button create = (Button) dialog.findViewById(R.id.create);
                create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        dialog.dismiss();
                    }

                });
*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



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

    private void initPageChanger() {

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                switch (position) {
                    case 0:
                        fab.show();
                        fabGroup.hide();
                        fabMessage.hide();
                        break;
                    case 1:
                        fabGroup.hide();
                        fabMessage.show();
                        fab.hide();
                        break;

                    default:
                        fabGroup.show();
                        fabMessage.hide();
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

     public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            //returning the current tabs
            switch (position) {
                case 0:
                    tab1Questions tab1 = new  tab1Questions();
                    return tab1;
                case 1:
                    tabMessages tab2 = new tabMessages();
                    return tab2;
                case 2:
                    tabGroups tab3 = new tabGroups();
                    return tab3;
              /*  case 3:
                    tab4More tab4 = new tab4More();
                    return tab4;*/
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 3;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "QUESTIONS";
                case 1:
                    return "MESSAGES";
                case 2:
                    return "GROUPS";
               /* case 3:
                    return "MORE";*/
            }
            return null;
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
                if (id == R.id.action_logout) {

                   auth.signOut();
                   this.recreate();
                    Toast.makeText(MainActivity.this, "You have successfully Logged out!",Toast.LENGTH_LONG).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

}
