package com.example.timemanagementapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.timemanagementapp.data.local.converter.DateConverter;
import com.example.timemanagementapp.data.local.dao.TaskDao;
import com.example.timemanagementapp.data.local.dao.ProjectDao;
import com.example.timemanagementapp.data.local.dao.UserDao;
import com.example.timemanagementapp.data.local.entity.Project;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.data.local.entity.User;
import com.example.timemanagementapp.data.local.dao.TaskCommentDao;
import com.example.timemanagementapp.data.local.entity.TaskComment;

@Database(entities = {Task.class, Project.class, User.class, TaskComment.class}, version = 5, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "time_management_db";
    private static volatile AppDatabase INSTANCE;
    
    public abstract TaskCommentDao taskCommentDao();
    public abstract TaskDao taskDao();
    public abstract ProjectDao projectDao();
    public abstract UserDao userDao();
    // Можно добавить другие DAO: ProjectDao, UserDao

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 