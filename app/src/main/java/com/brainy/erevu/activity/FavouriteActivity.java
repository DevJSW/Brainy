package com.brainy.erevu.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brainy.erevu.Pojos.Question;
import com.brainy.erevu.R;
import com.brainy.erevu.tabs.tab3Achievements;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavouriteActivity extends AppCompatActivity {

    private RecyclerView mFavouriteList;
    private DatabaseReference mDatabaseUsers, mDatabaseUserFavourites, mDatabaseUsersFavourite;
    private FirebaseAuth auth;
    private ImageView backBtn;
    private TextView mNoFavouriteTxt;
    private ProgressBar progressBar;
    LinearLayoutManager mLinearlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(my_toolbar);

        Window window = FavouriteActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( FavouriteActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // AUTH
        auth = FirebaseAuth.getInstance();

        // database channels
        mDatabaseUserFavourites = FirebaseDatabase.getInstance().getReference().child("Users_favourite");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // SYNC DATABASE
        mDatabaseUserFavourites.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        mFavouriteList = (RecyclerView) findViewById(R.id.favourite_list);
        mFavouriteList.setHasFixedSize(true);
        mLinearlayout = new LinearLayoutManager(this);
        mLinearlayout.setReverseLayout(true);
        mLinearlayout.setStackFromEnd(true);
        mFavouriteList.setLayoutManager(mLinearlayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mNoFavouriteTxt = (TextView) findViewById(R.id.noFavourTxt);
        if (auth.getCurrentUser() != null) {
            mDatabaseUserFavourites.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FavouriteActivity.this.finish();
            }
        });

    }

    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView viewCounter, answersCounter, favouritesCounter;
        private DatabaseReference mDatabaseUsers, mDatabaseUserFavourites, mDatabaseUsersFavourite;
        FirebaseAuth auth;
        Typeface custom_font;
        ProgressBar mProgressBar;
        Context context;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);
            mDatabaseUserFavourites = FirebaseDatabase.getInstance().getReference().child("Users_favourite");
            auth = FirebaseAuth.getInstance();
           /* custom_font = Typeface.createFromAsset(itemView.getAssets(), "fonts/Aller_Rg.ttf");*/
        }

        public void setPosted_date(String posted_date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(posted_date);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setQuestion_body(String question_body) {

            TextView post_body = (TextView) mView.findViewById(R.id.post_quiz_body);
            post_body.setText(question_body);
        }

        public void setQuestion_title(String question_title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            post_title.setText(question_title);
        }

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
            FirebaseRecyclerAdapter<Question, FavouriteActivity.LetterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Question, FavouriteActivity.LetterViewHolder>(

                    Question.class,
                    R.layout.favourite_item,
                    FavouriteActivity.LetterViewHolder.class,
                    mDatabaseUserFavourites.child(auth.getCurrentUser().getUid())


            ) {
                @Override
                protected void populateViewHolder(final FavouriteActivity.LetterViewHolder viewHolder, final Question model, int position) {

                    final String quiz_key = getRef(position).getKey();
                    final String PostKey = getRef(position).getKey();

                    viewHolder.setSender_name(model.getSender_name());
                    //viewHolder.setPosted_date(model.getPosted_date());
                    viewHolder.setQuestion_body(model.getQuestion_body());
                    viewHolder.setQuestion_title(model.getQuestion_title());
                    viewHolder.setSender_image(FavouriteActivity.this,model.getSender_image());

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent openRead = new Intent(FavouriteActivity.this, ReadQuestionActivity.class);
                            openRead.putExtra("question_id", quiz_key );
                            startActivity(openRead);

                        }
                    });

                    viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            Snackbar snackbar = Snackbar
                                    .make(view, "Remove post from favourites!", Snackbar.LENGTH_LONG)
                                    .setAction("REMOVE", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mDatabaseUserFavourites
                                                    .child(auth.getCurrentUser().getUid())
                                                    .child(quiz_key)
                                                    .removeValue();
                                        }
                                    });

                            snackbar.show();
                            return true;
                        }
                    });

                }


            };

            mFavouriteList.setAdapter(firebaseRecyclerAdapter);

        }

    }
}
