package com.brainy.brainy.activity;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.brainy.brainy.Adapters.QuestionAdapter;
import com.brainy.brainy.data.Question;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.srx.widget.PullCallback;
import com.srx.widget.PullToLoadView;

import java.util.ArrayList;

/**
 * Created by Shephard on 8/25/2017.
 */

public class Paginator {

    Context c;
    private PullToLoadView pullToLoadView;
    private RecyclerView mQuestionsList;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private Boolean isLoading = false;
    private Boolean hasLoadedAll = false;
    private int nextPage;

    QuestionAdapter questionAdapter;
    private final ArrayList<Question> questionList = new ArrayList<>();

    public Paginator(Context c, PullToLoadView pullToLoadView) {
        this.c=c;
        this.pullToLoadView=pullToLoadView;

        RecyclerView rv = pullToLoadView.getRecyclerView();
        rv.setLayoutManager(new LinearLayoutManager(c, LinearLayoutManager.VERTICAL, false));

        questionAdapter = new QuestionAdapter(c, new ArrayList<Question>());
        rv.setAdapter(questionAdapter);

        initializePaginator();

    }

    private void initializePaginator() {

        pullToLoadView.isLoadMoreEnabled(true);
        pullToLoadView.setPullCallback(new PullCallback() {
            @Override
            public void onLoadMore() {
                LoadData(nextPage);

            }

            @Override
            public void onRefresh() {

                questionList.clear();
                hasLoadedAll=false;
                LoadData(1);

            }

            @Override
            public boolean isLoading() {
                return isLoading();
            }

            @Override
            public boolean hasLoadedAllItems() {
                return hasLoadedAll;
            }
        });

        pullToLoadView.initLoad();
    }

    public void LoadData(final int page) {

        isLoading = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                for (int i=0; i<5; i++) {

                    questionList.add(new Question("question : "+String.valueOf(i)+ "int page : " +String.valueOf(page)));

                }

                pullToLoadView.setComplete();
                isLoading=false;
                nextPage=page+1;

            }
        },3000);
    }
}

