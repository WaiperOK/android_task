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
import com.example.timemanagementapp.notifications.TaskAssignmentNotifier;
import java.util.List;
import java.util.Date;
import android.util.Log;
import com.example.timemanagementapp.data.local.entity.CurrentUserManager;
import java.util.Calendar;

public class TaskViewModel extends AndroidViewModel {
    private static final String TAG = "TaskViewModel";
    private TaskRepository repository;
    private MediatorLiveData<List<Task>> allTasksMediator = new MediatorLiveData<>();
    private LiveData<List<Task>> tasksSortedByDueDate;
    private LiveData<List<Task>> tasksSortedByPriority;
    private LiveData<List<Project>> allProjects;
    private LiveData<List<User>> allUsers;
    private LiveData<List<Task>> tasksAssignedToMe;

    // Enum для режимов сортировки
    public enum SortMode {
        BY_DUE_DATE,
        BY_PRIORITY,
        ASSIGNED_TO_ME
    }

    private SortMode currentSortMode = SortMode.BY_DUE_DATE; // Режим по умолчанию

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        
        // Инициализируем канал уведомлений при создании ViewModel
        TaskAssignmentNotifier.createNotificationChannel(application);
        tasksSortedByDueDate = repository.getAllTasks(); // Изначально это getAllTasksSortedByDueDate()
        tasksSortedByPriority = repository.getAllTasksSortedByPriority();
        
        // Получаем задачи, назначенные текущему пользователю
        User currentUser = CurrentUserManager.getCurrentUser();
        if (currentUser != null) {
            tasksAssignedToMe = repository.getActiveTasksForUser(currentUser.getUserId());
        } else {
            // Если текущий пользователь не установлен, используем пустой список
            tasksAssignedToMe = new MediatorLiveData<>();
        }
        
        allProjects = repository.getAllProjects(); // Получаем все проекты
        allUsers = repository.getAllUsers(); // Получаем всех пользователей

        // Обновляем имена проектов на украинский язык
        updateProjectNamesToUkrainian();

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
                    // Добавляем тестовые задачи после создания проектов
                    addSampleTasks(defaultOwnerId);
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
        
