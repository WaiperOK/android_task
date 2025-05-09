package com.example.timemanagementapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.timemanagementapp.notifications.NotificationHelper;
import com.example.timemanagementapp.ui.calendar.CalendarFragment;
import com.example.timemanagementapp.ui.tasks.TaskEditFragment;
import com.example.timemanagementapp.ui.tasks.TaskListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Главное активити приложения.
 * 
 * Обратите внимание на следующие изменения в зависимостях:
 * 1. Библиотека календаря была обновлена с устаревшей com.github.kizitonwose:CalendarView:1.0.4
 *    на новую com.kizitonwose.calendar:view:2.3.0
 * 2. Обновились имена пакетов с com.kizitonwose.calendarview.* на com.kizitonwose.calendar.*
 * 3. Изменились некоторые классы и интерфейсы:
 *    - DayBinder теперь называется MonthDayBinder
 *    - Модели перенесены из пакета .model в пакет .core
 *    - Интерфейсы для биндеров перенесены из .ui в .view
 * 
 * Подробнее о миграции с версии 1.x на 2.x можно прочитать здесь: 
 * https://github.com/kizitonwose/Calendar/blob/main/docs/MigrationGuide.md
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация канала уведомлений
        NotificationHelper.createNotificationChannel(this);

        // Настройка нижней навигации
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_tasks) {
                selectedFragment = new TaskListFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Установка фрагмента по умолчанию при запуске
        if (savedInstanceState == null) {
            // Проверяем, был ли запуск из уведомления
            if (getIntent() != null && getIntent().hasExtra("taskId")) {
                String taskId = getIntent().getStringExtra("taskId");
                openTaskEditFragmentForNotification(taskId);
            } else {
                // Обычный запуск, открываем список задач
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new TaskListFragment())
                        .commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Важно обновить интент активити
        // Проверяем, был ли запуск из уведомления, если активити уже было создано
        if (intent != null && intent.hasExtra("taskId")) {
            String taskId = intent.getStringExtra("taskId");
            openTaskEditFragmentForNotification(taskId);
        }
    }

    private void openTaskEditFragmentForNotification(String taskId) {
        TaskEditFragment fragment = new TaskEditFragment();
        Bundle args = new Bundle();
        args.putString(TaskEditFragment.ARG_TASK_ID, taskId);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null) // Позволяет вернуться к предыдущему состоянию
                .commit();
    }
}