package com.example.timemanagementapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.timemanagementapp.data.local.entity.TaskComment;
import java.util.List;

@Dao
public interface TaskCommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskComment comment);

    @Update
    void update(TaskComment comment);

    @Delete
    void delete(TaskComment comment);

    @Query("SELECT * FROM task_comments WHERE task_id = :taskId ORDER BY created_at DESC")
    LiveData<List<TaskComment>> getCommentsForTask(String taskId);

    @Query("SELECT * FROM task_comments WHERE comment_id = :commentId")
    LiveData<TaskComment> getCommentById(String commentId);

    @Query("SELECT COUNT(*) FROM task_comments WHERE task_id = :taskId")
    LiveData<Integer> getCommentCountForTask(String taskId);

    @Query("DELETE FROM task_comments WHERE task_id = :taskId")
    void deleteAllCommentsForTask(String taskId);
} 