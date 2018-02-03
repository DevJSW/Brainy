package com.brainy.erevu.activity;

import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.brainy.erevu.Adapters.UsersAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabase, mDatabaseUsers ;
    private FirebaseAuth auth;
    private RecyclerView mSearchList;
    private EditText mSearchInput;
    UsersAdapter usersAdapter;
    private final List<Users> searchList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        usersAdapter = new UsersAdapter(FindFriendsActivity.this,searchList);

        mSearchList = (RecyclerView) findViewById(R.id.search_list);
        mLinearlayout = new LinearLayoutManager(FindFriendsActivity.this);

        mSearchList.setHasFixedSize(true);
        mSearchList.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));
        mSearchList.setAdapter(usersAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setIconified(false);
        searchView.setQueryHint("Search username...");
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

        Query quizQuery = mDatabaseUsers.orderByChild("name").equalTo(newText.toUpperCase());

        searchList.clear();
        usersAdapter.notifyDataSetChanged();
        usersAdapter.notifyItemInserted(0);

        quizQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Users message = dataSnapshot.getValue(Users.class);

                searchList.add(message);
                usersAdapter.notifyDataSetChanged();
                usersAdapter.notifyItemInserted(0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Users message = dataSnapshot.getValue(Users.class);
                searchList.clear();
                searchList.add(message);
                usersAdapter.notifyDataSetChanged();
                usersAdapter.notifyItemInserted(0);

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
        mSearchList.setAdapter(usersAdapter);
        return false;

    }

}

