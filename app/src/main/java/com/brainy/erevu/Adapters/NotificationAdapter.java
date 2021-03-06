package com.brainy.erevu.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.brainy.erevu.activity.ReadQuestionActivity;
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

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.QuestionViewHolder>
{


    private List<Answer>  mQuestionList;
    Context ctx;

    private DatabaseReference mDatabase;

    FirebaseAuth mAuth;

    public NotificationAdapter(Context ctx, List<Answer> mQuestionList)
    {
        this.mQuestionList = mQuestionList;
        this.ctx = ctx;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.noty_item,parent, false);

        return new QuestionViewHolder(v);
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView posted_reason;
        public TextView post_name;
        public TextView post_answer;
        public RelativeTimeTextView post_date;
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
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            Typeface custom_font = Typeface.createFromAsset(ctx.getAssets(), "fonts/Aller_Rg.ttf");
            post_date.setTypeface(custom_font);

            mAuth= FirebaseAuth.getInstance();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            viewCounter = (TextView) mView.findViewById(R.id.viewsCounter);
            answersCounter = (TextView) mView.findViewById(R.id.answersCounter);
            favouritesCounter = (TextView) mView.findViewById(R.id.favouriteCounter);
            answer_rely = (RelativeLayout) mView.findViewById(R.id.anser_rely);

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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_notifications").child(mAuth.getCurrentUser().getUid());
        mDatabase.keepSynced(true);

        final String answer_key = c.getPost_id();

        holder.posted_reason.setText(c.getPosted_reason());
        holder.post_answer.setText(c.getPosted_answer());
        holder.post_name.setText(c.getSender_name());
        holder.post_date.setReferenceTime(Long.parseLong(String.valueOf(c.getPosted_date())));

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
                dialog.setTitle("Notification Options");

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
                        .setMessage("Are you sure you want to delete this notification!")

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

        Glide.with(ctx)
                .load(c.getSender_image()).asBitmap()
                .placeholder(R.drawable.e2_icon)
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

