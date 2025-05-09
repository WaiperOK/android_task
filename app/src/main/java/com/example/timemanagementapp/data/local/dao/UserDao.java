package com.example.timemanagementapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.timemanagementapp.data.local.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(String userId);

    @Query("SELECT * FROM users ORDER BY name ASC")
    LiveData<List<User>> getAllUsers();
} 