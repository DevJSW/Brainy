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

import com.brainy.erevu.Pojos.Chat;
import com.brainy.erevu.Pojos.Group;
import com.brainy.erevu.R;
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

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>
{


    private List<Group>  mChatList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseGroupChatlist;
    FirebaseAuth mAuth;

    public GroupAdapter(Context ctx, List<Group> mChatList)
    {
        this.mChatList = mChatList;
        this.ctx = ctx;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item,parent, false);

        return new GroupViewHolder(v);

    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView group_name;
        public TextView message;
        public ImageView civ;

        RelativeLayout answer_rely;


        public GroupViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            message = (TextView) itemView.findViewById(R.id.post_message);
            group_name = (TextView) itemView.findViewById(R.id.group_name);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            mAuth = FirebaseAuth.getInstance();
            mDatabaseGroupChatlist = FirebaseDatabase.getInstance().getReference().child("Group_chatlist");

        }

    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {

        final Group c = mChatList.get(position);
        final String post_key = c.getPost_id();

        holder.group_name.setText(c.getGroup_name());
        String GroupId = c.getGroup_id();

        mDatabaseGroupChatlist.child(GroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.child("message").getValue().toString();
                holder.message.setText(message);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Glide.with(ctx)
                .load(c.getGroup_image()).asBitmap()
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

