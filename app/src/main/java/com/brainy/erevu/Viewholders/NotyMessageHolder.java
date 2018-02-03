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

import com.brainy.erevu.Pojos.Chat;
import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 1/25/2018.
 */

public class NotyMessageHolder extends RecyclerView.ViewHolder {
    public TextView received_message;
    FirebaseAuth mAuth;
    Context mContext;

    NotyMessageHolder(View itemView) {
        super(itemView);
        received_message = (TextView) itemView.findViewById(R.id.post_message);
        Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/Aller_Rg.ttf");
        mAuth = FirebaseAuth.getInstance();

    }

    void bind(Chat message) {
        received_message.setText(message.getMessage());

    }
}

