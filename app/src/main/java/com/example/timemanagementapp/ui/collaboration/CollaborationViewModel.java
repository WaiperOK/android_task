package com.example.timemanagementapp.ui.collaboration;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timemanagementapp.data.TaskRepository;
import com.example.timemanagementapp.data.local.entity.User;
import com.example.timemanagementapp.data.local.entity.Project;
import com.example.timemanagementapp.data.local.entity.CurrentUserManager;
import com.example.timemanagementapp.data.local.entity.CollaborationInvite;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CollaborationViewModel extends AndroidViewModel {
    private static final String TAG = "CollaborationViewModel";
    private TaskRepository repository;
    private MutableLiveData<String> _inviteStatus = new MutableLiveData<>();
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    // Добавленные поля для хранения приглашений
    private MutableLiveData<List<CollaborationInvite>> _pendingInvites = new MutableLiveData<>();

    public LiveData<String> getInviteStatus() {
        return _inviteStatus;
    }
    
    public LiveData<List<CollaborationInvite>> getPendingInvites() {
        return _pendingInvites;
    }

    public CollaborationViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
    }

    /**
     * Отправить приглашение пользователю по email для совместной работы над проектом
     * 
     * @param email Email пользователя для приглашения
     * @param projectId ID проекта для приглашения
     * @param role Роль пользователя в проекте (editor, viewer)
     */
    public void inviteUserToProject(String email, String projectId, String role) {
        if (email == null || email.isEmpty() || projectId == null || projectId.isEmpty() || role == null) {
            _inviteStatus.postValue("Не всі поля заповнені");
            return;
        }

        User currentUser = CurrentUserManager.getCurrentUser();
        if (currentUser == null) {
            _inviteStatus.postValue("Помилка: спочатку увійдіть в систему");
            return;
        }

        executor.execute(() -> {
            // Проверяем, существует ли проект
            LiveData<Project> projectLiveData = repository.getProjectById(projectId);
            Project project = null;
            
            // Здесь простая обработка - на самом деле нужен другой механизм ожидания LiveData
            try {
                Thread.sleep(500); // Ждем, чтобы LiveData успела загрузиться
                if (projectLiveData.getValue() != null) {
                    project = projectLiveData.getValue();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error waiting for project data: " + e.getMessage());
            }

            if (project == null) {
                _inviteStatus.postValue("Проект не знайдено");
                return;
            }

            // Проверяем, является ли текущий пользователь владельцем проекта
            if (!project.getOwnerUserId().equals(currentUser.getUserId())) {
                _inviteStatus.postValue("Ви не є власником цього проекту");
                return;
            }

            // В реальном приложении здесь нужно реализовать отправку email
            // Для прототипа сохраняем запись в локальную базу данных
            CollaborationInvite invite = new CollaborationInvite();
            invite.setInviteId(UUID.randomUUID().toString());
            invite.setProjectId(projectId);
            invite.setInvitedEmail(email);
            invite.setInviterUserId(currentUser.getUserId());
            invite.setRole(role);
            invite.setCreatedAt(new Date());
            invite.setStatus("pending");
            
            // В реальной реализации нужен CollaborationInviteDao
            // repository.insertInvite(invite);

            _inviteStatus.postValue("Запрошення надіслано на " + email);
            
            // Имитация отправки email
            Log.d(TAG, "Email invitation sent to " + email + " for project " + projectId);
        });
    }
    
    /**
     * Загрузить ожидающие приглашения для пользователя по email
     * 
     * @param email Email пользователя
     */
    public void loadPendingInvitesForUser(String email) {
        if (email == null || email.isEmpty()) {
            _inviteStatus.postValue("Email не вказано");
            return;
        }
        
        // В реальном приложении здесь нужно использовать CollaborationInviteDao
        // LiveData<List<CollaborationInvite>> invites = repository.getPendingInvitesForEmail(email);
        // _pendingInvites.postValue(invites.getValue());
        
        // Для прототипа создаем тестовые данные
        executor.execute(() -> {
            // Имитация задержки загрузки
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            List<CollaborationInvite> mockInvites = createMockInvites(email);
            _pendingInvites.postValue(mockInvites);
        });
    }
    
    /**
     * Получить название проекта по ID
     * 
     * @param projectId ID проекта
     */
    public LiveData<String> getProjectName(String projectId) {
        MutableLiveData<String> projectName = new MutableLiveData<>();
        
        if (projectId == null || projectId.isEmpty()) {
            projectName.setValue("Невідомий проект");
            return projectName;
        }
        
        executor.execute(() -> {
            // В реальном приложении здесь нужно использовать Project DAO
            // Project project = repository.getProjectById(projectId).getValue();
            // if (project != null) {
            //     projectName.postValue(project.getName());
            // } else {
            //     projectName.postValue("Невідомий проект");
            // }
            
            // Для прототипа возвращаем фиксированное значение
            projectName.postValue("Тестовий проект");
        });
        
        return projectName;
    }
    
    /**
     * Получить имя пользователя по ID
     * 
     * @param userId ID пользователя
     */
    public LiveData<String> getUserName(String userId) {
        MutableLiveData<String> userName = new MutableLiveData<>();
        
        if (userId == null || userId.isEmpty()) {
            userName.setValue("Невідомий користувач");
            return userName;
        }
        
        executor.execute(() -> {
            // В реальном приложении здесь нужно использовать User DAO
            // User user = repository.getUserById(userId).getValue();
            // if (user != null) {
            //     userName.postValue(user.getName());
            // } else {
            //     userName.postValue("Невідомий користувач");
            // }
            
            // Для прототипа возвращаем фиксированное значение
            userName.postValue("Адміністратор");
        });
        
        return userName;
    }
    
    /**
     * Ответить на приглашение (принять или отклонить)
     * 
     * @param inviteId ID приглашения
     * @param accept true - принять, false - отклонить
     */
    public void respondToInvite(String inviteId, boolean accept) {
        if (inviteId == null || inviteId.isEmpty()) {
            _inviteStatus.postValue("Помилка: ID запрошення не вказано");
            return;
        }
        
        User currentUser = CurrentUserManager.getCurrentUser();
        if (currentUser == null) {
            _inviteStatus.postValue("Помилка: спочатку увійдіть в систему");
            return;
        }
        
        executor.execute(() -> {
            // В реальном приложении здесь нужно использовать CollaborationInviteDao
            // CollaborationInvite invite = repository.getInviteById(inviteId).getValue();
            // if (invite == null) {
            //     _inviteStatus.postValue("Запрошення не знайдено");
            //     return;
            // }
            
            // if (!invite.getInvitedEmail().equals(currentUser.getEmail())) {
            //     _inviteStatus.postValue("Це запрошення не для вас");
            //     return;
            // }
            
            // Обновляем статус приглашения
            // invite.setStatus(accept ? "accepted" : "rejected");
            // invite.setResolvedAt(new Date());
            // repository.updateInvite(invite);
            
            // Если приглашение принято, создаем запись о сотрудничестве
            // if (accept) {
            //     ProjectCollaborator collaborator = new ProjectCollaborator(
            //         invite.getProjectId(), currentUser.getUserId(), invite.getRole()
            //     );
            //     repository.insertProjectCollaborator(collaborator);
            // }
            
            // Для прототипа просто обновляем UI
            _inviteStatus.postValue(accept ? 
                "Запрошення прийнято. Тепер ви можете працювати з проектом." :
                "Запрошення відхилено.");
            
            // Обновляем список приглашений в UI
            List<CollaborationInvite> currentInvites = _pendingInvites.getValue();
            if (currentInvites != null) {
                for (int i = 0; i < currentInvites.size(); i++) {
                    if (currentInvites.get(i).getInviteId().equals(inviteId)) {
                        currentInvites.remove(i);
                        break;
                    }
                }
                _pendingInvites.postValue(currentInvites);
            }
        });
    }
    
    // Вспомогательный метод для создания тестовых данных
    private List<CollaborationInvite> createMockInvites(String email) {
        List<CollaborationInvite> invites = new java.util.ArrayList<>();
        
        CollaborationInvite invite1 = new CollaborationInvite();
        invite1.setInviteId(UUID.randomUUID().toString());
        invite1.setProjectId("project_1");
        invite1.setInvitedEmail(email);
        invite1.setInviterUserId("admin_001");
        invite1.setRole("editor");
        invite1.setCreatedAt(new Date());
        invite1.setStatus("pending");
        
        CollaborationInvite invite2 = new CollaborationInvite();
        invite2.setInviteId(UUID.randomUUID().toString());
        invite2.setProjectId("project_2");
        invite2.setInvitedEmail(email);
        invite2.setInviterUserId("admin_002");
        invite2.setRole("viewer");
        invite2.setCreatedAt(new Date());
        invite2.setStatus("pending");
        
        invites.add(invite1);
        invites.add(invite2);
        
        return invites;
    }
} 