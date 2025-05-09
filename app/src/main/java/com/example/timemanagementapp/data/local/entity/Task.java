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

@Entity(tableName = "tasks",
        foreignKeys = {
                @ForeignKey(entity = Project.class,
                            parentColumns = "project_id",
                            childColumns = "project_id",
                            onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = User.class,
                            parentColumns = "user_id",
                            childColumns = "assignee_user_id",
                            onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Task.class,
                            parentColumns = "task_id",
                            childColumns = "parent_task_id",
                            onDelete = ForeignKey.CASCADE)
        })
@TypeConverters(DateConverter.class)
public class Task {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "task_id")
    public String taskId;

    @ColumnInfo(name = "project_id", index = true)
    public String projectId;

    @NonNull
    public String title;
    public String description;

    @ColumnInfo(name = "assignee_user_id", index = true)
    public String assigneeUserId;

    @NonNull
    @ColumnInfo(name = "creator_user_id")
    public String creatorUserId;

    @ColumnInfo(name = "due_date")
    public Date dueDate;

    public int priority; // e.g., 1 (low), 2 (medium), 3 (high)

    @NonNull
    public String status; // e.g., "todo", "in_progress", "done"

    @ColumnInfo(name = "is_recurring")
    public boolean isRecurring;

    @ColumnInfo(name = "recurrence_rule")
    public String recurrenceRule;

    @ColumnInfo(name = "parent_task_id", index = true)
    public String parentTaskId;

    @NonNull
    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    @ColumnInfo(name = "completed_at")
    public Date completedAt;

    @ColumnInfo(name = "reminder_offset_millis")
    public Long reminderOffsetMillisBeforeDueDate; // null if no reminder, 0 for on-time, >0 for before

    public Task(@NonNull String title, @NonNull String creatorUserId) {
        this.taskId = UUID.randomUUID().toString();
        this.title = title;
        this.creatorUserId = creatorUserId;
        this.priority = 2; // Default priority
        this.status = "todo"; // Default status
        this.isRecurring = false;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getters and setters
    @NonNull
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(@NonNull String taskId) {
        this.taskId = taskId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(String assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    @NonNull
    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(@NonNull String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
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

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    // Getter and Setter for reminderOffsetMillisBeforeDueDate
    public Long getReminderOffsetMillisBeforeDueDate() {
        return reminderOffsetMillisBeforeDueDate;
    }

    public void setReminderOffsetMillisBeforeDueDate(Long reminderOffsetMillisBeforeDueDate) {
        this.reminderOffsetMillisBeforeDueDate = reminderOffsetMillisBeforeDueDate;
    }
} 