package com.example.timemanagementapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.TaskComment;
import com.example.timemanagementapp.data.local.entity.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import android.util.Log;

public class CommentAdapter extends ListAdapter<TaskComment, CommentAdapter.CommentViewHolder> {
    private static final String TAG = "CommentsDebug";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
    private Map<String, User> userCache = new HashMap<>();
    private final OnCommentActionListener listener;
    private String currentUserId;

    public interface OnCommentActionListener {
        void onEditComment(TaskComment comment);
        void onDeleteComment(TaskComment comment);
        void onUserClicked(String userId);
    }

    public CommentAdapter(OnCommentActionListener listener, String currentUserId) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    private static final DiffUtil.ItemCallback<TaskComment> DIFF_CALLBACK = new DiffUtil.ItemCallback<TaskComment>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskComment oldItem, @NonNull TaskComment newItem) {
            return oldItem.getCommentId().equals(newItem.getCommentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskComment oldItem, @NonNull TaskComment newItem) {
            return oldItem.getText().equals(newItem.getText()) &&
                   oldItem.getUpdatedAt().equals(newItem.getUpdatedAt());
        }
    };

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "CommentAdapter: onCreateViewHolder, viewType: " + viewType);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        TaskComment comment = getItem(position);
        Log.d(TAG, "CommentAdapter: onBindViewHolder for position " + position + ", comment ID: " + comment.getCommentId() + " by user: " + comment.getUserId());
        User author = userCache.get(comment.getUserId());
        
        holder.authorText.setText(author != null ? author.getName() : "Пользователь");
        holder.contentText.setText(comment.getText());
        
        String dateStr = formatDate(comment.getUpdatedAt() != null ? 
                comment.getUpdatedAt() : comment.getCreatedAt());
        holder.dateText.setText(dateStr);

        // Показываем кнопки редактирования только для комментариев текущего пользователя
        boolean isCurrentUserComment = comment.getUserId().equals(currentUserId);
        holder.editButton.setVisibility(isCurrentUserComment ? View.VISIBLE : View.GONE);
        holder.deleteButton.setVisibility(isCurrentUserComment ? View.VISIBLE : View.GONE);

        // Настраиваем слушатели
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) listener.onEditComment(comment);
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteComment(comment);
        });

        holder.authorText.setOnClickListener(v -> {
            if (listener != null) listener.onUserClicked(comment.getUserId());
        });
    }

    @Override
    public void submitList(@Nullable List<TaskComment> list) {
        super.submitList(list);
        Log.d(TAG, "CommentAdapter: submitList called. New list size: " + (list == null ? "null" : list.size()) + ", Current item count: " + getItemCount());
    }

    public void updateUserCache(Map<String, User> users) {
        Log.d(TAG, "CommentAdapter: updateUserCache called. Users map size: " + (users == null ? "null" : users.size()));
        if (users != null) {
            userCache.clear();
            userCache.putAll(users);
            // Рекомендуется избегать notifyDataSetChanged() с ListAdapter, если возможно.
            // Обновление имен пользователей может потребовать перерисовки видимых элементов.
            Log.d(TAG, "CommentAdapter: Notifying dataset changed after user cache update.");
            notifyDataSetChanged(); 
        }
    }

    private String formatDate(Date date) {
        return date != null ? dateFormat.format(date) : "";
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        final TextView authorText;
        final TextView dateText;
        final TextView contentText;
        final ImageButton editButton;
        final ImageButton deleteButton;

        CommentViewHolder(View itemView) {
            super(itemView);
            authorText = itemView.findViewById(R.id.commentAuthorText);
            dateText = itemView.findViewById(R.id.commentDateText);
            contentText = itemView.findViewById(R.id.commentContentText);
            editButton = itemView.findViewById(R.id.editCommentButton);
            deleteButton = itemView.findViewById(R.id.deleteCommentButton);
        }
    }
} 