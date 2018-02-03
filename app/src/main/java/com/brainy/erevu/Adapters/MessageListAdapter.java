package com.brainy.erevu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainy.erevu.R;
import com.brainy.erevu.activity.ViewPhotoActivity;
import com.brainy.erevu.activity.ViewUserProfileActivity;
import com.brainy.erevu.Pojos.Chat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shephard on 1/25/2018.
 */

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_NOTIFICATION = 3;
    private static final int VIEW_TYPE_PHOTO_SENT = 4;
    private static final int VIEW_TYPE_PHOTO_RECEIVED = 5;

    private Context mContext;
    private List<Chat> mMessageList;
    public FirebaseAuth auth;

    public MessageListAdapter(Context context, List<Chat> messageList) {
        mContext = context;
        mMessageList = messageList;

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Chat message = mMessageList.get(position);
        auth = FirebaseAuth.getInstance();
        String current_user_id = auth.getCurrentUser().getUid();
        String sender_user_id = message.getSender_uid();
        String message_type = message.getMessage_type();

        if (sender_user_id != null) {
            if (message_type.equals("NOTIFICATION")){
                return VIEW_TYPE_MESSAGE_NOTIFICATION;
            } else if (message_type.equals("PHOTO")){
                if (sender_user_id.equalsIgnoreCase(current_user_id)) {
                    // If the current user is the sender of the message
                    return VIEW_TYPE_PHOTO_SENT;
                } else {
                    // If some other user sent the message
                    return VIEW_TYPE_PHOTO_RECEIVED;
                }
            } else {
                if (sender_user_id.equalsIgnoreCase(current_user_id)) {
                    // If the current user is the sender of the message
                    return VIEW_TYPE_MESSAGE_SENT;
                } else {
                    // If some other user sent the message
                    return VIEW_TYPE_MESSAGE_RECEIVED;
                }
            }

        } else {
            return VIEW_TYPE_MESSAGE_SENT;
        }
    }
    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else  if (viewType == VIEW_TYPE_MESSAGE_NOTIFICATION) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_noty_message, parent, false);
            return new NotyMessageHolder(view);
        } else  if (viewType == VIEW_TYPE_PHOTO_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo_sent, parent, false);
            return new SentPhotoHolder(view);
        } else  if (viewType == VIEW_TYPE_PHOTO_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo_received, parent, false);
            return new ReceivedPhotoHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Chat message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_NOTIFICATION:
                ((NotyMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_PHOTO_SENT:
                ((SentPhotoHolder) holder).bind(message);
                break;
            case VIEW_TYPE_PHOTO_RECEIVED:
                ((ReceivedPhotoHolder) holder).bind(message);
        }
    }

    public class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        public RelativeTimeTextView post_date;
        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.post_message);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
        }

        void bind(Chat message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            if (message.getPosted_date() != null)
            post_date.setReferenceTime(Long.parseLong(String.valueOf(message.getPosted_date())));
        }
    }

    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;
        public RelativeTimeTextView post_date;
        FirebaseAuth auth;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.post_message);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            profileImage = (CircleImageView) itemView.findViewById(R.id.post_image);
            auth = FirebaseAuth.getInstance();
        }

        void bind(Chat message) {
            messageText.setText(message.getMessage());
            final String user_id = message.getSender_uid();
            // Format the stored timestamp into a readable String using method.
            post_date.setReferenceTime(Long.parseLong(String.valueOf(message.getPosted_date())));

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openRead = new Intent(mContext, ViewUserProfileActivity.class);
                    openRead.putExtra("user_id", user_id );
                    mContext.startActivity(openRead);
                }
            });

            // Insert the profile image from the URL into the ImageView.
            Glide.with(mContext)
                    .load(message.getSender_image()).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(profileImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profileImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }
    }

    public class NotyMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        NotyMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.post_message);
        }

        void bind(Chat message) {
            messageText.setText(message.getMessage());
        }
    }

    public class ReceivedPhotoHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage, post_photo;
        public RelativeTimeTextView post_date;
        FirebaseAuth auth;

        ReceivedPhotoHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.post_message);
            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            profileImage = (CircleImageView) itemView.findViewById(R.id.post_image);
            post_photo = (ImageView) itemView.findViewById(R.id.post_photo);
            auth = FirebaseAuth.getInstance();
        }

        void bind(final Chat message) {
            messageText.setText(message.getMessage());
            final String user_id = message.getSender_uid();
            // Format the stored timestamp into a readable String using method.
            post_date.setReferenceTime(Long.parseLong(String.valueOf(message.getPosted_date())));

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openRead = new Intent(mContext, ViewUserProfileActivity.class);
                    openRead.putExtra("user_id", user_id );
                    mContext.startActivity(openRead);
                }
            });

            post_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (message.getGroup_id() != null) {
                        Intent openRead = new Intent(mContext, ViewPhotoActivity.class);
                        openRead.putExtra("group_id", message.getGroup_id());
                        openRead.putExtra("photo_key", message.getPost_id());
                        mContext.startActivity(openRead);
                    }
                }
            });

            // Insert the profile image from the URL into the ImageView.
            Glide.with(mContext)
                    .load(message.getSender_image()).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(profileImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profileImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            Glide.with(mContext)
                    .load(message.getPhoto()).asBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(profileImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            post_photo.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }
    }

    public class SentPhotoHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, post_photo;
        public RelativeTimeTextView post_date;
        FirebaseAuth auth;

        SentPhotoHolder(View itemView) {
            super(itemView);

            post_date = (RelativeTimeTextView) itemView.findViewById(R.id.post_date);
            post_photo = (ImageView) itemView.findViewById(R.id.post_photo);
            auth = FirebaseAuth.getInstance();
        }

        void bind(final Chat message) {
            final String user_id = message.getSender_uid();
            // Format the stored timestamp into a readable String using method.
            post_date.setReferenceTime(Long.parseLong(String.valueOf(message.getPosted_date())));

            post_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (message.getGroup_id() != null) {
                        Intent openRead = new Intent(mContext, ViewPhotoActivity.class);
                        openRead.putExtra("group_id", message.getGroup_id());
                        openRead.putExtra("photo_key", message.getPost_id());
                        mContext.startActivity(openRead);
                    }
                }
            });

            Glide.with(mContext)
                    .load(message.getPhoto())
                    .placeholder(R.drawable.placeholder_image)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(post_photo);

        }
    }
}
