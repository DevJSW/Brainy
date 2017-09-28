package com.brainy.brainy.activity;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.brainy.R;
import com.brainy.brainy.Services.GPSTracker;
import com.brainy.brainy.tabs.tab1Questions;
import com.brainy.brainy.tabs.tab2Inbox;
import com.brainy.brainy.tabs.tab3Achievements;
import com.brainy.brainy.tabs.tab4More;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.brainy.brainy.R.layout.spinner_item;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    String selectedTopic = null;
    private ViewPager mViewPager;
    private FloatingActionButton fab;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions, mDatabaseInboxUsers;
    Context mContext;
    private FirebaseAuth auth;

    GPSTracker gps;
    Geocoder geocoder;
    List<Address> addresses;

    // Get reference of widgets from XML layout

    // Initializing a String Array
    String[] topics = new String[]{
            "Tag your question...",
            "Math",
            "Computer science",
            "Economics",
            "Languages",
            "Law",
            "Physics",
            "Chemistry",
            "Aviation",
            "Health Science"
    };


    final List<String> topicList = new ArrayList<>(Arrays.asList(topics));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = MainActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( MainActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mContext=this;

        // Set up the ViewPager with the sections adapter.

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseInboxUsers = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseQuestions.keepSynced(true);
        mDatabaseInboxUsers.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser() != null) {
                    sendQuestion();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view, "You need to be signed in order for you to post a question!", Snackbar.LENGTH_LONG)
                            .setAction("SIGN IN", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Snackbar snackbar1 = Snackbar.make(view, "SIGNED IN!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();
                                }
                            });

                    snackbar.show();
                }
            }
        });
        
        initPageChanger();
        checkUserLoggedIn();
      /*  getUserLocation();*/
      if (auth.getCurrentUser() != null) {
          checkForNotifications();
          awardReputation();
      }
    }

    private void awardReputation() {



        if (auth.getCurrentUser() != null) {

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long users_points = (Long) dataSnapshot.child("points_earned").getValue();
                    String users_pints = (String) dataSnapshot.child("bio").getValue();
                    String user_reputation = dataSnapshot.child("reputation").getValue().toString();


                    if (users_points < 100) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
                        // PREVELAGE

                    } else if (users_points > 100 && users_points < 499) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Smart");

                    } else if (users_points > 500 && users_points < 999) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Intelligent");

                    }  else if (users_points > 1000 && users_points < 1999) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Brainy");

                    } else if (users_points > 2000 ) {

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Super Brainy");

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    private void checkForNotifications() {

        mDatabaseInboxUsers.child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String name = (String) dataSnapshot.child("sender_name").getValue();
                String image = (String) dataSnapshot.child("sender_image").getValue();
                String question_key = (String) dataSnapshot.child("question_key").getValue();
                String sender_uid = (String) dataSnapshot.child("sendert_uid").getValue();
                String message = (String) dataSnapshot.child("posted_answer").getValue();
                Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();
                String post_id = (String) dataSnapshot.child("post_id").getValue();

                if (read_status.equals(false)) {
                    // send notification to reciever

                    Context context = getApplicationContext();
                    Intent intent = new Intent(context, ReadQuestionActivity.class);
                    intent.putExtra("question_id", question_key);
                    PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                    Notification noty = new Notification.Builder(MainActivity.this)
                            .setContentTitle("Brainy")
                            .setTicker("Inbox alert!")
                            .setContentText(name + " answered a question you asked - " + message)
                            .setSmallIcon(R.drawable.ic_brainy_tech_noty)
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

    }

    private void getUserLocation() {
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
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("city").setValue(city);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("country").setValue(country);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("address").setValue(address);

                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("address").setValue(address);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("city").setValue(city);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("state").setValue(state);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("country").setValue(country);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("postalCode").setValue(postalCode);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("knownName").setValue(knownName);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("city").setValue(city);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("country").setValue(country);
                mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("address").setValue(address);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
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
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
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
                final String stringDate2 = DateFormat.getDateInstance().format(date);

                final String questionTitlTag = questionTitleInput.getText().toString().trim();
                final String questionBodyTag = questionBodyInput.getText().toString().trim();
                if (TextUtils.isEmpty(questionTitlTag) || TextUtils.isEmpty(questionBodyTag)) {

                } else if (selectedTopic == null) {

                } else {

                    dialog.dismiss();

                    final DatabaseReference newPost = mDatabaseQuestions.push();

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
                            map.put("posted_date", stringDate2);
                            map.put("tag", selectedTopic);
                            map.put("post_id", newPost.getKey());
                            newPost.setValue(map);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

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
                        //fabPerson.hide();
                        break;
                    case 1:
                        //fabPerson.show();
                        fab.hide();
                        break;

                    default:
                        //fabHash.hide();
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

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            // sign out
            auth.signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
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
                    tab2Inbox tab2 = new tab2Inbox();
                    return tab2;
                case 2:
                    tab3Achievements tab3 = new tab3Achievements();
                    return tab3;
                case 3:
                    tab4More tab4 = new tab4More();
                    return tab4;
            }
            return null;
        }


        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "QUESTIONS";
                case 1:
                    return "INBOX";
                case 2:
                    return "FAVOURITE";
                case 3:
                    return "MORE";
            }
            return null;
        }
    }


}
