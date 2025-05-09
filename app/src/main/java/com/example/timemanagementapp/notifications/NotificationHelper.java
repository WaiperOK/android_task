package com.example.timemanagementapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.MainActivity;
import android.util.Log;

public class NotificationHelper {
    private static final String CHANNEL_ID = "task_reminders_channel";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final String CHANNEL_DESC = "Notifications for task deadlines";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showReminderNotification(Context context, String taskId, String taskTitle, String taskContent) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("taskId", taskId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                taskId.hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(taskTitle)
                .setContentText(taskContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = taskId.hashCode();
        
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "Missing notification permission", e);
        }
    }
} 