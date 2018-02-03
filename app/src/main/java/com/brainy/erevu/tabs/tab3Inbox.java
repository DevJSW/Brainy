package com.brainy.erevu.tabs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.activity.SigninActivity;
import com.brainy.erevu.Pojos.Answer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
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
public class tab3Inbox extends Fragment {

    String  personName = null;
    String  personEmail = null;
    String  personId = null;
    Uri personPhoto = null;
    private Button signIn;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mFavouriteList;
    private DatabaseReference mDatabaseUsers, mDatabaseUserFavourites, mDatabaseUserInbox, mDatabase;
    private FirebaseAuth auth;
    private TextView mNoFavouriteTxt;
    private static final String TAG = "tab2Inbox";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;
    LinearLayoutManager mLinearlayout;
    public tab3Inbox() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab2_inbox, container, false);

        // AUTH
        auth = FirebaseAuth.getInstance();

        // database channels
        mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        if (auth.getCurrentUser() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_inbox").child(auth.getCurrentUser().getUid());
            mDatabase.keepSynced(true);
        }

        // SYNC DATABASE
        mDatabaseUserInbox.keepSynced(true);
        mDatabaseUsers.keepSynced(true);


        mFavouriteList = (RecyclerView) view.findViewById(R.id.Inbox_list);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mFavouriteList.setHasFixedSize(true);
        mLinearlayout = new LinearLayoutManager(getActivity());
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);
        mFavouriteList.setLayoutManager(mLinearlayout);

        if (auth.getCurrentUser() != null) {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            mNoFavouriteTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout faviurite_layout = (RelativeLayout) view.findViewById(R.id.inbox_layout);

            faviurite_layout.setVisibility(View.VISIBLE);
            mNoFavouriteTxt.setVisibility(View.GONE);
            signinBtn.setVisibility(View.GONE);

        } else {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            mNoFavouriteTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout faviurite_layout = (RelativeLayout) view.findViewById(R.id.inbox_layout);

            faviurite_layout.setVisibility(View.GONE);
            mNoFavouriteTxt.setVisibility(View.VISIBLE);
            signinBtn.setVisibility(View.VISIBLE);
        }

        mNoFavouriteTxt = (TextView) view.findViewById(R.id.noInboxTxt);
        if (auth.getCurrentUser() != null) {
            mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoFavouriteTxt.setVisibility(View.VISIBLE);
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
        newPost.child(auth.getCurrentUser().getUid()).child("points_earned").setValue("0");
        newPost.child(auth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

    }


    void refreshItems() {
        // Load items
        // ...

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }


    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView post_reason, post_name, post_answer;
        private DatabaseReference mDatabase, mDatabaseUserFavourites, mDatabaseUserInbox;
        FirebaseAuth auth;
        Typeface custom_font;
        ProgressBar mProgressBar;
        Context context;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
            post_answer = (TextView) mView.findViewById(R.id.posted_answer);
            post_name = (TextView) mView.findViewById(R.id.post_name);
            post_reason = (TextView) mView.findViewById(R.id.posted_reason);
            auth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_inbox").child(auth.getCurrentUser().getUid());
            mDatabase.keepSynced(true);
        }

        public void setPosted_date(Long posted_date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            post_date.setReferenceTime(Long.parseLong(String.valueOf(posted_date)));
        }

        public void setPosted_reason(String posted_reason) {

            TextView post_reason = (TextView) mView.findViewById(R.id.posted_reason);
            post_reason.setText(posted_reason);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }


        public void setPosted_answer(String posted_answer) {

            TextView post_title = (TextView) mView.findViewById(R.id.posted_answer);
            post_title.setText(posted_answer);
        }

        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.sender_image);

            Glide.with(ctx)
                    .load(sender_image).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(civ) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            civ.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }

    }
    @Override
    public void onResume() {
        super.onResume();

        if (auth.getCurrentUser() != null) {
            FirebaseRecyclerAdapter<Answer, tab3Inbox.LetterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Answer, tab3Inbox.LetterViewHolder>(

                    Answer.class,
                    R.layout.inbox_item,
                    tab3Inbox.LetterViewHolder.class,
                    mDatabaseUserInbox.child(auth.getCurrentUser().getUid())


            ) {
                @Override
                protected void populateViewHolder(final tab3Inbox.LetterViewHolder viewHolder, final Answer model, int position) {

                    final String quiz_key = getRef(position).getKey();
                    final String PostKey = getRef(position).getKey();

                    viewHolder.setSender_name(model.getSender_name());
                    viewHolder.setPosted_date(model.getPosted_date());
                    viewHolder.setPosted_reason(model.getPosted_reason());
                    viewHolder.setPosted_answer(model.getPosted_answer());
                    viewHolder.setSender_image(getContext(),model.getSender_image());

                    mDatabase.child(quiz_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();

                            if (read_status.equals(true) ) {

                                viewHolder.post_answer.setTextColor(Color.parseColor("#ABABAB"));
                                viewHolder.post_reason.setTextColor(Color.parseColor("#FF75B4E9"));
                                viewHolder.post_name.setTextColor(Color.parseColor("#FFE99639"));

                            } else {

                                viewHolder.post_answer.setTextColor(Color.BLACK);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDatabase.child(quiz_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String Quiz_key = dataSnapshot.child("question_key").getValue().toString();

                                    //MARK MESSAGE AS READ ON DB
                                    mDatabase.child(quiz_key).child("read").setValue(true);

                                    Intent openRead = new Intent(getActivity(), ReadQuestionActivity.class);
                                    openRead.putExtra("question_id", Quiz_key );
                                    startActivity(openRead);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            // custom dialog
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.setContentView(R.layout.inbox_popup_dialog);
                            dialog.setTitle("Inbox Options");

                            LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                            deleteLiny.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    AlertDialog diaBox = AskOption();
                                    diaBox.show();
                                    dialog.dismiss();
                                }
                            });


                            // if button is clicked, close the custom dialog

                            dialog.show();
                            return false;
                        }

                        private AlertDialog AskOption() {
                            AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                                    //set message, title, and icon
                                    .setTitle("Delete Alert!")
                                    .setMessage("Are you sure you want to remove this message from your inbox!")

                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //your deleting code

                                            mDatabase.child(quiz_key).removeValue();
                                            dialog.dismiss();
                                            Toast.makeText(getActivity(), "Message deleted!",Toast.LENGTH_SHORT).show();
                                        }

                                    })


                                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();

                                        }
                                    })
                                    .create();
                            return myQuittingDialogBox;
                        }
                    });


                }


            };

            mFavouriteList.setAdapter(firebaseRecyclerAdapter);

        }

    }

}
