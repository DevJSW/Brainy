package com.brainy.erevu.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.brainy.erevu.R;
import com.brainy.erevu.activity.ChatroomActivity;
import com.brainy.erevu.Pojos.Chat;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>
{

    private List<Chat>  mChatList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseForumNotifications, mDatabaseChats, mDatabaseChatslist;
    //private RelativeLayout counter;
    FirebaseAuth mAuth;

    public ChatListAdapter(Context ctx, List<Chat> mChatList)
    {
        this.mChatList = mChatList;
        this.ctx = ctx;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatlist_item,parent, false);

        return new ChatViewHolder(v);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public TextView sender_name;
        public TextView message;
        public TextView unreadCounter;
        public RelativeTimeTextView post_date;
        public ImageView civ;
        public RelativeLayout min_rely, counter;
        public LinearLayout linearChat, liny;
        public TextView viewCounter, answersCounter, favouritesCounter;

        RelativeLayout answer_rely;


        public ChatViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            message = (TextView) itemView.findViewById(R.id.post_message);
            sender_name = (TextView) itemView.findViewById(R.id.post_name);
            unreadCounter = (TextView) itemView.findViewById(R.id.unreadCounter);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
            post_date.setTypeface(custom_font);
            linearChat = (LinearLayout) itemView.findViewById(R.id.main_lay);
            liny = (LinearLayout) itemView.findViewById(R.id.liny);
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Discuss_forum");
            mDatabaseForumNotifications = FirebaseDatabase.getInstance().getReference().child("Forum_notifications");
            mDatabaseChatslist = FirebaseDatabase.getInstance().getReference().child("Chats_list");
            mDatabaseChats = FirebaseDatabase.getInstance().getReference().child("Chats");
            counter = (RelativeLayout) itemView.findViewById(R.id.counter);

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
        final String user_id = c.getSender_uid();
        final String post_id = c.getPost_id();
        final String user_name = c.getSender_name();
        final String user_image = c.getSender_image();
        //String from_user_id = c.getSender_uid();

       /* mDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Discuss_forum")
                .child(post_key);*/

        holder.sender_name.setText(c.getSender_name());
        holder.message.setText(c.getMessage());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //MARK MESSAGE AS READ ON DB
                mDatabaseChatslist.child(mAuth.getCurrentUser().getUid()).child(user_id).child("read").setValue(true);

                Intent openActivity = new Intent(ctx, ChatroomActivity.class);
                openActivity.putExtra("user_id", user_id );
                openActivity.putExtra("user_name", user_name );
                openActivity.putExtra("user_image", user_image );
                //openActivity.putExtra("user_name", user_name );
                ctx.startActivity(openActivity);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog diaBox = AskOption();
                diaBox.show();
                return false;
            }

            private AlertDialog AskOption() {
                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(ctx)
                        //set message, title, and icon
                        .setMessage("Remove this chat")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code

                                mDatabaseChatslist.child(mAuth.getCurrentUser().getUid()).child(user_id).removeValue();
                                mDatabaseChats.child(mAuth.getCurrentUser().getUid()).child(user_id).child(post_id).removeValue();
                                dialog.dismiss();
                                Toast.makeText(ctx, "chat deleted!",Toast.LENGTH_SHORT).show();
                            }

                        })


                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .create();
                return myQuittingDialogBox;
            }
        });

        mDatabaseChatslist.child(mAuth.getCurrentUser().getUid()).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();

                if (read_status != null) {
                    if (read_status.equals(false)) {

                        holder.counter.setVisibility(View.VISIBLE);

                    } else {

                        holder.counter.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

