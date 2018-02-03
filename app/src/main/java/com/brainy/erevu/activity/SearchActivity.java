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

import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Question;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class SearchActivity extends AppCompatActivity{

    private EditText mSearchInput;
    private ImageView backBtn;
    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Window window = SearchActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( SearchActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");

        mSearchInput = (EditText) findViewById(R.id.search_edit);
        mResultList = (RecyclerView) findViewById(R.id.search_list);
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
                if (searchText.length() > 2) {
                    firebaseUserSearch(searchText);
                } else {

                }
            }
        });

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.this.finish();
            }
        });
        //initSearch();

    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = mUserDatabase.orderByChild("question_title").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerAdapter<Question, SearchActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Question, SearchActivity.UsersViewHolder>(

                Question.class,
                R.layout.search_item,
                SearchActivity.UsersViewHolder.class,
                firebaseSearchQuery

        ) {
            @Override
            protected void populateViewHolder(SearchActivity.UsersViewHolder viewHolder, final Question model, int position) {

                viewHolder.setDetails(getApplicationContext(), model.getQuestion_title());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openRead = new Intent(SearchActivity.this, ReadQuestionActivity.class);
                        openRead.putExtra("question_id", model.getPost_id() );
                        startActivity(openRead);
                    }
                });
            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }

    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDetails(Context ctx, String quizTitle){

            TextView quiz_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            //TextView quiz_body = (TextView) mView.findViewById(R.id.post_username);

            quiz_title.setText(quizTitle);
            //quiz_body.setText(quizBody);


        }


    }

}
