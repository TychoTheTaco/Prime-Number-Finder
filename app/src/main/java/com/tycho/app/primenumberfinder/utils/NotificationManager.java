package com.tycho.app.primenumberfinder.utils;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.tycho.app.primenumberfinder.activities.MainActivity;

import easytasks.ITask;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

//TODO: REMOVE NOTIFICATIONS AND AUTO SAVE (maybe auto save only for tasks that take too long)
public class NotificationManager {

    public static final int TASK_TYPE_FIND_PRIMES = 0;
    public static final int TASK_TYPE_FIND_FACTORS = 1;
    public static final int TASK_TYPE_PRIME_FACTORIZATION = 2;
    public static final int TASK_TYPE_LCM = 3;
    public static final int TASK_TYPE_GCF = 4;

    private static int nextNotificationId = 0;

    public static synchronized void displayNotification(final Context context, final String channelId, final ITask task, final int taskType, final String contentText, final int smallIconDrawable){

        //Create notification
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("taskType", taskType);
        intent.putExtra("taskId", task.getId());
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, nextNotificationId, intent, FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIconDrawable)
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
