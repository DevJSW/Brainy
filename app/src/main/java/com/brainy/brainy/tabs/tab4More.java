package com.brainy.brainy.tabs;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brainy.brainy.MainActivity;
import com.brainy.brainy.R;
import com.brainy.brainy.activity.ProfileActiity;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab4More extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab4_more, container, false);


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
                startActivity(new Intent(new Intent(getActivity(), ProfileActiity.class)));
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
                String shareBody ="Download Churchblaze messenger on google play store today";
                String shareSub = "Dear ";
                myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
                myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                startActivity(Intent.createChooser(myIntent,"Invite a friend"));
            }
        });

        return v;  }

}
