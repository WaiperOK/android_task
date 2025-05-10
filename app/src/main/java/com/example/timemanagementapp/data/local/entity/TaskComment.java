package com.example.timemanagementapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.TypeConverters;
import com.example.timemanagementapp.data.local.converter.DateConverter;
import java.util.Date;
import java.util.UUID;
import androidx.annotation.NonNull;

@Entity(tableName = "task_comments",
        foreignKeys = {
                @ForeignKey(entity = Task.class,
                        parentColumns = "task_id",
                        childColumns = "task_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE)
        })
@TypeConverters(DateConverter.class)
public class TaskComment {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "comment_id")
    private String commentId;

    @NonNull
    @ColumnInfo(name = "task_id", index = true)
    private String taskId;

    @NonNull
    @ColumnInfo(name = "user_id", index = true)
    private String userId;

    @NonNull
    private String text;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public TaskComment(@NonNull String taskId, @NonNull String userId, @NonNull String text) {
        this.commentId = UUID.randomUUID().toString();
        this.taskId = taskId;
        this.userId = userId;
        this.text = text;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    // Getters and setters
    @NonNull
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(@NonNull String commentId) {
        this.commentId = commentId;
    }

    @NonNull
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(@NonNull String taskId) {
        this.taskId = taskId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(@NonNull String text) {
        this.text = text;
    }

    @NonNull
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 