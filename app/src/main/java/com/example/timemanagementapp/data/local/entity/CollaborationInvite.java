package com.example.timemanagementapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timemanagementapp.data.local.converter.DateConverter;

import java.util.Date;

@Entity(tableName = "collaboration_invites",
        foreignKeys = {
                @ForeignKey(entity = Project.class,
                        parentColumns = "project_id",
                        childColumns = "project_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "inviter_user_id",
                        onDelete = ForeignKey.CASCADE)
        })
@TypeConverters(DateConverter.class)
public class CollaborationInvite {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "invite_id")
    private String inviteId;

    @NonNull
    @ColumnInfo(name = "project_id", index = true)
    private String projectId;

    @NonNull
    @ColumnInfo(name = "inviter_user_id", index = true)
    private String inviterUserId;

    @NonNull
    @ColumnInfo(name = "invited_email")
    private String invitedEmail;

    @NonNull
    @ColumnInfo(name = "role")
    private String role; // editor, viewer

    @NonNull
    @ColumnInfo(name = "status")
    private String status; // pending, accepted, rejected

    @NonNull
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "resolved_at")
    private Date resolvedAt;

    // Getters and Setters
    @NonNull
    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(@NonNull String inviteId) {
        this.inviteId = inviteId;
    }

    @NonNull
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(@NonNull String projectId) {
        this.projectId = projectId;
    }

    @NonNull
    public String getInviterUserId() {
        return inviterUserId;
    }

    public void setInviterUserId(@NonNull String inviterUserId) {
        this.inviterUserId = inviterUserId;
    }

    @NonNull
    public String getInvitedEmail() {
        return invitedEmail;
    }

    public void setInvitedEmail(@NonNull String invitedEmail) {
        this.invitedEmail = invitedEmail;
    }

    @NonNull
    public String getRole() {
        return role;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
} 