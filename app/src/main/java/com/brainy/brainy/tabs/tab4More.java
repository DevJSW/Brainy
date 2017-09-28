package com.brainy.brainy.tabs;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.brainy.brainy.activity.EditProfileActivity;
import com.brainy.brainy.activity.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab4More extends Fragment {

    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab4_more, container, false);

        auth = FirebaseAuth.getInstance();
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
                startActivity(new Intent(new Intent(getActivity(), EditProfileActivity.class)));

                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(new Intent(getActivity(), EditProfileActivity.class)));
                } else {
                    Snackbar snackbar = Snackbar
                            .make(v, "You need to be signed in order for you to post a question!", Snackbar.LENGTH_LONG)
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
