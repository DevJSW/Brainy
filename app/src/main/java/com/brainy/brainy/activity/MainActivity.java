package com.brainy.brainy.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.brainy.brainy.Services.GPSTracker;
import com.brainy.brainy.tabs.tab1Questions;
import com.brainy.brainy.tabs.tab2Inbox;
import com.brainy.brainy.tabs.tab3Achievements;
import com.brainy.brainy.tabs.tab4More;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private FloatingActionButton fab;
    private DatabaseReference mDatabaseUsers, mDatabaseQuestions;
    Context mContext;
    private FirebaseAuth auth;

    GPSTracker gps;
    Geocoder geocoder;
    List<Address> addresses;

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
        mDatabaseQuestions.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendQuestion();
            }
        });


        
        initPageChanger();
        checkUserLoggedIn();
      /*  getUserLocation();*/

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
        Button cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button create = (Button) dialog.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();
                dialog.dismiss();
            }

            private void startPosting() {

                Date date = new Date();
                final String stringDate = DateFormat.getDateTimeInstance().format(date);
                final String stringDate2 = DateFormat.getDateInstance().format(date);

                final String questionTitlTag = questionTitleInput.getText().toString().trim();
                final String questionBodyTag = questionBodyInput.getText().toString().trim();
                if (TextUtils.isEmpty(questionTitlTag) || TextUtils.isEmpty(questionBodyTag)) {

                } else {

                    final DatabaseReference newPost = mDatabaseQuestions.push();

                    mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // getting user uid
                            newPost.child("question_title").setValue(questionTitlTag);
                            newPost.child("question_body").setValue(questionBodyTag);
                            newPost.child("sender_uid").setValue(auth.getCurrentUser().getUid());
                            newPost.child("sender_name").setValue(dataSnapshot.child("name").getValue());
                            newPost.child("sender_image").setValue(dataSnapshot.child("user_image").getValue());
                            newPost.child("posted_date").setValue(stringDate2);

                            //newPost2.child(auth.getCurrentUser().getUid()).child("uid").setValue(dataSnapshot.getValue());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
