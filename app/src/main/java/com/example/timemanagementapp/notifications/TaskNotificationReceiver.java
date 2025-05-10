package com.example.timemanagementapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;

/**
 * BroadcastReceiver для обработки действий с уведомлениями о задачах
 */
public class TaskNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "TaskNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "Received intent with null action");
            return;
        }

        Log.d(TAG, "Received intent with action: " + action);

        if ("OPEN_TASK".equals(action)) {
            String taskId = intent.getStringExtra("taskId");
            if (taskId != null) {
                openTaskDetails(context, taskId);
            } else {
                Log.e(TAG, "Received OPEN_TASK intent with null taskId");
            }
        }
    }

    /**
     * Открывает экран деталей задачи
     * @param context Контекст приложения
     * @param taskId ID задачи
     */
    private void openTaskDetails(Context context, String taskId) {
        // В реальном приложении здесь нужно создать интент для открытия экрана задачи
        // Например, открыть MainActivity с параметром, указывающим, что нужно открыть редактирование задачи
        Intent intent = new Intent(context, getMainActivityClass(context));
        intent.putExtra("taskId", taskId);
        intent.putExtra("action", "OPEN_TASK_DETAILS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Получает класс MainActivity. Используется reflection для поиска класса
     * @param context Контекст приложения
     */
    private Class<?> getMainActivityClass(Context context) {
        try {
            return Class.forName("com.example.timemanagementapp.MainActivity");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MainActivity class not found", e);
            // Возвращаем любой activity из приложения
            String packageName = context.getPackageName();
            Intent mainIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (mainIntent != null) {
                String className = mainIntent.getComponent().getClassName();
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException ex) {
                    Log.e(TAG, "Launch activity not found", ex);
                }
            }
            throw new RuntimeException("Could not find main activity class");
        }
    }
} 