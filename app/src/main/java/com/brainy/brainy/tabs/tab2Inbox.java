package com.brainy.brainy.tabs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.brainy.brainy.R;
import com.brainy.brainy.activity.ReadQuestionActivity;
import com.brainy.brainy.data.Answer;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A fragment with a Google +1 button.
 */
public class tab2Inbox extends Fragment {

    String  personName = null;
    String  personEmail = null;
    String  personId = null;
    Uri personPhoto = null;
    private Button signIn;
    private DatabaseReference mDatabaseUserInbox;
    private static final String TAG = "tab2Inbox";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth auth;
    private TextView mNoInbox;
    private RecyclerView mInboxList;

    public tab2Inbox() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab2_inbox, container, false);


        // Database channels
        mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUserInbox.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

       /* //RECYCLERVIEW
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
*/
        mInboxList = (RecyclerView) view.findViewById(R.id.Inbox_list);
        mInboxList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mInboxList.setHasFixedSize(true);

        //auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            TextView noPostTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout inbox_layout = (RelativeLayout) view.findViewById(R.id.inbox_layout);

            inbox_layout.setVisibility(View.VISIBLE);
            noPostTxt.setVisibility(View.GONE);
            signinBtn.setVisibility(View.GONE);
        } else {

            Button signinBtn = (Button) view.findViewById(R.id.signIn);
            TextView noPostTxt = (TextView) view.findViewById(R.id.noPostTxt);
            RelativeLayout inbox_layout = (RelativeLayout) view.findViewById(R.id.inbox_layout);

            inbox_layout.setVisibility(View.GONE);
            noPostTxt.setVisibility(View.VISIBLE);
            signinBtn.setVisibility(View.VISIBLE);
        }

        mNoInbox = (TextView) view.findViewById(R.id.noInboxTxt);


        if (auth.getCurrentUser() != null) {
            mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {

                        mNoInbox.setVisibility(View.VISIBLE);
                    } else {
                        mNoInbox.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {}

        signIn = (Button) view.findViewById(R.id.signIn);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        //Get Firebase auth instance


        initSignIn();

        return view;
    }

    private void initSignIn() {

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showSignInDialog();
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

        Button googleBtn = (Button) dialog.findViewById(R.id.googleBtn);
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

                        }

                        // ...
                    }
                });
    }

    private void postUserInfoToDB() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);


        final DatabaseReference newPost = mDatabaseUsers;

        newPost.child(auth.getCurrentUser().getUid()).child("name").setValue(personName);
        newPost.child(auth.getCurrentUser().getUid()).child("status").setValue("");
        newPost.child(auth.getCurrentUser().getUid()).child("user_image").setValue(personPhoto.toString());
        newPost.child(auth.getCurrentUser().getUid()).child("joined_date").setValue(stringDate);
        newPost.child(auth.getCurrentUser().getUid()).child("personId").setValue(personId);
        newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
        newPost.child(auth.getCurrentUser().getUid()).child("user_gmail").setValue(personEmail);
        newPost.child(auth.getCurrentUser().getUid()).child("sign_in_type").setValue("google_signIn");

    }

        @Override
        public void onStart() {
            super.onStart();
            if (auth.getCurrentUser() != null) {
                final FirebaseRecyclerAdapter<Answer, AnswerViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Answer, AnswerViewHolder>(

                        Answer.class,
                        R.layout.inbox_item,
                        AnswerViewHolder.class,
                        mDatabaseUserInbox.child(auth.getCurrentUser().getUid())


                ) {
                    @Override
                    protected void populateViewHolder(final AnswerViewHolder viewHolder, final Answer model, final int position) {

                        final String answer_key = getRef(position).getKey();

                        viewHolder.setSender_name(model.getSender_name());
                        viewHolder.setPosted_date(model.getPosted_date());
                        viewHolder.setPosted_answer(model.getPosted_answer());
                        viewHolder.setSender_image(getContext(), model.getSender_image());
                        viewHolder.setPosted_reason(model.getPosted_reason());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).child(answer_key).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String quiz_key = dataSnapshot.child("quiz_key").getValue().toString();

                                        Intent openRead = new Intent(getActivity(), ReadQuestionActivity.class);
                                        openRead.putExtra("question_id", quiz_key );
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
                            public boolean onLongClick(View v) {


                                final Context context = getActivity();

                                // custom dialog
                                final Dialog dialog = new Dialog(context);
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

                            private AlertDialog AskOption()
                            {

                                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                                        //set message, title, and icon
                                        .setTitle("Delete Alert!")
                                        .setMessage("Are you sure you want to remove this message from your inbox!")

                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                //your deleting code

                                                mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).child(answer_key).removeValue();
                                                dialog.dismiss();
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

                mInboxList.setAdapter(firebaseRecyclerAdapter);
            } else {}
        }

        public static class AnswerViewHolder extends RecyclerView.ViewHolder {

            View mView;

            DatabaseReference mDatabaseUnread;
            FirebaseAuth mAuth;
            ProgressBar mProgressBar;

            public AnswerViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                mAuth = FirebaseAuth.getInstance();

            }

            public void setPosted_date(String posted_date) {

                TextView post_date = (TextView) mView.findViewById(R.id.post_date);
                post_date.setText(posted_date);
            }

            public void setSender_name(String sender_name) {

                TextView post_name = (TextView) mView.findViewById(R.id.post_name);
                post_name.setText(sender_name);
            }

            public void setPosted_answer(String posted_answer) {

                TextView post_answer2 = (TextView) mView.findViewById(R.id.posted_answer);
                post_answer2.setText(posted_answer);
            }

            public void setPosted_reason(String posted_reason) {

                TextView post_reason = (TextView) mView.findViewById(R.id.posted_reason);
                post_reason.setText(posted_reason);
            }

            public void setSender_image(final Context ctx, final String sender_image) {

                final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.sender_image);

                Picasso.with(ctx).load(sender_image).networkPolicy(NetworkPolicy.OFFLINE).into(civ, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {


                        Picasso.with(ctx).load(sender_image).into(civ);
                    }
                });
            }

        }



}
