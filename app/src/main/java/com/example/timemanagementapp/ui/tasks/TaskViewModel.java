package com.example.timemanagementapp.ui.tasks;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.timemanagementapp.data.TaskRepository;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.notifications.ReminderWorker;
import java.util.List;
import java.util.Date;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private MediatorLiveData<List<Task>> allTasksMediator = new MediatorLiveData<>();
    private LiveData<List<Task>> tasksSortedByDueDate;
    private LiveData<List<Task>> tasksSortedByPriority;

    // Enum для режимов сортировки
    public enum SortMode {
        BY_DUE_DATE,
        BY_PRIORITY
    }

    private SortMode currentSortMode = SortMode.BY_DUE_DATE; // Режим по умолчанию

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        tasksSortedByDueDate = repository.getAllTasks(); // Изначально это getAllTasksSortedByDueDate()
        tasksSortedByPriority = repository.getAllTasksSortedByPriority();

        // Наблюдаем за источником данных по умолчанию
        setSortMode(SortMode.BY_DUE_DATE);
    }

    public void insert(Task task) {
        repository.insert(task);
        scheduleOrCancelReminder(task, false);
    }

    public void update(Task task) {
        repository.update(task);
        scheduleOrCancelReminder(task, false); // Перепланируем или отменяем при обновлении
    }

    public void delete(Task task) {
        repository.delete(task);
        scheduleOrCancelReminder(task, true); // Отменяем напоминание при удалении
    }

    private void scheduleOrCancelReminder(Task task, boolean isDeleting) {
        if (isDeleting) {
            ReminderWorker.cancelReminder(getApplication(), task.getTaskId());
            return;
        }

        // Сначала отменяем любое существующее напоминание для этой задачи
        ReminderWorker.cancelReminder(getApplication(), task.getTaskId());

        if (task.getDueDate() != null && task.getReminderOffsetMillisBeforeDueDate() != null) {
            long dueDateMillis = task.getDueDate().getTime();
            long reminderOffset = task.getReminderOffsetMillisBeforeDueDate();
            long reminderTimeMillis = dueDateMillis - reminderOffset;
            long currentTimeMillis = System.currentTimeMillis();

            if (reminderTimeMillis > currentTimeMillis) {
                long delayMillis = reminderTimeMillis - currentTimeMillis;
                String taskContent = task.getDescription() != null && !task.getDescription().isEmpty() ? 
                                     task.getDescription() : "Не забудьте о вашей задаче!";
                ReminderWorker.scheduleReminder(
                        getApplication(),
                        delayMillis,
                        task.getTaskId(),
                        task.getTitle(),
                        taskContent
                );
            }
        }
    }

    public void deleteAllTasks() {
        // При удалении всех задач, нужно пройтись по ним и отменить напоминания
        // Это потребует загрузки всех задач перед удалением, что может быть неоптимально
        // Либо WorkManager должен поддерживать отмену по общему тегу для всех напоминаний приложения, если это возможно
        // Пока оставим без массовой отмены напоминаний здесь, т.к. это редкая операция
        repository.deleteAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasksMediator;
    }

    public LiveData<Task> getTaskById(String taskId) {
        return repository.getTaskById(taskId);
    }

    public LiveData<List<Task>> getTasksForProject(String projectId) {
        // Если нужна сортировка и здесь, нужно будет добавить аналогичную логику с MediatorLiveData
        return repository.getTasksForProject(projectId);
    }

    public LiveData<List<Task>> getActiveTasksForUser(String userId) {
        // Аналогично для этого метода
        return repository.getActiveTasksForUser(userId);
    }

    public void setSortMode(SortMode sortMode) {
        if (currentSortMode == sortMode && allTasksMediator.getValue() != null) {
            // Если режим не изменился и данные уже есть, ничего не делаем
            // или можно принудительно обновить, если это необходимо
            return;
        }
        currentSortMode = sortMode;

        // Удаляем предыдущий источник, если он был
        if (sortMode == SortMode.BY_PRIORITY) {
            allTasksMediator.removeSource(tasksSortedByDueDate);
            allTasksMediator.addSource(tasksSortedByPriority, tasks -> allTasksMediator.setValue(tasks));
        } else { // BY_DUE_DATE или любой другой режим по умолчанию
            allTasksMediator.removeSource(tasksSortedByPriority);
            allTasksMediator.addSource(tasksSortedByDueDate, tasks -> allTasksMediator.setValue(tasks));
        }
    }

    public SortMode getCurrentSortMode() {
        return currentSortMode;
    }
} 