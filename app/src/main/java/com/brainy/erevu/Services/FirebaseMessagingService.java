package com.brainy.erevu.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.brainy.erevu.R;
import com.google.firebase.messaging.RemoteMessage;


/**
 * Created by Shephard on 9/24/2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String quiz_post_id = remoteMessage.getData().get("quiz_post_id");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.brainy_icon)
                        .setContentTitle(notification_title)
                        .setContentText(notification_body);

        Intent intent = new Intent(click_action);
        intent.putExtra("question_id", quiz_post_id );
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(pendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

}
