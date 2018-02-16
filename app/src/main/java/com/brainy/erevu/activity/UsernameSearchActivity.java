package com.brainy.erevu.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Users;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsernameSearchActivity extends AppCompatActivity {

    String searchInput = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabase, mDatabaseUsernames;
    private FirebaseAuth auth;
    private RecyclerView mSearchList;
    private EditText mSearchInput;
    private Button submit;
    private ImageView backBtn;
    Context ctx;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_search);
        Window window = UsernameSearchActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( UsernameSearchActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsernames = FirebaseDatabase.getInstance().getReference().child("Usernames");

        mSearchList = (RecyclerView) findViewById(R.id.search_list);
        mSearchList.setLayoutManager(new LinearLayoutManager(this));
        mSearchList.setHasFixedSize(true);

        mSearchInput = (EditText) findViewById(R.id.search_edit);
        searchInput = mSearchInput.getText().toString();

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsernameSearchActivity.this.finish();
            }
        });

        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSearch(searchInput);
            }
        });

        /*mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                names.clear();
                LoadMessage();

            }

            @Override
            public void afterTextChanged(Editable editable) {

                names.clear();
                LoadMessage();

            }
        });*/

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void getDetails(final Context ctx, String Username, String Name, String User_image) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

            post_name.setText(Name);
            post_username.setText(Username);
            Glide.with(ctx)
                    .load(User_image).asBitmap()
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

    private void loadSearch(String searchInput) {


        Query userQuery = mDatabase.orderByChild("name").startAt(searchInput).endAt(searchInput + "\uf8ff");
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsernameSearchActivity.UsersViewHolder>(

                Users.class,
                R.layout.user_search_item,
                UsersViewHolder.class,
                userQuery

        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final Users model, int position) {

                final String quiz_key = getRef(position).getKey();

                viewHolder.getDetails(getApplicationContext(), model.getUsername(),model.getName(),model.getUser_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openRead = new Intent(UsernameSearchActivity.this, MessageChatActivity.class);
                        openRead.putExtra("question_id", quiz_key );
                        startActivity(openRead);

                        UsernameSearchActivity.this.finish();
                    }
                });


            }


        };

        mSearchList.setAdapter(firebaseRecyclerAdapter);
    }

}
