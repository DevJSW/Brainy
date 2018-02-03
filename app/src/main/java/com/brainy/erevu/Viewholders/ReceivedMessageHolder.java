package com.brainy.erevu.Viewholders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Chat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 1/25/2018.
 */

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    public TextView sender_name;
    public TextView received_message;
    public TextView message2;
    public RelativeTimeTextView post_date;
    public ImageView civ;
    FirebaseAuth mAuth;
    Context mContext;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        received_message = (TextView) itemView.findViewById(R.id.post_message);
        sender_name = (TextView) itemView.findViewById(R.id.post_name);
        civ = (CircleImageView) itemView.findViewById(R.id.post_image);
        post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
        Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/Aller_Rg.ttf");
        post_date.setTypeface(custom_font);
        mAuth = FirebaseAuth.getInstance();

    }

    void bind(Chat message) {
        received_message.setText(message.getMessage());
        sender_name.setText(message.getSender_name());
        post_date.setReferenceTime(Long.parseLong(String.valueOf(message.getPosted_date())));

        Glide.with(mContext)
                .load(message.getSender_image()).asBitmap()
                .placeholder(R.drawable.placeholder_image)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .centerCrop()
                .into(new BitmapImageViewTarget(civ) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        civ.setImageDrawable(circularBitmapDrawable);
                    }
                });

    }
}

