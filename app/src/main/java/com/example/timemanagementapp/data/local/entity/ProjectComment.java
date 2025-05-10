package com.example.timemanagementapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timemanagementapp.data.local.converter.DateConverter;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "project_comments",
        foreignKeys = {
                @ForeignKey(entity = Project.class,
                        parentColumns = "project_id",
                        childColumns = "project_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.SET_NULL)
        })
@TypeConverters(DateConverter.class)
public class ProjectComment {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "comment_id")
    private String commentId;

    @NonNull
    @ColumnInfo(name = "project_id", index = true)
    private String projectId;

    @ColumnInfo(name = "user_id", index = true)
    private String userId;

    @NonNull
    @ColumnInfo(name = "text")
    private String text;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    // Конструктор
    public ProjectComment(@NonNull String projectId, String userId, @NonNull String text) {
        this.commentId = UUID.randomUUID().toString();
        this.projectId = projectId;
        this.userId = userId;
        this.text = text;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getters and Setters
    @NonNull
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(@NonNull String commentId) {
        this.commentId = commentId;
    }

    @NonNull
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(@NonNull String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    @NonNull
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 