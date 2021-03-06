package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.tabs.ViewUserTabs.ViewAboutProfileTab;
import com.brainy.erevu.tabs.ViewUserTabs.ViewAnsProfileTab;
import com.brainy.erevu.tabs.ViewUserTabs.ViewQuizProfileTab;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewUserProfileActivity extends AppCompatActivity {


    String UserId = null;
    String username = null;
    String name = null;
    String image = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DatabaseReference mDatabaseUsers, mDatabaseUsers2, mDatabase, mDatabaseLastSeen, mDatabaseProfileViews;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView mGroupIcon, backBtn, sendMsg;
    private EditText searchInput;
    private TextView uname, mLocation, cuname;
    private ProgressDialog mProgress;
    private RecyclerView mAlbumList;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        Window window = ViewUserProfileActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ViewUserProfileActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        UserId = getIntent().getStringExtra("user_id");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();
        mGroupIcon = (ImageView) findViewById(R.id.user_avator);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseProfileViews = FirebaseDatabase.getInstance().getReference().child("Profile_views");
        mDatabaseUsers2 = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId);
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mDatabaseProfileViews.keepSynced(true);
        mDatabaseUsers2.keepSynced(true);

        uname = (TextView) findViewById(R.id.name);
        cuname = (TextView) findViewById(R.id.username);

        //ADD USER ID TO PROFILE VIEWS
        if (mAuth.getCurrentUser() != null) {
            mDatabaseProfileViews.child(UserId).child(mAuth.getCurrentUser().getUid()).setValue("iView");
        }

        mDatabaseUsers.child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               /* if (dataSnapshot.hasChild("status") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {*/

                name = dataSnapshot.child("name").getValue().toString();
                if (dataSnapshot.hasChild("username")) {
                username = dataSnapshot.child("username").getValue().toString();
                cuname.setText(username);
                cuname.setVisibility(View.VISIBLE);
                }
                image = dataSnapshot.child("user_image").getValue().toString();


                uname.setText(name);

                Glide.with(getApplicationContext())
                        .load(image).asBitmap()
                        .placeholder(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .centerCrop()
                        .into(new BitmapImageViewTarget(mGroupIcon) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                mGroupIcon.setImageDrawable(circularBitmapDrawable);
                            }
                        });

              /*  } else {}*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUserProfileActivity.this.finish();
            }
        });

        sendMsg = (ImageView) findViewById(R.id.sendMsg);
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openActivity = new Intent(ViewUserProfileActivity.this, MessageChatActivity.class);
                openActivity.putExtra("user_id", UserId);
                openActivity.putExtra("username", username);
                openActivity.putExtra("user_image", image);
                openActivity.putExtra("name", name);
                startActivity(openActivity);
            }
        });

            awardBadge();
            initLocation();


    }

    private void initLocation() {

        mLocation = (TextView) findViewById(R.id.location);
        mDatabaseUsers.child(UserId).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("state")) {
                    String state = dataSnapshot.child("state").getValue().toString();
                    String city = dataSnapshot.child("city").getValue().toString();
                    if (state != null || city != null) {
                        if (state != null) {
                            mLocation.setText(state);
                        } else if (city != null) {
                            mLocation.setText(city);
                        }
                    } else {

                        LinearLayout loc_liny = (LinearLayout) findViewById(R.id.location_liny);
                        loc_liny.setVisibility(View.GONE);
                    }
                } else {
                    LinearLayout loc_liny = (LinearLayout) findViewById(R.id.location_liny);
                    loc_liny.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void awardBadge() {

        mDatabaseUsers.child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long users_points = (Long) dataSnapshot.child("points_earned").getValue();
                String users_pints = (String) dataSnapshot.child("bio").getValue();
                String user_reputation = dataSnapshot.child("reputation").getValue().toString();

                ImageView badge = (ImageView) findViewById(R.id.brainy_badge);

                if (users_points < 100) {

                    mDatabaseUsers.child(UserId).child("reputation").setValue("Beginner");
                    badge.setVisibility(View.GONE);
                    // PREVELAGE

                } else if (users_points > 100 && users_points < 999) {

                    mDatabaseUsers.child(UserId).child("reputation").setValue("Smart");
                    badge.setImageResource(R.drawable.smart_badge);

                } else if (users_points > 1000 && users_points < 2999) {

                    mDatabaseUsers.child(UserId).child("reputation").setValue("Intelligent");
                    badge.setImageResource(R.drawable.intelligent_badge);

                }  else if (users_points > 3000 && users_points < 4999) {

                    mDatabaseUsers.child(UserId).child("reputation").setValue("Erevu");
                    badge.setImageResource(R.drawable.brainy_badge);

                } else if (users_points > 10000 ) {

                    mDatabaseUsers.child(UserId).child("reputation").setValue("Super Erevu");
                    badge.setImageResource(R.drawable.super_brainy_badge);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }*/

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
                    ViewAboutProfileTab tab1 = new  ViewAboutProfileTab();
                    Bundle bundle = new Bundle();
                    bundle.putString("UserId", UserId);
                    tab1.setArguments(bundle);
                    return tab1;
                case 1:
                    ViewAnsProfileTab tab2 = new ViewAnsProfileTab ();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("UserId", UserId);
                    tab2.setArguments(bundle2);
                    return tab2;
                case 2:
                    ViewQuizProfileTab tab3 = new ViewQuizProfileTab();
                    Bundle bundle3 = new Bundle();
                    bundle3.putString("UserId", UserId);
                    tab3.setArguments(bundle3);
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
