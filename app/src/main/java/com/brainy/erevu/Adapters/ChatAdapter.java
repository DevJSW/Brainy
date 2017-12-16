package com.brainy.erevu.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.erevu.R;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>
{

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
                .inflate(R.layout.chat_item,parent, false);

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

        public void setPosted_date(Long posted_date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            post_date.setReferenceTime(Long.parseLong(String.valueOf(posted_date)));
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

            Glide.with(ctx)
                    .load(sender_image).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(civ) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            civ.setImageDrawable(circularBitmapDrawable);
                        }
                    });

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

       /* mDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Discuss_forum")
                .child(post_key);*/

        holder.sender_name.setText(c.getSender_name());
        holder.message.setText(c.getMessage());

       /* if (from_user_id.equals(mAuth.getCurrentUser().getUid())) {

            holder.liny.setVisibility(View.GONE);
        } else {

            holder.min_rely.setVisibility(View.VISIBLE);
        }
*/
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

