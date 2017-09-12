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

        //GET USER LOCATION
        geocoder = new Geocoder(this, Locale.getDefault());

        // create class object
        gps = new GPSTracker(brainy.this);
        // check if GPS enabled
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // \n is for new line
            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("latitude").setValue(latitude);
            mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("longitude").setValue(longitude);

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 0);

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
           /* gps.showSettingsAlert();*/
        }

    }
}
