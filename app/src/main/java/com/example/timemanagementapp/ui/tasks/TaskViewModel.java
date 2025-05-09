package com.example.timemanagementapp.ui.tasks;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.timemanagementapp.data.TaskRepository;
import com.example.timemanagementapp.data.local.entity.Task;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public void deleteAllTasks() {
        repository.deleteAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<Task> getTaskById(String taskId) {
        return repository.getTaskById(taskId);
    }

    public LiveData<List<Task>> getTasksForProject(String projectId) {
        return repository.getTasksForProject(projectId);
    }

    public LiveData<List<Task>> getActiveTasksForUser(String userId) {
        return repository.getActiveTasksForUser(userId);
    }
} 