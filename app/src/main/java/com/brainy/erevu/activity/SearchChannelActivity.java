package com.brainy.erevu.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.Pojos.Group;
import com.brainy.erevu.Pojos.Users;
import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchChannelActivity extends AppCompatActivity {

    private EditText mSearchInput;
    private ImageView backBtn;
    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_search);
        Window window = SearchChannelActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( SearchChannelActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUserDatabase = FirebaseDatabase.getInstance().getReference("Channels");

        mSearchInput = (EditText) findViewById(R.id.search_edit);
        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = mSearchInput.getText().toString();
                if (searchText.length() > 1) {
                    firebaseUserSearch(searchText);
                } else {

                }
            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchChannelActivity.this.finish();
            }
        });
    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = mUserDatabase.orderByChild("group_name").startAt("#"+searchText).endAt("#"+searchText + "\uf8ff");

        FirebaseRecyclerAdapter<Group, SearchChannelActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, SearchChannelActivity.UsersViewHolder>(

                Group.class,
                R.layout.channel_search_item,
                SearchChannelActivity.UsersViewHolder.class,
                firebaseSearchQuery

        ) {
            @Override
            protected void populateViewHolder(SearchChannelActivity.UsersViewHolder viewHolder, final Group model, int position) {
                final String user_id = getRef(position).getKey();

              /*  mUserDatabase.child("user_id").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/

                viewHolder.setDetails(getApplicationContext(), model.getGroup_name(), model.getGroup_image());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openRead = new Intent(SearchChannelActivity.this, ChannelChatroomActivity.class);
                        openRead.putExtra("group_id", model.getGroup_id() );
                        openRead.putExtra("founder_id", model.getFounder_id() );
                        openRead.putExtra("group_name", model.getGroup_name());
                        openRead.putExtra("group_image", model.getGroup_image() );
                        startActivity(openRead);
                        SearchChannelActivity.this.finish();
                    }
                });

               /* viewHolder.user_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openRead = new Intent(SearchChannelActivity.this, ViewUserProfileActivity.class);
                        openRead.putExtra("user_id", user_id );
                        startActivity(openRead);
                    }
                });*/
            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }

    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView user_image;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            user_image = (CircleImageView) mView.findViewById(R.id.post_image);
        }

        public void setDetails(Context ctx, String userName, String userImage){

            TextView user_name = (TextView) mView.findViewById(R.id.post_name);

            user_name.setText(userName);

            Glide.with(ctx).load(userImage).placeholder(R.drawable.placeholder_image).into(user_image);


        }


    }
}
