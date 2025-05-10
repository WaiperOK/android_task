package com.example.timemanagementapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timemanagementapp.data.local.entity.CollaborationInvite;

import java.util.List;

@Dao
public interface CollaborationInviteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvite(CollaborationInvite invite);

    @Update
    void updateInvite(CollaborationInvite invite);

    @Delete
    void deleteInvite(CollaborationInvite invite);

    @Query("SELECT * FROM collaboration_invites WHERE invite_id = :inviteId")
    LiveData<CollaborationInvite> getInviteById(String inviteId);

    @Query("SELECT * FROM collaboration_invites WHERE project_id = :projectId")
    LiveData<List<CollaborationInvite>> getInvitesForProject(String projectId);

    @Query("SELECT * FROM collaboration_invites WHERE inviter_user_id = :userId")
    LiveData<List<CollaborationInvite>> getInvitesSentByUser(String userId);

    @Query("SELECT * FROM collaboration_invites WHERE invited_email = :email")
    LiveData<List<CollaborationInvite>> getInvitesForEmail(String email);

    @Query("SELECT * FROM collaboration_invites WHERE invited_email = :email AND status = 'pending'")
    LiveData<List<CollaborationInvite>> getPendingInvitesForEmail(String email);

    @Query("UPDATE collaboration_invites SET status = :status WHERE invite_id = :inviteId")
    void updateInviteStatus(String inviteId, String status);
} 