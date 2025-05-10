package com.example.timemanagementapp.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.timemanagementapp.data.local.AppDatabase;
import com.example.timemanagementapp.data.local.dao.TaskDao;
import com.example.timemanagementapp.data.local.dao.ProjectDao;
import com.example.timemanagementapp.data.local.dao.UserDao;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.data.local.entity.Project;
import com.example.timemanagementapp.data.local.entity.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private TaskDao taskDao;
    private ProjectDao projectDao;
    private UserDao userDao;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Project>> allProjects;
    private LiveData<List<User>> allUsers;
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();
        projectDao = database.projectDao();
        userDao = database.userDao();
        allTasks = taskDao.getAllTasksSortedByDueDate();
        allProjects = projectDao.getAllProjects();
        allUsers = userDao.getAllUsers();
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

    // --- Project DAO операции ---
    public void insertProject(Project project) {
        executorService.execute(() -> projectDao.insertProject(project));
    }

    public void updateProject(Project project) {
        executorService.execute(() -> projectDao.updateProject(project));
    }

    public void deleteProject(Project project) {
        executorService.execute(() -> projectDao.deleteProject(project));
    }

    // --- User DAO операции (примеры, можно добавить по необходимости) ---
    public void insertUser(User user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public void updateUser(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    public void deleteUser(User user) {
        executorService.execute(() -> userDao.delete(user));
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

    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public LiveData<Project> getProjectById(String projectId) {
        return projectDao.getProjectById(projectId);
    }

    public LiveData<List<Project>> getProjectsByOwner(String userId) {
        return projectDao.getProjectsByOwner(userId);
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getUserById(String userId) {
        return userDao.getUserById(userId);
    }
} 