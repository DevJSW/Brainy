package com.brainy.brainy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.brainy.brainy.tabs.AboutProfileTab;
import com.brainy.brainy.tabs.AnsProfileTab;
import com.brainy.brainy.tabs.QuizProfileTab;
import com.brainy.brainy.tabs.tab1Questions;
import com.brainy.brainy.tabs.tab2Inbox;
import com.brainy.brainy.tabs.tab3Achievements;
import com.brainy.brainy.tabs.tab4More;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DatabaseReference mDatabaseUsers, mDatabaseUsers2, mDatabase, mDatabaseLastSeen;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView mGroupIcon;
    private EditText searchInput;
    private TextView username;
    private ProgressDialog mProgress;
    private RecyclerView mAlbumList;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Window window = EditProfileActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( EditProfileActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();
        mGroupIcon = (ImageView) findViewById(R.id.user_avator);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers2 = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mDatabaseUsers2.keepSynced(true);

        username = (TextView) findViewById(R.id.username);

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               /* if (dataSnapshot.hasChild("status") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {*/

                String user_location = dataSnapshot.child("location").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();


                username.setText(name);

               /* Glide.with(EditProfileActivity.this)
                        .load(image)
                        .priority(Priority.IMMEDIATE)
                        .into(mGroupIcon);*/

               Glide.with(EditProfileActivity.this)
                       .load(image)
                       .into(mGroupIcon);



              /*  } else {}*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mAuth.getCurrentUser() != null) {

            awardBadge();
        }

    }

    private void awardBadge() {

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long users_points = (Long) dataSnapshot.child("points_earned").getValue();
                String users_pints = (String) dataSnapshot.child("bio").getValue();
                String user_reputation = dataSnapshot.child("reputation").getValue().toString();

                ImageView badge = (ImageView) findViewById(R.id.brainy_badge);

                if (users_points < 100) {

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
                    badge.setVisibility(View.GONE);
                    // PREVELAGE

                } else if (users_points > 100 && users_points < 499) {

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("reputation").setValue("Smart");
                    badge.setImageResource(R.drawable.smart_badge);

                } else if (users_points > 500 && users_points < 999) {

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("reputation").setValue("Intelligent");
                    badge.setImageResource(R.drawable.intelligent_badge);

                }  else if (users_points > 1000 && users_points < 1999) {

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("reputation").setValue("Brainy");
                    badge.setImageResource(R.drawable.brainy_badge);

                } else if (users_points > 2000 ) {

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("reputation").setValue("Super Brainy");
                    badge.setImageResource(R.drawable.super_brainy_badge);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ic_edit) {

            Intent openRead = new Intent(EditProfileActivity.this, ProfileEditActivity.class);
           /* openRead.putExtra("question_id", QuizKey );*/
            startActivity(openRead);

            return true;
        }
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

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
            View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
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
                    AboutProfileTab tab1 = new  AboutProfileTab();
                    return tab1;
                case 1:
                    AnsProfileTab tab2 = new AnsProfileTab ();
                    return tab2;
                case 2:
                    QuizProfileTab tab3 = new QuizProfileTab();
                    return tab3;

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "About";
                case 1:
                    return "Answers";
                case 2:
                    return "Question";

            }
            return null;
        }
    }
}
