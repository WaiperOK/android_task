package com.example.timemanagementapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.timemanagementapp.data.local.entity.Project;
import java.util.List;

@Dao
public interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProject(Project project);

    @Update
    void updateProject(Project project);

    @Delete
    void deleteProject(Project project);

    @Query("SELECT * FROM projects ORDER BY name ASC")
    LiveData<List<Project>> getAllProjects();

    @Query("SELECT * FROM projects WHERE project_id = :projectId")
    LiveData<Project> getProjectById(String projectId);

    @Query("SELECT * FROM projects WHERE owner_user_id = :userId ORDER BY name ASC")
    LiveData<List<Project>> getProjectsByOwner(String userId);
} 