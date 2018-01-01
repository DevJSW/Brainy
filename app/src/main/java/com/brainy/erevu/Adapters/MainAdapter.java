package com.brainy.erevu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.SingleLineTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.activity.DiscussForumActivity;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.activity.ViewUserProfileActivity;
import com.brainy.erevu.data.Ad;
import com.brainy.erevu.data.Question;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Shephard on 8/7/2017.
 */

/*
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{


    private ArrayList<Object> items;
    Context ctx;
    private final int VERTICAL = 1;
    private final int HORIZONTAL = 2;

    public MainAdapter(Context ctx, ArrayList<Object> items)
    {
        this.items = items;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view;
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case VERTICAL:
                view = inflater.inflate(R.layout.vertical, parent, false);
                holder = new VerticalViewHolder(view);
                break;
            case HORIZONTAL:
                view = inflater.inflate(R.layout.horizontal, parent, false);
                holder = new HorizontalViewHolder(view);
                break;

            default:
                view = inflater.inflate(R.layout.horizontal, parent, false);
                holder = new HorizontalViewHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VERTICAL)
            verticalView((VerticalViewHolder) holder);
        else if (holder.getItemViewType() == HORIZONTAL)
            horizontalView((HorizontalViewHolder) holder);

    }

  */
/*  private void verticalView(VerticalViewHolder holder) {

        QuestionAdapter adapter1 = new QuestionAdapter(getVerticalData());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        holder.recyclerView.setAdapter(adapter1);
    }

    private void horizontalView(HorizontalViewHolder holder) {

        //AdsAdapter adapter = new AdsAdapter(getHorizontalData());
        QuestionAdapter adapter1 = new QuestionAdapter(getVerticalData());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter1);
    }*//*


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Question)
            return VERTICAL;
        if (items.get(position) instanceof Question)
            return HORIZONTAL;
        return -1;
    }

    public class HorizontalViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;

        HorizontalViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.Ads_list);
        }

    }

    public class VerticalViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;

        VerticalViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.Questions_list);
        }

    }


}
*/

