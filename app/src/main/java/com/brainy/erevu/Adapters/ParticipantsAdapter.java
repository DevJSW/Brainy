package com.brainy.erevu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.Pojos.Users;
import com.brainy.erevu.R;
import com.brainy.erevu.activity.ViewUserProfileActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.AnswersViewHolder>
{

    private List<Users>  mAnswersList;
    Context ctx;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    public ParticipantsAdapter(Context ctx, List<Users> mAnswersList)
    {
        this.mAnswersList = mAnswersList;
        this.ctx = ctx;
    }

    public void filterList(List<Users> filterdNames) {
        this.mAnswersList = filterdNames;
        notifyDataSetChanged();
    }

    @Override
    public AnswersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_grid,parent, false);

        return new AnswersViewHolder(v);
    }

    public class AnswersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView post_name,post_username;
        public ImageView civ;

        public AnswersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            civ = (CircleImageView) itemView.findViewById(R.id.user_image);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_username = (TextView) itemView.findViewById(R.id.post_username);
            mAuth= FirebaseAuth.getInstance();

        }

    }

    @Override
    public int getItemCount() {
        return mAnswersList.size();
    }

    @Override
    public void onBindViewHolder(final AnswersViewHolder holder, int position) {

        final Users c = mAnswersList.get(position);
        final String user_id = c.getUid();

        holder.post_name.setText(c.getName());
        holder.post_username.setText(c.getUsername());


        /*Glide.with(ctx)
                .load(c.getUser_image())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.civ);*/

        Picasso.with(ctx)
                .load(c.getUser_image())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.civ);

    }

}

