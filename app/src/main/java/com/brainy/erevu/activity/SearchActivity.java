package com.brainy.erevu.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brainy.erevu.Adapters.SearchAdapter;
import com.brainy.erevu.R;
import com.brainy.erevu.data.Question;
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

public class SearchActivity extends AppCompatActivity/* implements SearchView.OnQueryTextListener*/{

    String searchInput = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private RecyclerView mSearchList;
    private EditText mSearchInput;
    private ImageView backBtn;
    SearchAdapter searchAdapter;
    private final List<Question> searchList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");

        searchAdapter = new SearchAdapter(SearchActivity.this,searchList);

        mSearchList = (RecyclerView) findViewById(R.id.search_list);
        mLinearlayout = new LinearLayoutManager(SearchActivity.this);

        mSearchList.setHasFixedSize(true);
        mSearchList.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mSearchList.setAdapter(searchAdapter);

        mSearchInput = (EditText) findViewById(R.id.search_edit);
        searchInput = mSearchInput.getText().toString();

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.this.finish();
            }
        });
        initSearch();
    }

    private void initSearch() {
        final long delay = 1000; // 1 seconds after user stops typing
        final long[] last_text_edit = {0};
        final Handler handler = new Handler();

        final Runnable input_finish_checker = new Runnable() {
            public void run() {
                if (System.currentTimeMillis() > (last_text_edit[0])) {
                    // ............
                    // ............

                    //user is typing

                }
            }
        };
        
        mSearchInput.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged (CharSequence s,int start, int count,
                                                                           int after){
                                            }
                                            @Override
                                            public void onTextChanged ( final CharSequence s, int start, int before,
                                                                        int count){
                                                //You need to remove this to run only once
                                                handler.removeCallbacks(input_finish_checker);
                                                //recreate();
                                                showSearchResults();

                                            }
                                            @Override
                                            public void afterTextChanged ( final Editable s){
                                                //avoid triggering event when text is empty
                                                if (s.length() > 0) {
                                                    last_text_edit[0] = System.currentTimeMillis();
                                                    handler.postDelayed(input_finish_checker, delay);
                                                    
                                                    //showSearchResults();

                                                } else {

                                                    // USER IS TYPING
                                                   // showSearchResults();

                                                }
                                            }
                                        }

        );
    }

    private void showSearchResults() {

        Query quizQuery = mDatabase.orderByChild("question_title")
                .startAt(searchInput.toUpperCase())
                //.endAt(searchInput.toUpperCase()+"\uf8ff")
                .limitToLast(30);

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
    }


    /* @Override
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
 */
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


    /*@Override
    public boolean onQueryTextSubmit(String query) {

        if (!query.isEmpty()) {
            Intent searchIntent = new Intent(getActivity(), MainActivity.class);
            searchIntent.setAction(Intent.ACTION_SEARCH);
            searchIntent.putExtra(SearchManager.QUERY, query);
            startActivity(searchIntent);
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

            Query quizQuery = mDatabase.orderByChild("question_title")
                    .startAt(newText.toUpperCase())
                    .endAt(newText.toUpperCase()+"\uf8ff")
                    .limitToLast(30);


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

    }*/
}
