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
import android.widget.LinearLayout;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 8/7/2017.
 */

public class SolutionsAdapter extends RecyclerView.Adapter<SolutionsAdapter.AnswersViewHolder>
{

    private List<Answer>  mAnswersList;
    Context ctx;

    private DatabaseReference mDatabase, mDatabaseUsers, mDatabaseUsersAns, mDatabaseUsersAns2, mDatabaseVotes, mDatabaseUsersInbox, mDatabaseNotifications;
    public Boolean mProcessApproval = false;
    FirebaseAuth mAuth;

    String username = null;
    String user_points = null;
    String posted_answer = null;

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
        public ImageView civ, answer_photo;
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
            answer_photo = (ImageView) itemView.findViewById(R.id.answerPhoto);
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
            mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Users_notifications");
            mDatabaseVotes = FirebaseDatabase.getInstance().getReference().child("Answers_votes");

            avator = (ImageView) mView.findViewById(R.id.post_image);

            mDatabase.keepSynced(true);
            mDatabaseUsers.keepSynced(true);
            mDatabaseUsersAns.keepSynced(true);
            mDatabaseNotifications.keepSynced(true);

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
        mDatabaseUsersInbox = FirebaseDatabase.getInstance().getReference().child("Users_inbox");
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
        final String ans_photo = c.getAnswer_photo();

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

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    if (user_id.equals(mAuth.getCurrentUser().getUid())) {

                        // custom dialog
                        final Dialog dialog = new Dialog(ctx);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.delete_popup_dialog);
                        dialog.setCancelable(true);
                        dialog.show();

                        LinearLayout delete = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDatabaseUsersAns.child(QuizKey).removeValue();
                                Toast.makeText(ctx, "Answer removed!", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });

                    }
                }
            }
        });

        mDatabaseUsersAns.child(QuizKey).child(answer_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("answer_photo")) {
                    holder.answer_photo.setVisibility(View.VISIBLE);
                } else {
                    holder.answer_photo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //GET USER INFO
        mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("name").getValue().toString();
                user_points = dataSnapshot.child("points_earned").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                    //CHECK IF USER IS THE ONE WHO POSTED THIS SOLUTION
                    if (mAuth.getCurrentUser().getUid().equals(user_id)){
                        Toast.makeText(ctx, "You cannot up vote your own answer", Toast.LENGTH_LONG).show();
                    } else {
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
                                                        final String posted_answer = (String) dataSnapshot.child("posted_answer").getValue();

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
                                                        int x = Integer.parseInt(user_points);
                                                        int y = 10;
                                                        int z = x + y;

                                                        // SEND MESSAGE TO QUESTION SENDER INBOX
                                                        final DatabaseReference newPost = mDatabaseNotifications.child(sender_uid).child(answer_key);
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("posted_reason", "Hi, You've been AWARDED 10 points because this answer was up voted! you now have "+ z+ " points.");
                                                        map.put("posted_answer", posted_answer);
                                                        map.put("sender_uid", mAuth.getCurrentUser().getUid());
                                                        map.put("sender_name", "Erevu");
                                                        map.put("inbox_type", "up_vote");
                                                        map.put("read", false);
                                                        map.put("question_key", QuizKey);
                                                        map.put("answer_key", answer_key);
                                                        map.put("posted_date", System.currentTimeMillis());
                                                        map.put("post_id", newPost.getKey());
                                                        newPost.setValue(map);

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

                    }

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
                                                        final String posted_answer = (String) dataSnapshot.child("posted_answer").getValue();

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

                                                        int x = Integer.parseInt(user_points);
                                                        int y = 2;
                                                        int z = x - y;

                                                        // SEND MESSAGE TO QUESTION SENDER INBOX
                                                        final DatabaseReference newPost = mDatabaseNotifications.child(sender_uid).child(answer_key);
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("posted_reason", "Hi, You've been DEDUCTED 2 points because this answer was down voted! you now have "+ z+ " points.");
                                                        map.put("posted_answer", posted_answer);
                                                        map.put("sender_uid", mAuth.getCurrentUser().getUid());
                                                        map.put("sender_name", "Erevu");
                                                        map.put("inbox_type", "down_vote");
                                                        map.put("read", false);
                                                        map.put("question_key", QuizKey);
                                                        map.put("answer_key", answer_key);
                                                        map.put("posted_date", System.currentTimeMillis());
                                                        map.put("post_id", newPost.getKey());
                                                        newPost.setValue(map);
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

