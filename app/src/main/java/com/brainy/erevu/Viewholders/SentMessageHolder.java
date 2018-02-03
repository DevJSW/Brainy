package com.brainy.erevu.Viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Chat;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 1/26/2018.
 */

public class SentMessageHolder extends RecyclerView.ViewHolder {
    public TextView sent_message;
    public TextView message2;
    public RelativeTimeTextView post_date;
    public ImageView civ;
    FirebaseAuth mAuth;
    Context mContext;

    SentMessageHolder(View itemView) {
        super(itemView);
       sent_message = (TextView) itemView.findViewById(R.id.post_message);
        civ = (CircleImageView) itemView.findViewById(R.id.post_image);
        post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
        Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/Aller_Rg.ttf");
        post_date.setTypeface(custom_font);
        mAuth = FirebaseAuth.getInstance();

    }

    void bind(Chat message) {
        sent_message.setText(message.getMessage());
        post_date.setReferenceTime(Long.parseLong(String.valueOf(message.getPosted_date())));

    }
}
