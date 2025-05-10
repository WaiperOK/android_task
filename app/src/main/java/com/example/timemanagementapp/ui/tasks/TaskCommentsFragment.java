package com.example.timemanagementapp.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.TaskComment;
import com.example.timemanagementapp.data.local.entity.User;
import com.example.timemanagementapp.data.local.entity.CurrentUserManager;
import com.example.timemanagementapp.ui.adapters.CommentAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class TaskCommentsFragment extends Fragment implements CommentAdapter.OnCommentActionListener {
    private static final String TAG = "CommentsDebug";
    private TaskViewModel viewModel;
    private CommentAdapter adapter;
    private EditText commentInput;
    private String taskId;
    private Map<String, User> userCache = new HashMap<>();

    public static TaskCommentsFragment newInstance(String taskId) {
        TaskCommentsFragment fragment = new TaskCommentsFragment();
        Bundle args = new Bundle();
        args.putString("task_id", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        if (getArguments() != null) {
            taskId = getArguments().getString("task_id");
            Log.d(TAG, "onCreate: TaskId received: " + taskId);
        } else {
            Log.e(TAG, "onCreate: TaskId is NULL in arguments.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called for taskId: " + taskId);

        // Инициализация RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.commentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        User currentUser = CurrentUserManager.getCurrentUser();
        adapter = new CommentAdapter(this, currentUser != null ? currentUser.getUserId() : null);
        recyclerView.setAdapter(adapter);

        // Инициализация поля ввода и кнопки отправки
        commentInput = view.findViewById(R.id.commentInput);
        MaterialButton sendButton = view.findViewById(R.id.sendCommentButton);

        sendButton.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (!text.isEmpty() && taskId != null) {
                Log.d(TAG, "Sending comment for taskId: " + taskId + ", text: " + text);
                viewModel.addComment(taskId, text);
                commentInput.setText("");
            }
        });

        // Наблюдение за комментариями
        if (taskId != null) {
            viewModel.getCommentsForTask(taskId).observe(getViewLifecycleOwner(), comments -> {
                if (comments != null) {
                    Log.d(TAG, "Comments received for taskId " + taskId + ". Count: " + comments.size());
                    adapter.submitList(comments);
                    if (comments.isEmpty()) {
                        Log.d(TAG, "Comment list is empty for taskId: " + taskId);
                    }
                } else {
                    Log.d(TAG, "Comments list is NULL for taskId: " + taskId);
                }
            });
        } else {
            Log.e(TAG, "onViewCreated: TaskId is NULL, cannot observe comments.");
        }

        // Наблюдение за пользователями для кэширования
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                Log.d(TAG, "Users received for cache. Count: " + users.size());
                userCache.clear();
                for (User user : users) {
                    userCache.put(user.getUserId(), user);
                }
                adapter.updateUserCache(userCache);
                 Log.d(TAG, "User cache updated in adapter. Size: " + userCache.size());
            } else {
                Log.d(TAG, "User list for cache is NULL.");
            }
        });
    }

    @Override
    public void onEditComment(TaskComment comment) {
        EditText input = new EditText(requireContext());
        input.setText(comment.getText());
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Редактировать комментарий")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newText = input.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        comment.setText(newText);
                        viewModel.updateComment(comment);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDeleteComment(TaskComment comment) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Удалить комментарий")
                .setMessage("Вы уверены, что хотите удалить этот комментарий?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    viewModel.deleteComment(comment);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onUserClicked(String userId) {
        User user = userCache.get(userId);
        if (user != null) {
            Toast.makeText(requireContext(), 
                "Пользователь: " + user.getName(), 
                Toast.LENGTH_SHORT).show();
        }
    }
} 