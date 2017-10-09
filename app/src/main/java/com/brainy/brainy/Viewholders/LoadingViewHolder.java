package com.brainy.brainy.Viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.brainy.brainy.R;

/**
 * Created by Shephard on 10/1/2017.
 */

public class LoadingViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;

    public LoadingViewHolder(View view) {
        super(view);
       // progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
    }
}


