package com.brainy.erevu.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.brainy.erevu.Pojos.Answer;
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

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.AnswersViewHolder>
{

    private List<Answer>  mAnswersList;
    Context ctx;
    String postID = null;

    private DatabaseReference mDatabase, mDatabaseProfileAns, mDatabaseUsersAns,  mDatabaseVotes;
    FirebaseAuth mAuth;

    public AnswersAdapter(Context ctx, List<Answer> mAnswersList)
    {
        this.mAnswersList = mAnswersList;
        this.ctx = ctx;
    }

    @Override
    public AnswersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer3_item,parent, false);

        return new AnswersViewHolder(v);
    }

    public class AnswersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        String postID = null;
        public TextView post_answer;
        public TextView post_quiz_title;
        public TextView post_name;
        public TextView post_body;
        public RelativeTimeTextView post_date;
        public ImageView civ;
        public TextView  voteCount;
        public TextView  downVoteCount;
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
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            voteCount = (TextView) itemView.findViewById(R.id.voteCount);
            downVoteCount = (TextView) itemView.findViewById(R.id.downVoteCount);
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            mDatabaseVotes = FirebaseDatabase.getInstance().getReference().child("Answers_votes");
            if (mAuth.getCurrentUser() != null) {
                mDatabaseProfileAns = FirebaseDatabase.getInstance().getReference().child("Users_answers").child(mAuth.getCurrentUser().getUid());
                mDatabase.keepSynced(true);
                mDatabaseProfileAns.keepSynced(true);
            }

        }

        public void setPosted_date(String posted_date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
            post_date.setTypeface(custom_font);
            post_date.setReferenceTime(Long.parseLong(String.valueOf(posted_date)));
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
        return mAnswersList.size();
    }

    @Override
    public void onBindViewHolder(final AnswersViewHolder holder, int position) {

        final Answer c = mAnswersList.get(position);
      /*  String quiz_key = getRef(position).getKey();*/

        mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");
       /* holder.post_name.setText(c.getSender_name());*/
        holder.post_answer.setText(c.getPosted_answer());
        if (c.getPosted_date() != null)
        holder.post_date.setReferenceTime(Long.parseLong(String.valueOf(c.getPosted_date())));
        holder.post_quiz_title.setText(c.getPosted_quiz_title());

        final String answer_key = c.getPost_id();
        final String QuizKey = c.getQuestion_key();

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {


                // custom dialog
                final Dialog dialog = new Dialog(ctx);
                dialog.setContentView(R.layout.ans_popup_dialog);
                dialog.setTitle("Profile Options");

                LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                deleteLiny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog diaBox = AskOption();
                        diaBox.show();
                        dialog.dismiss();
                    }
                });

               /* LinearLayout editLiny = (LinearLayout) dialog.findViewById(R.id.editLiny);
                editLiny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent openRead = new Intent(ctx, EditQuestionActivity.class);
                        openRead.putExtra("question_id", QuizKey );
                        ctx.startActivity(openRead);
                        dialog.dismiss();
                    }
                });
*/
                // if button is clicked, close the custom dialog

                dialog.show();
                return false;
            }

            private AlertDialog AskOption()
            {

                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(ctx)
                        //set message, title, and icon
                        .setTitle("Remove Alert!")
                        .setMessage("If you remove this answer from your profile you will not be able to easily monitor it!")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code

                                mDatabaseProfileAns.child(mAuth.getCurrentUser().getUid()).child(QuizKey).removeValue();
                                dialog.dismiss();
                                Toast.makeText(ctx, "answer removed!",Toast.LENGTH_SHORT).show();
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

        //COUNT NUMBER OF UP-VOTE ON ANSWERS
        if (mAuth.getCurrentUser() != null) {
            if (QuizKey != null) {
                mDatabaseVotes
                        .child(QuizKey)
                        .child(answer_key)
                        .child("up_votes")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        }

        //COUNT NUMBER OF DOWN-VOTE ON ANSWERS
        if (mAuth.getCurrentUser() != null) {
            if (QuizKey != null) {
                mDatabaseVotes
                        .child(QuizKey)
                        .child(answer_key)
                        .child("down_votes")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                holder.downVoteCount.setText(dataSnapshot.getChildrenCount() + "");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        }

    }

}

