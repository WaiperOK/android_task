package com.example.timemanagementapp.ui.tasks;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.timemanagementapp.data.TaskRepository;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.data.local.entity.Project;
import com.example.timemanagementapp.data.local.entity.User;
import com.example.timemanagementapp.notifications.ReminderWorker;
import java.util.List;
import java.util.Date;
import android.util.Log;

public class TaskViewModel extends AndroidViewModel {
    private static final String TAG = "TaskViewModel";
    private TaskRepository repository;
    private MediatorLiveData<List<Task>> allTasksMediator = new MediatorLiveData<>();
    private LiveData<List<Task>> tasksSortedByDueDate;
    private LiveData<List<Task>> tasksSortedByPriority;
    private LiveData<List<Project>> allProjects;
    private LiveData<List<User>> allUsers;

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
        allProjects = repository.getAllProjects(); // Получаем все проекты
        allUsers = repository.getAllUsers(); // Получаем всех пользователей

        // Добавление тестовых пользователей, если их нет
        allUsers.observeForever(new androidx.lifecycle.Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users == null || users.isEmpty()) {
                    // Удаляем наблюдателя, чтобы избежать многократного добавления
                    allUsers.removeObserver(this);
                    addSampleUsers();
                }
                 // Можно также удалить наблюдателя здесь, если он нужен только для однократной проверки
                 // allUsers.removeObserver(this); 
            }
        });

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

    public void startTrackingTime(Task task) {
        if (task != null) {
            Log.d(TAG, "startTrackingTime for task: " + task.getTitle() + " ID: " + task.getTaskId());
            Task updatedTask = new Task(task); // Используем конструктор копирования
            updatedTask.setTimeTrackingStartTimeMillis(System.currentTimeMillis());
            updatedTask.setUpdatedAt(new Date()); // Обновляем время последнего изменения
            Log.d(TAG, "Task " + updatedTask.getTaskId() + " new startTime: " + updatedTask.getTimeTrackingStartTimeMillis());
            update(updatedTask);
        }
    }

    public void stopTrackingTime(Task task) {
        if (task != null && task.getTimeTrackingStartTimeMillis() != null) {
            Log.d(TAG, "stopTrackingTime for task: " + task.getTitle() + " ID: " + task.getTaskId());
            Task updatedTask = new Task(task); // Используем конструктор копирования
            
            long timeTrackedSession = System.currentTimeMillis() - updatedTask.getTimeTrackingStartTimeMillis(); // startTime берем из копии (такой же как в оригинале)
            long previousTimeSpent = updatedTask.getTimeSpentMillis();
            updatedTask.setTimeSpentMillis(previousTimeSpent + timeTrackedSession);
            updatedTask.setTimeTrackingStartTimeMillis(null);
            updatedTask.setUpdatedAt(new Date()); // Обновляем время последнего изменения

            Log.d(TAG, "Task " + updatedTask.getTaskId() + " new totalTimeSpent: " + updatedTask.getTimeSpentMillis() + ", startTime reset to null");
            update(updatedTask);
        }
    }

    // --- Методы для проектов ---
    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public void insertProject(Project project) {
        repository.insertProject(project);
    }

    public void updateProject(Project project) {
        repository.updateProject(project);
    }

    public void deleteProject(Project project) {
        // Подумать о каскадном удалении или обработке задач, связанных с проектом
        // В Task entity onDelete = ForeignKey.SET_NULL для project_id, так что projectId станет null
        repository.deleteProject(project);
    }

    // --- Методы для пользователей ---
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getUserById(String userId) {
        return repository.getUserById(userId);
    }

    public void insertUser(User user) {
        repository.insertUser(user);
    }

    private void addSampleUsers() {
        // Используем конструктор User(email, name)
        User user1 = new User("alice@example.com", "Alice Wonderland");
        user1.setUserId("alice_001"); // Устанавливаем предопределенный ID для удобства

        User user2 = new User("bob@example.com", "Bob The Builder");
        user2.setUserId("bob_002");

        User user3 = new User("charlie@example.com", "Charlie Chaplin");
        user3.setUserId("charlie_003");

        insertUser(user1);
        insertUser(user2);
        insertUser(user3);
        Log.d(TAG, "Added 3 sample users to the database.");
    }

    // Можно добавить updateUser, deleteUser если они нужны напрямую из ViewModel
} 