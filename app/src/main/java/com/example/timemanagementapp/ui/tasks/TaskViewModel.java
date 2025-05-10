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
import com.example.timemanagementapp.data.local.entity.TaskComment;
import com.example.timemanagementapp.notifications.ReminderWorker;
import java.util.List;
import java.util.Date;
import android.util.Log;
import com.example.timemanagementapp.data.local.entity.CurrentUserManager;

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
                    allUsers.removeObserver(this);
                    addSampleUsers();
                } else {
                    // Для MVP: выбираем первого пользователя как текущего
                    if (CurrentUserManager.getCurrentUser() == null) { // Устанавливаем, только если еще не установлен
                        CurrentUserManager.setCurrentUser(users.get(0));
                    }
                    allUsers.removeObserver(this);
                }
            }
        });

        // Добавление тестовых проектов, если их нет
        allProjects.observeForever(new androidx.lifecycle.Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                if (projects == null || projects.isEmpty()) {
                    allProjects.removeObserver(this);
                    String defaultOwnerId = "alice_001"; // Используем ID Алисы
                    // Убираем проверку существования пользователя alice, так как метода getUserByIdSynchronous нет
                    // и для тестовых данных предполагаем, что alice_001 будет создана.
                    addSampleProjects(defaultOwnerId);
                } else {
                    allProjects.removeObserver(this);
                }
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

    private void addSampleProjects(String ownerId) {
        if (ownerId == null) {
            Log.e(TAG, "Cannot add sample projects: ownerId is null.");
            // Пытаемся получить текущего пользователя, если он уже установлен
            User currentUser = CurrentUserManager.getCurrentUser();
            if (currentUser != null) {
                ownerId = currentUser.getUserId();
            } else {
                 // Если текущий пользователь не установлен, используем ID по умолчанию или возвращаемся
                ownerId = "alice_001"; // Запасной вариант
                Log.w(TAG, "Defaulting to ownerId 'alice_001' for sample projects as current user is null.");
            }
        }

        Project project1 = new Project("Личный проект Альфа", ownerId);
        project1.setDescription("Задачи для личного развития и хобби.");
        project1.setColorHex("#FF5722"); // Оранжевый

        Project project2 = new Project("Работа Бета", ownerId);
        project2.setDescription("Все рабочие задачи и совещания.");
        project2.setColorHex("#2196F3"); // Синий

        Project project3 = new Project("Дом Гамма", ownerId);
        project3.setDescription("Задачи по дому и семейным делам.");
        project3.setColorHex("#4CAF50"); // Зеленый

        insertProject(project1);
        insertProject(project2);
        insertProject(project3);
        Log.d(TAG, "Added 3 sample projects to the database for owner: " + ownerId);
    }

    private void addSampleUsers() {
        // Используем конструктор User(email, name)
        User user1 = new User("alice@example.com", "Alice Wonderland");
        user1.setUserId("alice_001"); // Устанавливаем предопределенный ID для удобства

        User user2 = new User("bob@example.com", "Bob The Builder");
        user2.setUserId("bob_002");

        User user3 = new User("charlie@example.com", "Charlie Chaplin");
        user3.setUserId("charlie_003");

        User user4 = new User("diana@example.com", "Diana Prince");
        user4.setUserId("diana_004");

        User user5 = new User("edward@example.com", "Edward Nigma");
        user5.setUserId("edward_005");

        insertUser(user1);
        insertUser(user2);
        insertUser(user3);
        insertUser(user4);
        insertUser(user5);
        Log.d(TAG, "Added 5 sample users to the database.");
    }

    // Можно добавить updateUser, deleteUser если они нужны напрямую из ViewModel

    // Методы для работы с комментариями
    public void addComment(String taskId, String text) {
        User currentUser = CurrentUserManager.getCurrentUser();
        if (currentUser != null) {
            TaskComment comment = new TaskComment(taskId, currentUser.getUserId(), text);
            repository.insertComment(comment);
        }
    }

    public void updateComment(TaskComment comment) {
        comment.setUpdatedAt(new Date());
        repository.updateComment(comment);
    }

    public void deleteComment(TaskComment comment) {
        repository.deleteComment(comment);
    }

    public LiveData<List<TaskComment>> getCommentsForTask(String taskId) {
        return repository.getCommentsForTask(taskId);
    }

    public LiveData<TaskComment> getCommentById(String commentId) {
        return repository.getCommentById(commentId);
    }

    public LiveData<Integer> getCommentCountForTask(String taskId) {
        return repository.getCommentCountForTask(taskId);
    }

    public void deleteAllCommentsForTask(String taskId) {
        repository.deleteAllCommentsForTask(taskId);
    }
} 