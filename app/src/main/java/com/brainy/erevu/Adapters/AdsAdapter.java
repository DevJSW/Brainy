package com.brainy.erevu.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.data.Ad;
import com.brainy.erevu.data.Chat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.AdViewHolder>
{

    private List<Ad>  mAdsList;
    Context ctx;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    public AdsAdapter(Context ctx, List<Ad> mAdsList)
    {
        this.mAdsList = mAdsList;
        this.ctx = ctx;
    }

    @Override
    public AdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ads_item,parent, false);

        return new AdViewHolder(v);
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public TextView ad_name;
        public ImageView civ;

        RelativeLayout answer_rely;


        public AdViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            //civ = (ImageView) itemView.findViewById(R.id.ad_image);
            //civ = (TextView) itemView.findViewById(R.id.ad_name);
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");

        }

    }

    @Override
    public int getItemCount() {
        return mAdsList.size();
    }

    @Override
    public void onBindViewHolder(final AdViewHolder holder, int position) {

        final Ad c = mAdsList.get(position);
       /* final String post_key = c.getPost_id();
        String from_user_id = c.getSender_uid();


        holder.sender_name.setText(c.getSender_name());
        holder.message.setText(c.getMessage());
        */

        Glide.with(ctx)
                .load(c.getAd_image()).asBitmap()
                .placeholder(R.drawable.placeholder_image)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .centerCrop()
                .into(new BitmapImageViewTarget(holder.civ) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.civ.setImageDrawable(circularBitmapDrawable);
                    }
                });

    }

}

