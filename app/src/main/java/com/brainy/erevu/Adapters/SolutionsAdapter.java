package com.brainy.erevu.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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

import com.brainy.erevu.R;
import com.brainy.erevu.activity.ViewUserProfileActivity;
import com.brainy.erevu.data.Answer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class SolutionsAdapter extends RecyclerView.Adapter<SolutionsAdapter.AnswersViewHolder>
{

    private List<Answer>  mAnswersList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseUsers, mDatabaseUsersAns, mDatabaseUsersAns2, mDatabaseVotes;
    public Boolean mProcessApproval = false;
    FirebaseAuth mAuth;

    public SolutionsAdapter(Context ctx, List<Answer> mAnswersList)
    {
        this.mAnswersList = mAnswersList;
        this.ctx = ctx;
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
        public RelativeTimeTextView post_date;
        public ImageView avator;
        public ImageView civ;
        public TextView  voteCount;
        public TextView  downVoteCount;
        public ImageView upVote, downVote;
        public Boolean mProcessApproval = false;
        RelativeLayout answer_rely;

        public AnswersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            post_answer = (TextView) itemView.findViewById(R.id.posted_answer);
            civ = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
            post_date.setTypeface(custom_font);
            voteCount = (TextView) itemView.findViewById(R.id.voteCount);
            downVoteCount = (TextView) itemView.findViewById(R.id.downVoteCount);
            upVote = (ImageView) itemView.findViewById(R.id.approve);
            downVote = (ImageView) itemView.findViewById(R.id.downvote);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Users_answers");
            mDatabaseVotes = FirebaseDatabase.getInstance().getReference().child("Answers_votes");

            avator = (ImageView) mView.findViewById(R.id.post_image);

            mDatabase.keepSynced(true);
            mDatabaseUsers.keepSynced(true);
            mDatabaseUsersAns.keepSynced(true);

        }
    }

    @Override
    public int getItemCount() {
        return mAnswersList.size();
    }

    @Override
    public void onBindViewHolder(final AnswersViewHolder holder, final int position) {

      /*  String quiz_key = getRef(position).getKey();*/

        final Answer c = mAnswersList.get(position);
        mAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabaseUsersAns = FirebaseDatabase.getInstance().getReference().child("Answers");
        mDatabaseUsersAns2 = FirebaseDatabase.getInstance().getReference().child("Users_answers");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseVotes = FirebaseDatabase.getInstance().getReference().child("Answers_votes");
        //final boolean mProcessApproval = false;
        mDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        final String answer_key = c.getPost_id();
        final String QuizKey = c.getQuestion_key();
        final String user_id = c.getSender_uid();

        holder.post_name.setText(c.getSender_name());
        holder.post_answer.setText(c.getPosted_answer());
        holder.post_date.setReferenceTime(Long.parseLong(String.valueOf(c.getPosted_date())));


        holder.avator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRead = new Intent(ctx, ViewUserProfileActivity.class);
                openRead.putExtra("user_id", user_id );
                ctx.startActivity(openRead);
            }
        });

        //COUNT NUMBER OF VOTE ON ANSWERS

        if (answer_key != null) {
            mDatabaseVotes
                    .child(QuizKey)
                    .child(answer_key)
                    .child("up_votes")
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

        //COUNT NUMBER OF DOWN VOTE ON ANSWERS

        if (answer_key != null) {
            mDatabaseVotes
                    .child(QuizKey)
                    .child(answer_key)
                    .child("down_votes")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            holder.downVoteCount.setText(dataSnapshot.getChildrenCount() + "");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }


       /* if (answer_key != null) {
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
        }*/

        holder.upVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if ((mAuth.getCurrentUser() != null))
                {

                    // custom dialog
                    final Dialog dialog = new Dialog(ctx);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.up_vote_dialog);
                    dialog.setCancelable(false);
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
                        public void onClick(final View v) {

                            dialog.dismiss();
                            mProcessApproval = true;

                           /* final ValueEventListener valueEventListener = mDatabase.child(QuizKey).child("Answers").addValueEventListener(new ValueEventListener() {*/
                            final ValueEventListener valueEventListener = mDatabaseVotes.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                                    if (mProcessApproval) {

                                        if (dataSnapshot.child(QuizKey).child(answer_key).child("up_votes").hasChild(mAuth.getCurrentUser().getUid())) {

                                            Toast.makeText(ctx, "You have already voted for this answer", Toast.LENGTH_LONG).show();
                                        /*Snackbar.make(v, "You have already voted for this answer ", Snackbar.LENGTH_SHORT).show();*/

                                            mDatabaseVotes.child(QuizKey).child(answer_key).child("up_votes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                    holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        } else {

                                          //  mDatabaseVotes.child(answer_key).child("up_votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");
                                            mDatabaseVotes.child(QuizKey).child(answer_key).child("up_votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");

                                            //PROFILE USER ANSWERS
                                           /* mDatabaseUsersAns.child(mAuth.getCurrentUser().getUid()).child(answer_key).child("votes").setValue("iVote");*/
                                           // mDatabaseUsersAns2.child(user_id).child(answer_key).child("votes").child("up_votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");

                                            // CHECK IF USER HAS VOTED AND ADD 10 POINTS TO THE USER WHO POSTED THE ANSWER.....................

                                            final int p = 10;

                                            mDatabaseUsersAns.child(QuizKey).child(answer_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                                                    final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                                    mDatabaseUsers.child(sender_uid).child("points_earned").runTransaction(new Transaction.Handler() {
                                                        @Override
                                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                                            if (mutableData.getValue() == null) {
                                                                mutableData.setValue(10);
                                                            } else {
                                                                int count = mutableData.getValue(Integer.class);
                                                                mutableData.setValue(count + 10);
                                                               // mDatabaseUsers.child(sender_uid).child("points_earned").setValue(user_points);
                                                            }
                                                            return Transaction.success(mutableData);
                                                        }

                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, boolean b, com.google.firebase.database.DataSnapshot dataSnapshot) {

                                                        }

                                                    });

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            mDatabaseVotes.child(QuizKey).child(answer_key).child("up_votes").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                    holder.voteCount.setText(dataSnapshot.getChildrenCount() + "");
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            mProcessApproval = false;
                                            Snackbar.make(v, "Vote was successful ", Snackbar.LENGTH_SHORT).show();

                                        }

                                        mProcessApproval = false;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    });

                } else
                {
                    Snackbar snackbar = Snackbar
                            .make(v, "You need to sign in first for you to vote for an answer", Snackbar.LENGTH_LONG)
                            .setAction("SIGN IN", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Snackbar snackbar1 = Snackbar.make(view, "Your vote has been reversed!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();
                                }
                            });

                    snackbar.show();
                }
            }
        });


        // DOWN VOTING AN ANSWER

        holder.downVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                mProcessApproval = true;
                if (mProcessApproval) {

                    if ((mAuth.getCurrentUser() != null)) {

                        // custom dialog
                        final Dialog dialog = new Dialog(ctx);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.ans_downvote_dialog);
                        dialog.setCancelable(false);
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
                            public void onClick(final View v) {
                                mProcessApproval = true;
                                if (mProcessApproval) {

                                    dialog.dismiss();
                                    mDatabaseVotes.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.child(QuizKey).child(answer_key).child("down_votes").hasChild(mAuth.getCurrentUser().getUid())) {

                                                //* mDatabaseQuestions.child(QuizKey).child("Answers").child(answer_key).child("votes").child(auth.getCurrentUser().getUid()).removeValue();*//*
                           /* Toast.makeText(ctx, "You have already down voted this answer",Toast.LENGTH_LONG).show();
                            Toast.makeText(ctx, "You need to have an account for you to vote",Toast.LENGTH_LONG).show();*/
                                                Snackbar snackbar = Snackbar
                                                        .make(view, "You have already down voted this answer", Snackbar.LENGTH_LONG)
                                                        .setAction("UNDO", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(final View view) {

                                                                mDatabaseVotes.child(QuizKey).child("down_votes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                                                Snackbar snackbar1 = Snackbar.make(view, "Your vote has been reversed!", Snackbar.LENGTH_SHORT);


                                                            }
                                                        });

                                                snackbar.show();


                                            } else {
                                                mDatabaseVotes.child(QuizKey).child(answer_key).child("down_votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");
                                               // mDatabaseUsersAns2.child(user_id).child(answer_key).child("votes").child("down_votes").child(mAuth.getCurrentUser().getUid()).setValue("iVote");

                                                // CHECK IF USER HAS DOWN VOTED AND DEDUCT 2 POINTS TO THE USER WHO POSTED THE ANSWER.....................

                                                final int p = 2;
                                                final int d = 1;

                                                mDatabaseUsersAns.child(QuizKey).child(answer_key).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                        final String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();

                                                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("points_earned").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {


                                                                    Long current_user_points = (Long) dataSnapshot.getValue();
                                                                    current_user_points = current_user_points - d;

                                                                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("points_earned").setValue(current_user_points);

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        mDatabaseUsers.child(sender_uid).child("points_earned").runTransaction(new Transaction.Handler() {
                                                            @Override
                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                if (mutableData.getValue() == null) {
                                                                    mutableData.setValue(-2);
                                                                } else {
                                                                    int count = mutableData.getValue(Integer.class);
                                                                    mutableData.setValue(count - 2);
                                                                    // mDatabaseUsers.child(sender_uid).child("points_earned").setValue(user_points);
                                                                }
                                                                return Transaction.success(mutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, boolean b, com.google.firebase.database.DataSnapshot dataSnapshot) {

                                                            }

                                                        });
                                                        mDatabaseVotes.child(QuizKey).child(answer_key).child("down_votes").addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                                holder.downVoteCount.setText(dataSnapshot.getChildrenCount() + "");
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
                                }
                            }

                        });

                    } else {
                        Snackbar snackbar = Snackbar
                                .make(view, "You need to sign in first for you to vote for an answer", Snackbar.LENGTH_LONG)
                                .setAction("SIGN IN", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Snackbar snackbar1 = Snackbar.make(view, "Your vote has been reversed!", Snackbar.LENGTH_SHORT);
                                        snackbar1.show();
                                    }
                                });

                        snackbar.show();
                    }

                }

            }
        });

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

