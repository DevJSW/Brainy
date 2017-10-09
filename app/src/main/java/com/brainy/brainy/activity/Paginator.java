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
import java.util.List;

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

    private int ITEMS_PER_PAGE = 5;

    QuestionAdapter questionAdapter;
    private final ArrayList<Question> questionList = new ArrayList<>();

    public Paginator(Context c, PullToLoadView pullToLoadView, RecyclerView mQuestionsList) {
        this.c=c;
        this.pullToLoadView=pullToLoadView;
        this.mQuestionsList=mQuestionsList;

        ITEMS_PER_PAGE = 10;

        RecyclerView rv = pullToLoadView.getRecyclerView();
        rv.setLayoutManager(new LinearLayoutManager(c, LinearLayoutManager.VERTICAL, false));

        questionAdapter = new QuestionAdapter(c, new ArrayList<Question>());
        mQuestionsList.setAdapter(questionAdapter);

        initializePaginator();

    }

    public void initializePaginator() {

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
                LoadData(0);

            }

            @Override
            public boolean isLoading() {
                return isLoading;
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

               /* for (Question question : getCurrentQuestion(page)) {

                    questionList.add(question);

                }*/
                pullToLoadView.setComplete();
                isLoading=false;
                nextPage=page+1;

            }
        },3000);
    }


    // CURRENT PAGE QUESTION LIST

   /* public List<Question> getCurrentQuestion(int currentPage) {

        int startItem = currentPage * ITEMS_PER_PAGE;
        List<Question> currentQuestion = new ArrayList<>();

        try {

        }
    }*/

}

