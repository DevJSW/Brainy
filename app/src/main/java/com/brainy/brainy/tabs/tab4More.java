package com.brainy.brainy.tabs;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.brainy.R;
import com.brainy.brainy.activity.EditProfileActivity;
import com.brainy.brainy.activity.ProfileActivity;
import com.brainy.brainy.activity.ProfileEditActivity;
import com.brainy.brainy.activity.SigninActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.DateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab4More extends Fragment {

    private FirebaseAuth auth;
    String  personName = null;
    String  personEmail = null;
    String  personId = null;
    Uri personPhoto = null;

    private static final String TAG = "tab2Inbox";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab4_more, container, false);

        auth = FirebaseAuth.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        LinearLayout openSettings = (LinearLayout) v.findViewById(R.id.lin_settings);
        openSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*startActivity(new Intent(getActivity(), SettingsActivity.class));*/
            }
        });

        LinearLayout openEditProfile = (LinearLayout) v.findViewById(R.id.lin_editprofile);
        openEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(new Intent(getActivity(), EditProfileActivity.class)));
                } else {
                    Snackbar snackbar = Snackbar
                            .make(v, "You need to be sign in!", Snackbar.LENGTH_LONG)
                            .setAction("SIGN IN", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showSignInDialog();
                                }
                            });

                    snackbar.show();
                }
            }
        });

        LinearLayout openAboutApp = (LinearLayout) v.findViewById(R.id.lin_about_app);
        openAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Context context = getActivity();

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.about_app_dialog);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();

                TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        LinearLayout openPrivacy = (LinearLayout) v.findViewById(R.id.lin_rate_app);
        openPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // open from a browser https://churchblaze.com/help/privacy
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://churchblaze.com/help/privacy"));
                startActivity(browserIntent);
            }
        });

        LinearLayout openInvite = (LinearLayout) v.findViewById(R.id.lin_invite);
        openInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody ="Download Brainy on google play store today";
                String shareSub = "Dear ";
                myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
                myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                startActivity(Intent.createChooser(myIntent,"Invite a friend"));
            }
        });

        return v;  }


    private void showSignInDialog() {

        final Context context = getActivity();

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.sign_in_dialog);
        dialog.setTitle("Let's get started...");
        dialog.show();

        Button signBtn = (Button) dialog.findViewById(R.id.sign_in);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openRead = new Intent(getActivity(), SigninActivity.class);
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

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(getActivity(), "Failed to connect to Google, check your internet connection.",
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

                postUserInfoToDB();
                Toast.makeText(getActivity(), "Sign in success!.",
                        Toast.LENGTH_LONG).show();

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
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            postUserInfoToDB();
                            postUserInfoToDB();
                            Toast.makeText(getActivity(), "Sign in success!.",
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        /*menu.findItem(R.id.action_search).setVisible(false);*/
        menu.clear();
    }
}
