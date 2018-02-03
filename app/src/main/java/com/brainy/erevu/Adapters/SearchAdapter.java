package com.brainy.erevu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.Pojos.Question;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.AnswersViewHolder>
{

    private List<Question>  mAnswersList;
    Context ctx;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    public SearchAdapter(Context ctx, List<Question> mAnswersList)
    {
        this.mAnswersList = mAnswersList;
        this.ctx = ctx;
    }

    @Override
    public AnswersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item,parent, false);

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
        public TextView  voteCount;
        public TextView viewCounter, answersCounter, favouritesCounter;

        RelativeLayout answer_rely;


        public AnswersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            post_answer = (TextView) itemView.findViewById(R.id.posted_answer);
            post_quiz_title = (TextView) itemView.findViewById(R.id.post_quiz_title);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_body = (TextView) itemView.findViewById(R.id.post_quiz_body);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (TextView) itemView.findViewById(R.id.post_date);
            voteCount = (TextView) itemView.findViewById(R.id.voteCount);
            mAuth= FirebaseAuth.getInstance();
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

        public void setPost_quiz_title(String post_quiz_title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            post_title.setText(post_quiz_title);
        }

        public void setSender_image(final Context ctx, final String sender_image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

            Glide.with(ctx)
                    .load(sender_image)
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(civ);
        }
    }

    @Override
    public int getItemCount() {
        return mAnswersList.size();
    }

    @Override
    public void onBindViewHolder(final AnswersViewHolder holder, int position) {

        final Question c = mAnswersList.get(position);
      /*  String quiz_key = getRef(position).getKey();*/

        holder.post_answer.setText(c.getQuestion_body());
        holder.post_quiz_title.setText(c.getQuestion_title());

        final String quiz_key = c.getPost_id();


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAuth.getCurrentUser() != null) {
                    mDatabase.child(quiz_key).child("views").child(mAuth.getCurrentUser().getUid()).setValue("iView");
                }
                Intent openRead = new Intent(ctx, ReadQuestionActivity.class);
                openRead.putExtra("question_id", quiz_key );
                ctx.startActivity(openRead);

            }
        });

    }

}

