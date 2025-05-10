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
import com.example.timemanagementapp.data.local.entity.Task;
import android.util.Log;

/**
 * Класс для отправки уведомлений о назначенных задачах
 */
public class TaskAssignmentNotifier {
    private static final String TAG = "TaskAssignmentNotifier";
    private static final String CHANNEL_ID = "task_assignment_channel";
    private static final int NOTIFICATION_ID_BASE = 2000; // Начало диапазона для ID уведомлений о назначенных задачах

    /**
     * Инициализирует канал уведомлений для API >= 26
     * @param context Контекст приложения
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Призначені завдання";
            String description = "Сповіщення про нові призначені вам завдання";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Отправляет уведомление о назначенной задаче
     * @param context Контекст приложения
     * @param task Задача, о которой нужно уведомить
     * @param assigneeId ID пользователя, которому назначена задача
     */
    public static void notifyTaskAssigned(Context context, Task task, String assigneeId) {
        if (task == null || assigneeId == null || assigneeId.isEmpty()) {
            Log.e(TAG, "Cannot notify with null task or assignee ID");
            return;
        }

        // Создаем интент для открытия задачи при нажатии на уведомление
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("taskId", task.getTaskId());
        intent.setAction("OPEN_TASK");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            task.getTaskId().hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Строим уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Вам призначено нове завдання")
            .setContentText(task.getTitle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        // Отправляем уведомление
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            int notificationId = NOTIFICATION_ID_BASE + task.getTaskId().hashCode() % 1000;
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "No permission to post notifications: " + e.getMessage());
        }
    }
} 