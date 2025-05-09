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

@Entity(tableName = "projects",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "owner_user_id",
                        onDelete = ForeignKey.CASCADE)
        })
@TypeConverters(DateConverter.class)
public class Project {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "project_id")
    private String projectId;

    @NonNull
    private String name;

    private String description;

    @ColumnInfo(name = "owner_user_id", index = true)
    @NonNull
    private String ownerUserId;

    @ColumnInfo(name = "color_hex")
    private String colorHex;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public Project(@NonNull String name, @NonNull String ownerUserId) {
        this.projectId = UUID.randomUUID().toString();
        this.name = name;
        this.ownerUserId = ownerUserId;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        this.colorHex = "#4285F4"; // Default color
    }

    // Getters and setters
    @NonNull
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(@NonNull String projectId) {
        this.projectId = projectId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(@NonNull String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
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