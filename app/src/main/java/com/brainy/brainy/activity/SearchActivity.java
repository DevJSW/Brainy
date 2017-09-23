package com.brainy.brainy.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brainy.brainy.Adapters.InboxAdapter;
import com.brainy.brainy.Adapters.SearchAdapter;
import com.brainy.brainy.Adapters.SolutionsAdapter;
import com.brainy.brainy.R;
import com.brainy.brainy.data.Answer;
import com.brainy.brainy.data.Question;
import com.brainy.brainy.tabs.tab3Achievements;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private RecyclerView mSearchList;
    private EditText mSearchInput;
    SearchAdapter searchAdapter;
    private final List<Question> searchList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");

        searchAdapter = new SearchAdapter(SearchActivity.this,searchList);

        mSearchList = (RecyclerView) findViewById(R.id.search_list);
        mLinearlayout = new LinearLayoutManager(SearchActivity.this);

        mSearchList.setHasFixedSize(true);
        mSearchList.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mSearchList.setAdapter(searchAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setIconified(false);
        searchView.setQueryHint("Search question...");
        searchView.requestFocus();
        menuItem.expandActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

       /* if (!query.isEmpty()) {
            Intent searchIntent = new Intent(getActivity(), MainActivity.class);
            searchIntent.setAction(Intent.ACTION_SEARCH);
            searchIntent.putExtra(SearchManager.QUERY, query);
            startActivity(searchIntent);
        }*/



        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

            Query quizQuery = mDatabase.orderByChild("question_title").startAt(newText.toUpperCase());

            searchList.clear();
            searchAdapter.notifyDataSetChanged();
        searchAdapter.notifyItemInserted(0);

            quizQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Question message = dataSnapshot.getValue(Question.class);

                    searchList.add(message);
                    searchAdapter.notifyDataSetChanged();
                    searchAdapter.notifyItemInserted(0);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    Question message = dataSnapshot.getValue(Question.class);

                    searchList.add(message);
                    searchAdapter.notifyDataSetChanged();
                    searchAdapter.notifyItemInserted(0);

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
        mSearchList.setAdapter(searchAdapter);
        return false;

    }

}