        // Если задача назначена другому пользователю (не создателю), отправляем уведомление
        checkAndNotifyAssignment(task);
    }

    public void update(Task task) {
        // Проверяем, изменился ли назначенный пользователь
        if (task.getTaskId() != null) {
            Task oldTask = repository.getTaskById(task.getTaskId()).getValue();
            if (oldTask != null && !stringEquals(oldTask.getAssigneeUserId(), task.getAssigneeUserId())) {
                // Если назначенный пользователь изменился, отправляем уведомление
                checkAndNotifyAssignment(task);
            }
        }
        
        repository.update(task);
        scheduleOrCancelReminder(task, false); // Перепланируем или отменяем при обновлении
    }

    // Вспомогательный метод для сравнения строк (учитывает null)
    private boolean stringEquals(String s1, String s2) {
        if (s1 == null) return s2 == null;
        return s1.equals(s2);
    }
    
    /**
     * Проверяет и отправляет уведомление о назначении задачи, если нужно
     */
    private void checkAndNotifyAssignment(Task task) {
        String assigneeId = task.getAssigneeUserId();
        String currentUserId = CurrentUserManager.getCurrentUserId();
        
        // Отправляем уведомление только если назначен другой пользователь (не создатель)
        if (assigneeId != null && !assigneeId.isEmpty() && !assigneeId.equals(currentUserId) && !assigneeId.equals(task.getCreatorUserId())) {
            TaskAssignmentNotifier.notifyTaskAssigned(getApplication(), task, assigneeId);
        }
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

        // Удаляем предыдущий источник
        allTasksMediator.removeSource(tasksSortedByDueDate);
        allTasksMediator.removeSource(tasksSortedByPriority);
        if (tasksAssignedToMe != null) {
            allTasksMediator.removeSource(tasksAssignedToMe);
        }

        // Добавляем новый источник в зависимости от режима сортировки
        if (sortMode == SortMode.BY_PRIORITY) {
            allTasksMediator.addSource(tasksSortedByPriority, tasks -> allTasksMediator.setValue(tasks));
        } else if (sortMode == SortMode.ASSIGNED_TO_ME) {
            if (tasksAssignedToMe != null) {
                allTasksMediator.addSource(tasksAssignedToMe, tasks -> allTasksMediator.setValue(tasks));
            } else {
                // Если tasksAssignedToMe не инициализирован (например, текущий пользователь не установлен)
                allTasksMediator.setValue(null);
            }
        } else { // BY_DUE_DATE или любой другой режим по умолчанию
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
            Log.e(TAG, "Cannot add sample projects: ownerId is null. Attempting to use current user or default.");
            User currentUser = CurrentUserManager.getCurrentUser();
            if (currentUser != null) {
                ownerId = currentUser.getUserId();
            } else {
                ownerId = "alice_001"; // Запасной вариант
                Log.w(TAG, "Defaulting to ownerId '" + ownerId + "' for sample projects as current user is null and ownerId was null.");
            }
        }

        Project project1 = new Project("Особистий проект", ownerId);
        project1.setDescription("Особисті справи та хобі");
        project1.setColorHex("#FF5722"); // Оранжевый

        Project project2 = new Project("Робота", ownerId);
        project2.setDescription("Робочі завдання та проекти");
        project2.setColorHex("#2196F3"); // Синий

        Project project3 = new Project("Дім", ownerId);
        project3.setDescription("Домашні справи та сімейні плани");
        project3.setColorHex("#4CAF50"); // Зеленый

        insertProject(project1);
        insertProject(project2);
        insertProject(project3);
        Log.d(TAG, "Added 3 sample projects to the database for owner: " + ownerId);
    }

    private void addSampleUsers() {
        User user1 = new User("alice@example.com", "Користувач");
        user1.setUserId("alice_001");
        insertUser(user1);

        User user2 = new User("bob@example.com", "Bob The Builder");
        user2.setUserId("bob_002");

        User user3 = new User("charlie@example.com", "Charlie Chaplin");
        user3.setUserId("charlie_003");

        User user4 = new User("diana@example.com", "Diana Prince");
        user4.setUserId("diana_004");

        User user5 = new User("edward@example.com", "Edward Nigma");
        user5.setUserId("edward_005");

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

    private void addSampleTasks(String ownerId) {
        if (ownerId == null) {
            Log.e(TAG, "Cannot add sample tasks: ownerId is null");
            return;
        }

        // Получаем проекты по ownerId из базы данных в синхронном режиме
        Calendar calendar = Calendar.getInstance();
        
        // Задача 1: В проекте "Робота"
        Task task1 = new Task("Підготувати звіт", ownerId);
        task1.setDescription("Зібрати дані та підготувати щомісячний звіт для керівництва");
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        task1.setDueDate(calendar.getTime());
        task1.setPriority(3); // Высокий
        task1.setAssigneeUserId("alice_001");
        task1.setStatus("in_progress");
        // task1.setProjectId будет установлено в обзервере проектов        
        insert(task1);

        // Задача 2: В проекте "Особистий проект"
        Task task2 = new Task("Організувати зустріч", ownerId);
        task2.setDescription("Запланувати і провести зустріч команди для обговорення нових завдань");
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        task2.setDueDate(calendar.getTime());
        task2.setPriority(2); // Средний
        task2.setAssigneeUserId("bob_002");
        task2.setStatus("todo");
        // task2.setProjectId будет установлено в обзервере проектов
        insert(task2);

        // Задача 3: В проекте "Дім"
        Task task3 = new Task("Вивчити новий фреймворк", ownerId);
        task3.setDescription("Пройти онлайн-курс по новому фреймворку і зробити тестовий проект");
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        task3.setDueDate(calendar.getTime());
        task3.setPriority(1); // Низкий
        task3.setAssigneeUserId("charlie_003");
        task3.setStatus("todo");
        // task3.setProjectId будет установлено в обзервере проектов
        insert(task3);

        // Задача 4: Без проекта
        Task task4 = new Task("Особиста задача", ownerId);
        task4.setDescription("Задача без прив'язки до проекту");
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        task4.setDueDate(calendar.getTime());
        task4.setPriority(2); // Средний
        task4.setAssigneeUserId("alice_001");
        task4.setStatus("on_hold"); // Отложена
        insert(task4);

        Log.d(TAG, "Added 4 sample tasks to the database for owner: " + ownerId);
        
        // Устанавливаем projectId для задач после того, как проекты созданы
        // Это будет работать асинхронно через LiveData, пока просто добавим задачи
        allProjects.observeForever(new androidx.lifecycle.Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                if (projects != null && !projects.isEmpty()) {
                    allProjects.removeObserver(this);
                    
                    try {
                        // Найдем проекты по названиям
                        String robotaProjectId = null;
                        String personalProjectId = null;
                        String domProjectId = null;
                        
                        for (Project project : projects) {
                            if ("Робота".equals(project.getName())) {
                                robotaProjectId = project.getProjectId();
                            } else if ("Особистий проект".equals(project.getName())) {
                                personalProjectId = project.getProjectId();
                            } else if ("Дім".equals(project.getName())) {
                                domProjectId = project.getProjectId();
                            }
                        }
                        
                        // Обновим задачи с projectId
                        if (robotaProjectId != null) {
                            task1.setProjectId(robotaProjectId);
                            update(task1);
                        }
                        
                        if (personalProjectId != null) {
                            task2.setProjectId(personalProjectId);
                            update(task2);
                        }
                        
                        if (domProjectId != null) {
                            task3.setProjectId(domProjectId);
                            update(task3);
                        }
                        
                        Log.d(TAG, "Updated sample tasks with project IDs");
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating sample tasks with project IDs: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void updateProjectNamesToUkrainian() {
        allProjects.observeForever(new androidx.lifecycle.Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                if (projects != null && !projects.isEmpty()) {
                    allProjects.removeObserver(this);
                    
                    for (Project project : projects) {
                        boolean needUpdate = false;
                        
                        // Обновляем названия и описания проектов на украинском языке
                        if ("Дом Гамма".equals(project.getName())) {
                            project.setName("Дім");
                            project.setDescription("Домашні справи та сімейні плани");
                            project.setColorHex("#4CAF50"); // Зеленый
                            needUpdate = true;
                        } else if ("Личный проект Альфа".equals(project.getName())) {
                            project.setName("Особистий проект");
                            project.setDescription("Особисті справи та хобі");
                            project.setColorHex("#FF5722"); // Оранжевый
                            needUpdate = true;
                        } else if ("Работа Бета".equals(project.getName())) {
                            project.setName("Робота");
                            project.setDescription("Робочі завдання та проекти");
                            project.setColorHex("#2196F3"); // Синий
                            needUpdate = true;
                        }
                        
                        // Если название было изменено, обновляем проект в БД
                        if (needUpdate) {
                            updateProject(project);
                        }
                    }
                    
                    Log.d(TAG, "Project names updated to Ukrainian");
                }
            }
        });
    }
} 