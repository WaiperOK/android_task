package com.example.timemanagementapp.ui.tasks;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.timemanagementapp.data.TaskRepository;
import com.example.timemanagementapp.data.local.entity.Task;
import java.util.List;

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