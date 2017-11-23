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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.Adapters.InboxAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.activity.SigninActivity;
import com.brainy.erevu.data.Answer;
import com.brainy.erevu.data.Question;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A fragment with a Google +1 button.
 */
public class tab2Inbox extends Fragment {

    String  personName = null;
    String  personEmail = null;
    String  personId = null;
    String  personGender = null;
    String  personBirthday = null;
    Uri personPhoto = null;
    URL personPhoto2 = null;
    private Button signIn;
    private DatabaseReference mDatabaseUserInbox, mDatabase;
    private static final String TAG = "tab2Inbox";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 1;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth auth;
    private TextView mNoInbox;
    private RecyclerView mInboxList;

    SwipeRefreshLayout mSwipeRefreshLayout;
    LoginButton login_button;
    CallbackManager callbackManager;

    InboxAdapter inboxAdapter;
    private final List<Answer> questionList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";


    public tab2Inbox() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab2_inbox, container, false);

        callbackManager = CallbackManager.Factory.create();

        login_button = (LoginButton) view.findViewById(R.id.login_button);
        initFb();

        // Database channels
        mDatabaseUserInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUserInbox.keepSynced(true);
        mDatabaseUsers.keepSynced(true);


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
        inboxAdapter = new InboxAdapter(getActivity(), questionList);

        mInboxList = (RecyclerView) view.findViewById(R.id.Inbox_list);
        mLinearlayout = new LinearLayoutManager(getActivity());
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);

        mInboxList.setHasFixedSize(true);
        mInboxList.setLayoutManager(mLinearlayout);
        mInboxList.setAdapter(inboxAdapter);

      /*  questionList.clear();
        if (auth.getCurrentUser() != null) {
            LoadMessage();
        } else {}
*/

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

    private void initFb() {



    }

    private void LoadMoreMessage() {

        Query quizQuery = mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).orderByKey().endAt(mLastKey).limitToLast(15);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Answer message = dataSnapshot.getValue(Answer.class);

                questionList.add(itemPos++, message);
                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                }

                inboxAdapter.notifyDataSetChanged();
                inboxAdapter.notifyItemInserted(0);

                mSwipeRefreshLayout.setRefreshing(false);

              /*  mQuestionsList.scrollToPosition(questionList.size()-1);*/

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        questionList.clear();
        if (auth.getCurrentUser() != null) {
            LoadMessage();
        } else {}
    }

    private void LoadMessage() {
        Query quizQuery = mDatabaseUserInbox.child(auth.getCurrentUser().getUid()).limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Answer message = dataSnapshot.getValue(Answer.class);
                questionList.add(message);
                inboxAdapter.notifyDataSetChanged();
                inboxAdapter.notifyItemInserted(0);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

   /* public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView posted_reason;
        public TextView post_name;
        public TextView post_answer;
        public RelativeTimeTextView post_date;
        public ImageView civ;
        public TextView viewCounter, answersCounter, favouritesCounter;
        public DatabaseReference  mDatabase;
        public  FirebaseAuth mAuth;

        RelativeLayout answer_rely;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            posted_reason = (TextView) itemView.findViewById(R.id.posted_reason);
            civ = (CircleImageView) itemView.findViewById(R.id.sender_image);
            post_answer = (TextView) itemView.findViewById(R.id.posted_answer);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            //Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
           // post_date.setTypeface(custom_font);

            mAuth= FirebaseAuth.getInstance();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_inbox").child(mAuth.getCurrentUser().getUid());
            mDatabase.keepSynced(true);
            viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);
            answer_rely = (RelativeLayout) mView.findViewById(R.id.anser_rely);

        }

        public void setPosted_date(String posted_date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            post_date.setReferenceTime(Long.parseLong(String.valueOf(posted_date)));
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setQuestion_body(String question_body) {

            TextView post_body = (TextView) mView.findViewById(R.id.post_quiz_body);
            post_body.setText(question_body);
        }

       *//* public void setQuestion_title(String question_title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            post_title.setText(question_title);
        }
*//*
        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

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
    public void onStart() {
        super.onStart();

        if (auth.getCurrentUser() != null) {
            FirebaseRecyclerAdapter<Answer, tab2Inbox.LetterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Answer, tab2Inbox.LetterViewHolder>(

                    Answer.class,
                    R.layout.inbox_item,
                    tab2Inbox.LetterViewHolder.class,
                    mDatabaseUserInbox.child(auth.getCurrentUser().getUid())


            ) {
                @Override
                protected void populateViewHolder(final tab2Inbox.LetterViewHolder viewHolder, final Answer model, int position) {

                    final String answer_key = getRef(position).getKey();
                    auth= FirebaseAuth.getInstance();


                   // viewHolder.posted_reason.setText(c.getPosted_reason());
                    viewHolder.setSender_name(model.getSender_name());
                   // viewHolder.setQuestion_title(model.getPosted_quiz_title());
                    viewHolder.setSender_image(getContext(),model.getSender_image());
                   *//* viewHolder.post_answer.setText(c.getPosted_answer());
                    viewHolder.post_name.setText(c.getSender_name());*//*
                    //viewHolder.post_date.setReferenceTime(Long.parseLong(String.valueOf(c.getPosted_date())));

                    mDatabase.child(answer_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();

                            if (read_status.equals(true) ) {

                                viewHolder.post_answer.setTextColor(Color.parseColor("#ABABAB"));

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
                        public void onClick(View v) {

                            mDatabase.child(answer_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String quiz_key = dataSnapshot.child("question_key").getValue().toString();

                                    //MARK MESSAGE AS READ ON DB
                                    mDatabase.child(answer_key).child("read").setValue(true);

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

                        private AlertDialog AskOption()
                        {

                            AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                                    //set message, title, and icon
                                    .setTitle("Delete Alert!")
                                    .setMessage("Are you sure you want to remove this message from your inbox!")

                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //your deleting code

                                            mDatabase.child(answer_key).removeValue();
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

                   *//* Glide.with(getActivity())
                            .load(c.getSender_image()).asBitmap()
                            .placeholder(R.drawable.placeholder_image)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .centerCrop()
                            .into(new BitmapImageViewTarget(holder.civ) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                                    circularBitmapDrawable.setCircular(true);
                                    holder.civ.setImageDrawable(circularBitmapDrawable);
                                }
                            });*//*

                }


            };

            mInboxList.setAdapter(firebaseRecyclerAdapter);

        }

    }
*/
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

        RelativeLayout googleBtn = (RelativeLayout) dialog.findViewById(R.id.googleBtn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               
                initGoogleSignIn();
                dialog.dismiss();
            }
        });

        Button signBtn = (Button) dialog.findViewById(R.id.sign_in);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openRead = new Intent(getActivity(), SigninActivity.class);
                startActivity(openRead);
                dialog.dismiss();
            }
        });

        RelativeLayout fbBtn = (RelativeLayout) dialog.findViewById(R.id.fbBtn);
        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initfBSignIn();
                dialog.dismiss();

            }

            private void initfBSignIn() {

                callbackManager = CallbackManager.Factory.create();
                LoginButton loginButton = (LoginButton) dialog.findViewById(R.id.login_button);
                loginButton.performClick();
                loginButton.registerCallback(callbackManager,
                        new FacebookCallback< LoginResult >() {@Override
                        public void onSuccess(LoginResult loginResult) {

                            System.out.println("onSuccess");

                            String accessToken = loginResult.getAccessToken()
                                    .getToken();
                            Log.i("accessToken", accessToken);

                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {@Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {

                                        Log.i("LoginActivity",
                                                response.toString());
                                        try {
                                            personId = object.getString("id");
                                            try {
                                                personPhoto2 = new URL(
                                                        "http://graph.facebook.com/" + personId + "/picture?type=large");
                                                Log.i("profile_pic",
                                                        personPhoto + "");

                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                            }

                                            personName = object.getString("name");
                                            personEmail = object.getString("email");
                                            personGender = object.getString("gender");
                                            personBirthday = object.getString("birthday");

                                            postFbUserInfoToDB();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields",
                                    "personId,personName,personEmail,personGender, personBirthday");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }

                            @Override
                            public void onCancel() {
                                System.out.println("onCancel");
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                System.out.println("onError");
                                Log.v("LoginActivity", exception.getCause().toString());
                            }
                        });
            }


        });


    }


    private void postFbUserInfoToDB() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateInstance().format(date);
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        final DatabaseReference newPost = mDatabaseUsers;

        newPost.child(auth.getCurrentUser().getUid()).child("name").setValue(personName);
        newPost.child(auth.getCurrentUser().getUid()).child("status").setValue("");
        newPost.child(auth.getCurrentUser().getUid()).child("user_image").setValue(personPhoto2.toString());
        newPost.child(auth.getCurrentUser().getUid()).child("joined_date").setValue(stringDate);
        newPost.child(auth.getCurrentUser().getUid()).child("personId").setValue(personId);
        newPost.child(auth.getCurrentUser().getUid()).child("uid").setValue(auth.getCurrentUser().getUid());
        newPost.child(auth.getCurrentUser().getUid()).child("user_gmail").setValue(personEmail);
        newPost.child(auth.getCurrentUser().getUid()).child("sign_in_type").setValue("facebook_signIn");
        newPost.child(auth.getCurrentUser().getUid()).child("reputation").setValue("Beginner");
        newPost.child(auth.getCurrentUser().getUid()).child("points_earned").setValue(10);
        newPost.child(auth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

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
        callbackManager.onActivityResult(requestCode, resultCode, data);

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

                Toast.makeText(getActivity(), "Sign in success!.",
                        Toast.LENGTH_LONG).show();

                getActivity().recreate();

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

}
