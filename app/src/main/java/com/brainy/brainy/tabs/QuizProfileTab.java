package com.brainy.brainy.tabs;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brainy.brainy.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuizProfileTab extends Fragment {


    public QuizProfileTab() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz_profile_tab, container, false);
    }

}
