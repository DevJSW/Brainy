package com.brainy.brainy.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.brainy.R;
import com.brainy.brainy.activity.ReadQuestionActivity;
import com.brainy.brainy.data.Answer;
import com.brainy.brainy.data.brainy;
import com.firebase.client.DataSnapshot;
import com.google.firebase.auth.FirebaseAuth;
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

public class SolutionsAdapter extends RecyclerView.Adapter<SolutionsAdapter.AnswersViewHolder>
{

    private List<Answer>  mAnswersList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseUsers, mDatabaseUsersAns;
    FirebaseAuth mAuth;

    public SolutionsAdapter(Context c, List<Answer> mAnswersList)
    {
        this.mAnswersList = mAnswersList;
    }

    @Override
    public AnswersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer2_item,parent, false);

        return new AnswersViewHolder(v);
    }

    public class AnswersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView post_answer;
        public TextView post_name;
        public TextView post_date;
        public ImageView civ;
        public TextView  voteCount;
        public ImageView upVote, downVote;
        public Boolean mProcessApproval = false;
        RelativeLayout answer_rely;


        public AnswersViewHolder(View itemView) {
            super(itemView);

            post_answer = (TextView) itemView.findViewById(R.id.posted_answer);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (TextView) itemView.findViewById(R.id.post_date);
            voteCount = (TextView) itemView.findViewById(R.id.voteCount);
            upVote = (ImageView) itemView.findViewById(R.id.approve);
            downVote = (ImageView) itemView.findViewById(R.id.downvote);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Users_answers").child(mAuth.getCurrentUser().getUid());

            mDatabase.keepSynced(true);
            mDatabaseUsers.keepSynced(true);
            mDatabaseUsersAns.keepSynced(true);

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
        return mAnswersList.size();
    }

    @Override
    public void onBindViewHolder(final AnswersViewHolder holder, int position) {

      /*  String quiz_key = getRef(position).getKey();*/

        final Answer c = mAnswersList.get(position);
        mAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        final Boolean[] mProcessApproval = {false};
        mDatabase.keepSynced(true);

        final String answer_key = c.getPost_id();
        final String QuizKey = c.getQuestion_key();

        holder.post_name.setText(c.getSender_name());
        holder.post_answer.setText(c.getPosted_answer());
        holder.post_date.setText(c.getPosted_date());


        //COUNT NUMBER OF VOTE ON ANSWERS
        if (answer_key != null) {
            mDatabase
                    .child(QuizKey)
                    .child("Answers")
                    .child(answer_key)
                    .child("votes")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        holder.upVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((mAuth.getCurrentUser() != null))
                {

                   /* // custom dialog
                    final Dialog dialog = new Dialog(ctx);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.vote_dialog);
                    dialog.setCancelable(false);
                            /*//*dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*//**//*
                    dialog.show();

                    Button cancel = (Button) dialog.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    Button create = (Button) dialog.findViewById(R.id.create);
                    create.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();*/
                            mProcessApproval[0] = true;

                            mDatabase.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.child(answer_key).child("votes").hasChild(mAuth.getCurrentUser().getUid())) {

                                       /* Snackbar.make(ctx, "You have already voted ", Snackbar.LENGTH_SHORT).show();*/

                                        mDatabase.child(QuizKey).child("Answers").child(answer_key).child("votes").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                            @Override
                                            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {

                                        mDatabase.child(QuizKey).child("Answers").child(answer_key).child("votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");
                                        //PROFILE USER ANSWERS
                                        mDatabaseUsersAns.child(answer_key).child("votes").setValue("iVote");
                                        // CHECK IF USER HAS VOTED AND ADD 10 POINTS TO THE USER WHO POSTED THE ANSWER.....................
                                        mDatabase.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();
                                                mDatabaseUsers.child(sender_uid).child("points_earned").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                        Long user_points = (Long) dataSnapshot.getValue();
                                                        user_points = user_points + 10;

                                                        mDatabaseUsers.child(sender_uid).child("points_earned").setValue(user_points);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        mDatabase.child(QuizKey).child("Answers").child(answer_key).child("votes").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                     //   Toast.makeText(ctx, "Vote was successful",Toast.LENGTH_LONG).show();

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
/*
                        }

                    });*/

                } else
                {
                    Toast.makeText(ctx, "You need to have an account for you to vote",Toast.LENGTH_LONG).show();
                }
            }
        });


        // DOWN VOTING AN ANSWER

        holder.downVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProcessApproval[0] = true;


                mDatabase.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(answer_key).child("down_votes").hasChild(mAuth.getCurrentUser().getUid())) {

                            //* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*//*
                            Toast.makeText(ctx, "You have already down voted",Toast.LENGTH_LONG).show();

                            mProcessApproval[0] = false;

                        } else {
                            mDatabase.child(QuizKey).child("Answers").child(answer_key).child("down_votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");

                            // CHECK IF USER HAS DOWN VOTED AND DEDUCT 2 POINTS TO THE USER WHO POSTED THE ANSWER.....................

                            mDatabase.child(QuizKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                    final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                    mDatabaseUsers.child(sender_uid).child("points_earned").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                            Long user_points = (Long) dataSnapshot.getValue();
                                                    user_points = user_points - 2;

                                            mDatabaseUsers.child(sender_uid).child("points_earned").setValue(user_points);

                                            //ALSO DEDUCT 1 POINT FROM THIS USER.....

                                            Long current_user_points = (Long) dataSnapshot.getValue();
                                            current_user_points = current_user_points - 1;
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    mDatabase.child(QuizKey).child("Answers").child(answer_key).child("down_votes").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                            holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    Toast.makeText(ctx, "Down vote was successful", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            }
        });



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

