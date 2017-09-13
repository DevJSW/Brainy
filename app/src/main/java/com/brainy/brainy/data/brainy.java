package com.brainy.brainy.data;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;

import com.brainy.brainy.Services.GPSTracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by John on 08-Jun-17.
 */
public class brainy extends Application {

    GPSTracker gps;
    Geocoder geocoder;
    List<Address> addresses;

    private FirebaseAuth auth;
    private DatabaseReference mDatabaseUsers;

    @Override
    public void onCreate() {
        super.onCreate();

       // getCacheDir();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Picasso.Builder builder = new Picasso.Builder(this);
       /* builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));*/
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        //DATABASE
        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

    }
}
