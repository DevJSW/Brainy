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
import com.brainy.brainy.data.Question;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>
{


    private List<Question>  mQuestionList;
    Context ctx;

    private DatabaseReference mDatabase;

    FirebaseAuth mAuth;

    public QuestionAdapter(Context c, List<Question> mQuestionList)
    {
        this.mQuestionList = mQuestionList;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_row,parent, false);

        return new QuestionViewHolder(v);
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView post_title;
        public TextView post_name;
        public TextView post_body;
        public TextView post_date;
        public ImageView civ;
        public TextView viewCounter, answersCounter, favouritesCounter;
        private DatabaseReference  mDatabase;

        RelativeLayout answer_rely;


        public QuestionViewHolder(View itemView) {
            super(itemView);

            post_title = (TextView) itemView.findViewById(R.id.post_quiz_title);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_body = (TextView) itemView.findViewById(R.id.post_quiz_body);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (TextView) itemView.findViewById(R.id.post_date);

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
           /* viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);
            answer_rely = (RelativeLayout) mView.findViewById(R.id.anser_rely);*/

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

            Picasso.with(ctx).load(sender_image).networkPolicy(NetworkPolicy.OFFLINE).into(civ, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx).load(sender_image).into(civ);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mQuestionList.size();
    }

    @Override
    public void onBindViewHolder(final QuestionViewHolder holder, int position) {

        final Question c = mQuestionList.get(position);
      /*  String quiz_key = getRef(position).getKey();*/
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabase.keepSynced(true);

        holder.post_title.setText(c.getQuestion_title());
        holder.post_body .setText(c.getQuestion_body());
        holder.post_name.setText(c.getSender_name());
        holder.post_date.setText(c.getPosted_date());


      /*  holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAuth.getCurrentUser() != null) {
                    mDatabase.child(quiz_key).child("views").child(mAuth.getCurrentUser().getUid()).setValue("iView");
                }
                Intent openRead = new Intent(ctx, ReadQuestionActivity.class);
                openRead.putExtra("question_id", quiz_key );

            }
        });

        holder.answer_rely.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(ctx, AnswersActivity.class);
                openRead.putExtra("question_id", quiz_key );

            }
        });

        // count number of views in a hashtag
        mDatabase.child(quiz_key).child("views").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                holder.viewCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of answers
        mDatabase.child(quiz_key).child("Answers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                holder.answersCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of favourites
        mDatabase.child(quiz_key).child("favourite").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                holder.favouritesCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


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

