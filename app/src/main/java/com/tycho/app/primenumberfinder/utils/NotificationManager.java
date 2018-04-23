package com.tycho.app.primenumberfinder.utils;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.MainActivity;

import easytasks.Task;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationManager {

    public static final int REQUEST_CODE_FIND_PRIMES = 0;
    public static final int REQUEST_CODE_FIND_FACTORS = 1;
    public static final int REQUEST_CODE_PRIME_FACTORIZATION = 2;

    private static int nextNotificationId = 0;

    public static synchronized void displayNotification(final Context context, final String channelId, final Task task, final int requestCode, final String contentText){

        //Create notification
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("taskType", requestCode);
        intent.putExtra("taskId", task.getId());
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.circle_white)
                .setContentTitle("Task Finished")
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //Register notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(channelId, "Default", android.app.NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Default notification channel.");
            final android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        final android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(nextNotificationId, builder.build());
        nextNotificationId++;

    }
}
