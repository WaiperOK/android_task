package com.example.timemanagementapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.example.timemanagementapp.data.local.converter.DateConverter;

import java.util.Date;

@Entity(tableName = "project_collaborators",
        primaryKeys = {"project_id", "user_id"},
        foreignKeys = {
                @ForeignKey(entity = Project.class,
                        parentColumns = "project_id",
                        childColumns = "project_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"project_id"}),
                @Index(value = {"user_id"})
        })
@TypeConverters(DateConverter.class)
public class ProjectCollaborator {
    @NonNull
    @ColumnInfo(name = "project_id")
    private String projectId;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "role")
    private String role; // owner, editor, viewer

    @NonNull
    @ColumnInfo(name = "joined_at")
    private Date joinedAt;

    @ColumnInfo(name = "last_accessed_at")
    private Date lastAccessedAt;

    // Конструктор
    public ProjectCollaborator(@NonNull String projectId, @NonNull String userId, @NonNull String role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = new Date();
    }

    // Getters и Setters
    @NonNull
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(@NonNull String projectId) {
        this.projectId = projectId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getRole() {
        return role;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    @NonNull
    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(@NonNull Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Date getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(Date lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
} 