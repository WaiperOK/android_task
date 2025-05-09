package com.example.timemanagementapp.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.timemanagementapp.data.local.AppDatabase;
import com.example.timemanagementapp.data.local.dao.TaskDao;
import com.example.timemanagementapp.data.local.entity.Task;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasksSortedByDueDate();
        executorService = Executors.newSingleThreadExecutor();
    }

    // --- DAO операции (выполняются в фоновом потоке) ---
    public void insert(Task task) {
        executorService.execute(() -> taskDao.insertTask(task));
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }

    public void deleteAllTasks() {
        executorService.execute(() -> taskDao.deleteAllTasks());
    }

    // --- Геттеры LiveData ---
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<Task> getTaskById(String taskId) {
        return taskDao.getTaskById(taskId);
    }

    public LiveData<List<Task>> getTasksForProject(String projectId) {
        return taskDao.getTasksForProject(projectId);
    }

    public LiveData<List<Task>> getActiveTasksForUser(String userId) {
        return taskDao.getActiveTasksForUser(userId);
    }

    // Новый метод для получения задач, отсортированных по приоритету
    public LiveData<List<Task>> getAllTasksSortedByPriority() {
        return taskDao.getAllTasksSortedByPriority();
    }
} 