package com.example.timemanagementapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.example.timemanagementapp.data.local.entity.Task;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(Task task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTasks(List<Task> tasks);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    LiveData<Task> getTaskById(String taskId);

    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    LiveData<List<Task>> getAllTasksSortedByDueDate();

    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY priority DESC, due_date ASC")
    LiveData<List<Task>> getTasksForProject(String projectId);

    @Query("SELECT * FROM tasks WHERE assignee_user_id = :userId AND status != 'done' ORDER BY due_date ASC")
    LiveData<List<Task>> getActiveTasksForUser(String userId);

    @Query("SELECT * FROM tasks ORDER BY priority DESC, due_date ASC")
    LiveData<List<Task>> getAllTasksSortedByPriority();
} 