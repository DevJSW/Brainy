package com.brainy.erevu.tabs;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Pojos.Group;
import com.brainy.erevu.Pojos.Question;
import com.brainy.erevu.R;
import com.brainy.erevu.activity.GroupActivity;
import com.brainy.erevu.activity.GroupChatroomActivity;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.activity.SigninActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment with a Google +1 button.
 */
public class tabGroups extends Fragment {

    private Button signIn;
    private RecyclerView mGroupList;
    private DatabaseReference mDatabaseUsers, mDatabaseGroups, mDatabaseGroupChatlist;
    private FirebaseAuth auth;
    private TextView mNoFavouriteTxt;
    private ProgressBar progressBar;
    LinearLayoutManager mLinearlayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab3_achievements, container, false);

        // AUTH
        auth = FirebaseAuth.getInstance();

        // database channels
        mDatabaseGroups = FirebaseDatabase.getInstance().getReference().child("Users_groups");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");

        // SYNC DATABASE
        mDatabaseGroups.keepSynced(true);
        mDatabaseGroupChatlist.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        mGroupList = (RecyclerView) view.findViewById(R.id.favourite_list);
        mGroupList.setHasFixedSize(true);
        mLinearlayout = new LinearLayoutManager(getActivity());
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);
        mGroupList.setLayoutManager(mLinearlayout);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        if (auth.getCurrentUser() != null) {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            mNoFavouriteTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout faviurite_layout = (RelativeLayout) view.findViewById(R.id.favourite_layout);

            faviurite_layout.setVisibility(View.VISIBLE);
            mNoFavouriteTxt.setVisibility(View.GONE);
            signinBtn.setVisibility(View.GONE);

        } else {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            mNoFavouriteTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout faviurite_layout = (RelativeLayout) view.findViewById(R.id.favourite_layout);

            faviurite_layout.setVisibility(View.GONE);
            mNoFavouriteTxt.setVisibility(View.VISIBLE);
            signinBtn.setVisibility(View.VISIBLE);
        }

        mNoFavouriteTxt = (TextView) view.findViewById(R.id.noFavourTxt);
        if (auth.getCurrentUser() != null) {
            mDatabaseGroups.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoFavouriteTxt.setVisibility(View.VISIBLE);
                        mNoFavouriteTxt.setText("Your group list is empty.");
                    } else {
                        mNoFavouriteTxt.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        signIn = (Button) view.findViewById(R.id.signIn);
        initSignIn();
        loadGroups();
        return view;
    }

    private void initSignIn() {

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openRead = new Intent(getActivity(), SigninActivity.class);
                startActivity(openRead);
                //showSignInDialog();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
    }

    private void loadGroups() {

        FirebaseRecyclerAdapter<Group, tabGroups.GroupsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, tabGroups.GroupsViewHolder>(

                Group.class,
                R.layout.group_item,
                tabGroups.GroupsViewHolder.class,
                mDatabaseGroups.child(auth.getCurrentUser().getUid())

        ) {
            @Override
            protected void populateViewHolder(tabGroups.GroupsViewHolder viewHolder, final Group model, int position) {

                viewHolder.setDetails(getActivity(), model.getGroup_name(), model.getMessage(), model.getGroup_image());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openRead = new Intent(getActivity(), GroupChatroomActivity.class);
                        openRead.putExtra("group_id", model.getGroup_id() );
                        openRead.putExtra("group_name", model.getGroup_name());
                        openRead.putExtra("group_image", model.getGroup_image() );
                        startActivity(openRead);
                    }
                });
            }
        };

        mGroupList.setAdapter(firebaseRecyclerAdapter);

    }

    // View Holder Class

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        DatabaseReference mDatabaseGroupChatlist;

        public GroupsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");
        }

        public void setDetails(Context ctx, String groupName, String groupMessage, String groupImage){

            TextView group_name = (TextView) mView.findViewById(R.id.group_name);
            TextView group_message = (TextView) mView.findViewById(R.id.post_message);
            CircleImageView user_image = (CircleImageView) mView.findViewById(R.id.post_image);

            group_name.setText(groupName);
            group_message.setText(groupMessage);

            Glide.with(ctx)
                    .load(groupImage)
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(user_image);

        }


    }

}
