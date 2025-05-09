package com.example.timemanagementapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import com.example.timemanagementapp.data.local.converter.DateConverter;
import java.util.Date;
import java.util.UUID;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
@TypeConverters(DateConverter.class)
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    private String email;

    @NonNull
    private String name;

    private String photoUrl;

    @ColumnInfo(name = "is_current_user")
    private boolean isCurrentUser;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public User(@NonNull String email, @NonNull String name) {
        this.userId = UUID.randomUUID().toString();
        this.email = email;
        this.name = name;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        this.isCurrentUser = false;
    }

    // Getters and setters
    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
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