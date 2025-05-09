package com.example.timemanagementapp.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;

public class ReminderWorker extends Worker {
    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_TASK_TITLE = "task_title";
    public static final String KEY_TASK_CONTENT = "task_content";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Извлечение данных, переданных воркеру
        String taskId = getInputData().getString(KEY_TASK_ID);
        String taskTitle = getInputData().getString(KEY_TASK_TITLE);
        String taskContent = getInputData().getString(KEY_TASK_CONTENT);

        if (taskId != null && taskTitle != null && taskContent != null) {
            // Показ уведомления
            NotificationHelper.showReminderNotification(getApplicationContext(), taskId, taskTitle, taskContent);
            return Result.success();
        } else {
            return Result.failure();
        }
    }

    public static void scheduleReminder(Context context, long delayMillis, String taskId, String taskTitle, String taskContent) {
        // Создание входных данных
        Data inputData = new Data.Builder()
                .putString(KEY_TASK_ID, taskId)
                .putString(KEY_TASK_TITLE, taskTitle)
                .putString(KEY_TASK_CONTENT, taskContent)
                .build();

        // Создание OneTimeWorkRequest с вычисленной задержкой
        OneTimeWorkRequest reminderWorkRequest =
                new OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .addTag("reminder_" + taskId)
                        .build();

        // Постановка работы в очередь
        WorkManager.getInstance(context).enqueue(reminderWorkRequest);
    }

    public static void cancelReminder(Context context, String taskId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_" + taskId);
    }
} 