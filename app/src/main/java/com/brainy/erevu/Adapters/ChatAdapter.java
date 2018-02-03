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
import com.brainy.erevu.Pojos.Chat;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>
{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Chat>  mChatList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseForumNotifications;
    FirebaseAuth mAuth;

    public ChatAdapter(Context ctx, List<Chat> mChatList)
    {
        this.mChatList = mChatList;
        this.ctx = ctx;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_sent,parent, false);

        return new ChatViewHolder(v);

    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public TextView sender_name;
        public TextView message;
        public TextView message2;
        public RelativeTimeTextView post_date;
        public ImageView civ;
        public RelativeLayout min_rely;
        public LinearLayout linearChat, liny;
        public TextView viewCounter, answersCounter, favouritesCounter;

        RelativeLayout answer_rely;


        public ChatViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            message = (TextView) itemView.findViewById(R.id.post_message);
            sender_name = (TextView) itemView.findViewById(R.id.post_name);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
            post_date.setTypeface(custom_font);
            linearChat = (LinearLayout) itemView.findViewById(R.id.main_lay);
            liny = (LinearLayout) itemView.findViewById(R.id.liny);
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");
            mDatabaseForumNotifications = FirebaseDatabase.getInstance().getReference().child("Forum_notifications");

        }

    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {

        final Chat c = mChatList.get(position);
        final String post_key = c.getPost_id();
        String from_user_id = c.getSender_uid();

        if (c.getSender_name() != null)
        holder.sender_name.setText(c.getSender_name());

        holder.message.setText(c.getMessage());

        String current_user_id = mAuth.getCurrentUser().getUid();
        String sender_user_id = c.getSender_uid();


        if (c.getPosted_date() != null)
        holder.post_date.setReferenceTime(Long.parseLong(String.valueOf(c.getPosted_date())));

        Glide.with(ctx)
                .load(c.getSender_image()).asBitmap()
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

