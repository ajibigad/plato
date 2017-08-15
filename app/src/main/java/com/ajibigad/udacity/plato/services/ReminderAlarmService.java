package com.ajibigad.udacity.plato.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ajibigad.udacity.plato.DetailsActivity;
import com.ajibigad.udacity.plato.R;
import com.ajibigad.udacity.plato.data.FavoriteMovieColumns;



/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ReminderAlarmService extends IntentService {
    private static final String TAG = ReminderAlarmService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 54;

    //This is a deep link intent, and needs the task stack
    public static PendingIntent getReminderPendingIntent(Context context, Uri movieUri) {
        Intent action = new Intent(context, ReminderAlarmService.class);
        action.setData(movieUri);
        return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public ReminderAlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Display a notification to view the task details
        Intent action = new Intent(this, DetailsActivity.class);
        Uri movieUri = intent.getData();
        action.setData(movieUri);
        PendingIntent operation = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Grab the task description
        Cursor cursor = getContentResolver().query(movieUri, null, null, null, null);
        String notificationMessage;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                notificationMessage = String.format(getString(R.string.notification_message), cursor.getString(cursor.getColumnIndex(FavoriteMovieColumns.TITLE)));
            } else return;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.reminder_title))
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_movie_black_24dp)
                .setContentIntent(operation)
                .setAutoCancel(true);
        inboxStyle.addLine(notificationMessage);
        Notification note = builder.setStyle(inboxStyle).build();

        manager.notify((int) (Math.random() * 100), note);
    }
}

