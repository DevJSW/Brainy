package com.brainy.brainy.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.brainy.R;
import com.brainy.brainy.data.Answer;
import com.brainy.brainy.data.Question;
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

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.AnswersViewHolder>
{

    private List<Answer>  mAnswersList;
    Context ctx;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    public AnswersAdapter(Context c, List<Answer> mAnswersList)
    {
        this.mAnswersList = mAnswersList;
    }

    @Override
    public AnswersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer3_item,parent, false);

        return new AnswersViewHolder(v);
    }

    public class AnswersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView post_answer;
        public TextView post_quiz_title;
        public TextView post_name;
        public TextView post_body;
        public TextView post_date;
        public ImageView civ;
        public TextView viewCounter, answersCounter, favouritesCounter;

        RelativeLayout answer_rely;


        public AnswersViewHolder(View itemView) {
            super(itemView);

            post_answer = (TextView) itemView.findViewById(R.id.posted_answer);
            post_quiz_title = (TextView) itemView.findViewById(R.id.post_quiz_title);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_body = (TextView) itemView.findViewById(R.id.post_quiz_body);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (TextView) itemView.findViewById(R.id.post_date);

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");

        }

        public void setPosted_date(String posted_date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(posted_date);
        }

        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setQuestion_body(String question_body) {

            TextView post_body = (TextView) mView.findViewById(R.id.post_quiz_body);
            post_body.setText(question_body);
        }

        public void setQuestion_title(String question_title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            post_title.setText(question_title);
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
        return mAnswersList.size();
    }

    @Override
    public void onBindViewHolder(final AnswersViewHolder holder, int position) {

        final Answer c = mAnswersList.get(position);
      /*  String quiz_key = getRef(position).getKey();*/


       /* holder.post_name.setText(c.getSender_name());*/
        holder.post_answer.setText(c.getPosted_answer());
        holder.post_date.setText(c.getPosted_date());
        holder.post_quiz_title.setText(c.getPosted_quiz_title());

        /*Picasso.with(ctx)
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
        });*/
    }

}

