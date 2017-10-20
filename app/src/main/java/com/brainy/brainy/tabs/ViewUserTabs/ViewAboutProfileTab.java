package com.brainy.brainy.tabs.ViewUserTabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ViewAboutProfileTab extends Fragment {

    String UserId = null;
    private DatabaseReference mDatabaseUsers, mDatabaseFavourites;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private TextView pointsEarned, reputation, userBio, favourites;


    public ViewAboutProfileTab() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about_profile_tab, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            UserId = bundle.getString("UserId", null);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId);
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("Users_favourite").child(UserId);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mDatabaseFavourites.keepSynced(true);

        favourites = (TextView) v.findViewById(R.id.favourite);
        reputation = (TextView) v.findViewById(R.id.reputation);
        userBio = (TextView) v.findViewById(R.id.user_bio);
        pointsEarned = (TextView) v.findViewById(R.id.points_earned);

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("bio") || dataSnapshot.hasChild("reputation") || dataSnapshot.hasChild("points_earned")) {

                    String user_bio = dataSnapshot.child("bio").getValue().toString();
                    String user_reputation = dataSnapshot.child("reputation").getValue().toString();
                    String user_point_earned = dataSnapshot.child("points_earned").getValue().toString();

                    userBio.setText(user_bio);
                    reputation.setText(user_reputation);
                    pointsEarned.setText(user_point_earned);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseFavourites.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                favourites.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }
}
