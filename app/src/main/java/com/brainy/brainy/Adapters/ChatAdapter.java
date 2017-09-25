package com.brainy.brainy.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.brainy.brainy.data.Answer;
import com.brainy.brainy.data.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>
{

    private List<Chat>  mChatList;
    Context ctx;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    public ChatAdapter(Context c, List<Chat> mChatList)
    {
        this.mChatList = mChatList;
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
        public TextView post_date;
        public ImageView civ;
        public RelativeLayout min_rely;
        public LinearLayout linearChat;
        public TextView viewCounter, answersCounter, favouritesCounter;

        RelativeLayout answer_rely;


        public ChatViewHolder(View itemView) {
            super(itemView);

            message = (TextView) itemView.findViewById(R.id.post_message);
            sender_name = (TextView) itemView.findViewById(R.id.post_name);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_date = (TextView) itemView.findViewById(R.id.post_date);
            linearChat = (LinearLayout) itemView.findViewById(R.id.main_lay);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");

        }

        public void setPosted_date(String posted_date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(posted_date);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx)
                    .load(sender_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.placeholder_image)
                    .into(civ, new Callback() {
                @Override
                public void onSuccess() {
                    Picasso.with(ctx)
                            .load(sender_image)
                            .into(civ);
                }

                @Override
                public void onError() {

                    Picasso.with(ctx)
                            .load(sender_image)
                            .placeholder(R.drawable.placeholder_image)
                            .into(civ);
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

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Discuss_forum").child(post_key);

        holder.sender_name.setText(c.getSender_name());
        holder.message.setText(c.getMessage());
        holder.post_date.setText(c.getPosted_date());

        String from_user_id = c.getSender_uid();

        if (from_user_id.equals(mAuth.getCurrentUser().getUid())) {

           holder.linearChat.setGravity(Gravity.RIGHT);

         /*  RelativeLayout.LayoutParams linearParams = new RelativeLayout.LayoutParams(
                   LinearLayout.LayoutParams.WRAP_CONTENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT

           );
            linearParams.gra = RelativeLayout.ALIGN_RIGHT;
           holder.linearChat.setLayoutParams(linearParams);*/

        } else {

            holder.linearChat.setGravity(Gravity.LEFT);
        }

        Picasso.with(ctx)
                .load(c.getSender_image())
                .placeholder(R.drawable.placeholder_image)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.civ, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(ctx)
                        .load(c.getSender_image())
                        .placeholder(R.drawable.placeholder_image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.civ);
            }
        });
    }

}

