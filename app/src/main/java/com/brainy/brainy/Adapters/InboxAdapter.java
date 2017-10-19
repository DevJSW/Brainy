package com.brainy.brainy.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.brainy.R;
import com.brainy.brainy.activity.DiscussForumActivity;
import com.brainy.brainy.activity.ReadQuestionActivity;
import com.brainy.brainy.data.Answer;
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

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.QuestionViewHolder>
{


    private List<Answer>  mQuestionList;
    Context ctx;

    private DatabaseReference mDatabase;

    FirebaseAuth mAuth;

    public InboxAdapter(Context ctx, List<Answer> mQuestionList)
    {
        this.mQuestionList = mQuestionList;
        this.ctx = ctx;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_item,parent, false);

        return new QuestionViewHolder(v);
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView posted_reason;
        public TextView post_name;
        public TextView post_answer;
        public TextView post_date;
        public ImageView civ;
        public TextView viewCounter, answersCounter, favouritesCounter;
        public DatabaseReference  mDatabase;
        public  FirebaseAuth mAuth;

        RelativeLayout answer_rely;


        public QuestionViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            posted_reason = (TextView) itemView.findViewById(R.id.posted_reason);
            civ = (CircleImageView) itemView.findViewById(R.id.sender_image);
            post_answer = (TextView) itemView.findViewById(R.id.posted_answer);
            post_name = (TextView) itemView.findViewById(R.id.post_name);
            post_date = (TextView) itemView.findViewById(R.id.post_date);

            mAuth= FirebaseAuth.getInstance();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);
            answer_rely = (RelativeLayout) mView.findViewById(R.id.anser_rely);

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

        final Answer c = mQuestionList.get(position);
        mAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_inbox").child(mAuth.getCurrentUser().getUid());
        mDatabase.keepSynced(true);

        final String answer_key = c.getPost_id();

        holder.posted_reason.setText(c.getPosted_reason());
        holder.post_answer.setText(c.getPosted_answer());
        holder.post_name.setText(c.getSender_name());
        holder.post_date.setText(c.getPosted_date());

        mDatabase.child(answer_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean read_status = (Boolean) dataSnapshot.child("read").getValue();

                if (read_status.equals(true) ) {

                    holder.post_answer.setTextColor(Color.parseColor("#ABABAB"));

                } else {

                    holder.post_answer.setTextColor(Color.BLACK);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(answer_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String quiz_key = dataSnapshot.child("question_key").getValue().toString();

                        //MARK MESSAGE AS READ ON DB
                        mDatabase.child(answer_key).child("read").setValue(true);

                        Intent openRead = new Intent(ctx, ReadQuestionActivity.class);
                        openRead.putExtra("question_id", quiz_key );
                        ctx.startActivity(openRead);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {


                // custom dialog
                final Dialog dialog = new Dialog(ctx);
                dialog.setContentView(R.layout.inbox_popup_dialog);
                dialog.setTitle("Inbox Options");

                LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                deleteLiny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog diaBox = AskOption();
                        diaBox.show();
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
                        .setTitle("Delete Alert!")
                        .setMessage("Are you sure you want to remove this message from your inbox!")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code

                                mDatabase.child(answer_key).removeValue();
                                dialog.dismiss();
                                Toast.makeText(ctx, "Message deleted!",Toast.LENGTH_SHORT).show();
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

        Picasso
                .with(ctx)
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

