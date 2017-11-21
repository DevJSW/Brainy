package com.brainy.erevu.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.brainy.erevu.activity.DiscussForumActivity;
import com.brainy.erevu.activity.EditQuestionActivity;
import com.brainy.erevu.activity.ReadQuestionActivity;
import com.brainy.erevu.activity.ViewUserProfileActivity;
import com.brainy.erevu.data.Question;
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

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>
{


    private List<Question>  mQuestionList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseAnswers;
    private ImageView avator;

    FirebaseAuth mAuth;

    public QuestionAdapter(Context ctx, List<Question> mQuestionList)
    {
        this.mQuestionList = mQuestionList;
        this.ctx = ctx;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_row,parent, false);

        return new QuestionViewHolder(v);
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView post_title;
        public TextView post_name;
        public TextView post_body;
        public RelativeTimeTextView post_date;
        public ImageView avator;
        public ImageView civ;
        public TextView viewCounter, answersCounter, favouritesCounter;
        public DatabaseReference  mDatabase, mDatabaseProfileAns;
        public  FirebaseAuth mAuth;

        RelativeLayout answer_rely;


        public QuestionViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            post_title = (TextView) itemView.findViewById(R.id.post_quiz_title);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_body = (TextView) itemView.findViewById(R.id.post_quiz_body);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
            post_date.setTypeface(custom_font);

            mAuth= FirebaseAuth.getInstance();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);
            answer_rely = (RelativeLayout) mView.findViewById(R.id.anser_rely);
            avator = (ImageView) mView.findViewById(R.id.post_image);
            mDatabaseProfileAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");

        }

        public void setPosted_date(String posted_date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            post_date.setText(posted_date);
        }


        public void setSender_name(String sender_name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(sender_name);
        }

        public void setQuestion_body(String question_body) {
            if (question_body != null) {
                TextView post_body = (TextView) mView.findViewById(R.id.post_quiz_body);
                post_body.setText(question_body);
            } else {
                post_body.setVisibility(View.GONE);
            }
        }

        public void setQuestion_title(String question_title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_quiz_title);
            post_title.setText(question_title);
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
        return mQuestionList.size();
    }

    @Override
    public void onBindViewHolder(final QuestionViewHolder holder, int position) {

        final Question c = mQuestionList.get(position);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mAuth= FirebaseAuth.getInstance();
        final DatabaseReference mDatabaseProfileAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");
        mDatabaseAnswers = FirebaseDatabase.getInstance().getReference().child("Answers");
        mDatabase.keepSynced(true);
        mDatabaseAnswers.keepSynced(true);

        final String quiz_key = c.getPost_id();
        final String user_id = c.getSender_uid();

        holder.post_title.setText(c.getQuestion_title());
        if (c.getQuestion_body() != null) {
            holder.post_body.setText(c.getQuestion_body());
        } else {
            holder.post_body.setVisibility(View.GONE);
        }
        holder.post_name.setText(c.getSender_name());
        holder.post_date.setReferenceTime(Long.parseLong(String.valueOf(c.getPosted_date())));

        holder.avator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(ctx, ViewUserProfileActivity.class);
                openRead.putExtra("user_id", user_id );
                ctx.startActivity(openRead);
            }
        });

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

        /*holder.mView.setOnLongClickListener(new View.OnLongClickListener() {

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

                LinearLayout editLiny = (LinearLayout) dialog.findViewById(R.id.editLiny);
                editLiny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent openRead = new Intent(ctx, EditQuestionActivity.class);
                        openRead.putExtra("question_id", quiz_key );
                        ctx.startActivity(openRead);
                        dialog.dismiss();
                    }
                });

                // if button is clicked, close the custom dialog

                dialog.show();
                return false;
            }

            private AlertDialog AskOption()
            {

                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(ctx)
                        //set message, title, and icon
                        .setTitle("Remove Alert!")
                        .setMessage("If you remove this question from your profile you will not be able to edit or easly monitor it!")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code

                                 if (mAuth.getCurrentUser() != null)
                                mDatabaseProfileAns.child(mAuth.getCurrentUser().getUid()).child(quiz_key).removeValue();
                                dialog.dismiss();
                                Toast.makeText(ctx, "Message removed!",Toast.LENGTH_SHORT).show();
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
*/


        holder.answer_rely.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(ctx, DiscussForumActivity.class);
                openRead.putExtra("question_id", quiz_key );

            }
        });

        // count number of views in a views

        if (quiz_key != null) {
            mDatabase
                    .child(quiz_key)
                    .child("views")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            holder.viewCounter.setText(dataSnapshot.getChildrenCount() + "");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        // count number of answers
        if (quiz_key != null) {
            mDatabaseAnswers
                    .child(quiz_key)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    holder.answersCounter.setText(dataSnapshot.getChildrenCount() + "");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // count number of favourites
        if (quiz_key != null) {
            mDatabase
                    .child(quiz_key)
                    .child("favourite")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    holder.favouritesCounter.setText(dataSnapshot.getChildrenCount() + "");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


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

