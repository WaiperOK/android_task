package com.example.timemanagementapp.data.local.entity;

public class CurrentUserManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
} 