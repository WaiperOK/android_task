package com.example.timemanagementapp.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.data.local.entity.User;
import com.example.timemanagementapp.ui.tasks.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.Log;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {
    private static final String TAG = "TaskAdapter";
    private OnItemClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private TaskViewModel taskViewModel;
    private List<User> userList = new ArrayList<>();

    public TaskAdapter(TaskViewModel taskViewModel) {
        super(DIFF_CALLBACK);
        this.taskViewModel = taskViewModel;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTaskId().equals(newItem.getTaskId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            Log.d("DiffUtil", "Comparing tasks: ID " + oldItem.getTaskId() + " Title: " + oldItem.getTitle());
            Log.d("DiffUtil", "Old - startTime: " + oldItem.getTimeTrackingStartTimeMillis() + ", spent: " + oldItem.getTimeSpentMillis());
            Log.d("DiffUtil", "New - startTime: " + newItem.getTimeTrackingStartTimeMillis() + ", spent: " + newItem.getTimeSpentMillis());
            boolean result = oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    Objects.equals(oldItem.getDueDate(), newItem.getDueDate()) &&
                    oldItem.getTimeSpentMillis() == newItem.getTimeSpentMillis() &&
                    Objects.equals(oldItem.getAssigneeUserId(), newItem.getAssigneeUserId()) &&
                    Objects.equals(oldItem.getTimeTrackingStartTimeMillis(), newItem.getTimeTrackingStartTimeMillis());
            Log.d("DiffUtil", "Result of areContentsTheSame: " + result);
            return result;
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = getItem(position);
        Log.d(TAG, "onBindViewHolder for task: " + currentTask.getTitle() + " ID: " + currentTask.getTaskId() + " startTime: " + currentTask.getTimeTrackingStartTimeMillis() + " totalSpent: " + currentTask.getTimeSpentMillis());
        holder.textViewTitle.setText(currentTask.getTitle());
        
        // Форматирование и отображение даты
        if (currentTask.getDueDate() != null) {
            holder.textViewDueDate.setVisibility(View.VISIBLE);
            holder.textViewDueDate.setText(dateFormat.format(currentTask.getDueDate()));
        } else {
            holder.textViewDueDate.setVisibility(View.GONE);
        }
        
        // Отображение исполнителя
        if (currentTask.getAssigneeUserId() != null && !currentTask.getAssigneeUserId().isEmpty()) {
            String assigneeName = getUserNameById(currentTask.getAssigneeUserId());
            holder.textViewTaskAssignee.setText(
                String.format(holder.itemView.getContext().getString(R.string.assignee_format), assigneeName)
            );
            holder.textViewTaskAssignee.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTaskAssignee.setVisibility(View.GONE);
        }

        // Отображение статуса
        holder.textViewStatus.setText(currentTask.getStatus());
        // Здесь можно также добавить логику для изменения фона/цвета текста статуса, если потребуется
        // Например, holder.textViewStatus.setBackgroundResource(R.drawable.status_background_completed);

        // Отображение приоритета
        String priorityText;
        int priorityColorResId;
        int priorityTextColor = Color.WHITE; // По умолчанию белый текст

        switch (currentTask.getPriority()) {
            case 1: // Низкий
                priorityText = "Низкий";
                priorityColorResId = R.drawable.priority_background_low;
                break;
            case 3: // Высокий
                priorityText = "Высокий";
                priorityColorResId = R.drawable.priority_background_high;
                break;
            default: // Средний (например, 2 или любое другое значение)
                priorityText = "Средний";
                priorityColorResId = R.drawable.priority_background_medium;
                priorityTextColor = Color.BLACK; // Для желтого фона лучше черный текст
                break;
        }
        holder.textViewPriority.setText(priorityText);
        holder.textViewPriority.setBackgroundResource(priorityColorResId);
        holder.textViewPriority.setTextColor(priorityTextColor);

        // Отображение затраченного времени и управление таймером
        long displayTimeMillis;
        if (currentTask.getTimeTrackingStartTimeMillis() != null && currentTask.getTimeTrackingStartTimeMillis() > 0) {
            // Таймер запущен
            displayTimeMillis = currentTask.getTimeSpentMillis() + (System.currentTimeMillis() - currentTask.getTimeTrackingStartTimeMillis());
            holder.buttonPlayPauseTimer.setImageResource(R.drawable.ic_stop);
            Log.d(TAG, "Task " + currentTask.getTaskId() + ": Timer RUNNING. Setting ic_stop. Calculated displayTime: " + displayTimeMillis);
            holder.buttonPlayPauseTimer.setOnClickListener(v -> {
                Log.d(TAG, "Stop button clicked for task: " + currentTask.getTaskId());
                taskViewModel.stopTrackingTime(currentTask);
            });
        } else {
            // Таймер остановлен
            displayTimeMillis = currentTask.getTimeSpentMillis();
            holder.buttonPlayPauseTimer.setImageResource(R.drawable.ic_play_arrow);
            Log.d(TAG, "Task " + currentTask.getTaskId() + ": Timer STOPPED. Setting ic_play_arrow. DisplayTime: " + displayTimeMillis);
            holder.buttonPlayPauseTimer.setOnClickListener(v -> {
                Log.d(TAG, "Play button clicked for task: " + currentTask.getTaskId());
                taskViewModel.startTrackingTime(currentTask);
            });
        }

        if (displayTimeMillis > 0) {
            holder.textViewTimeSpent.setVisibility(View.VISIBLE);
            holder.textViewTimeSpent.setText(formatMillisToTime(displayTimeMillis));
        } else {
            // Если общее время 0 и таймер не запущен, можно скрыть или показать 0
            holder.textViewTimeSpent.setText(formatMillisToTime(0)); // Показываем "0 сек"
            holder.textViewTimeSpent.setVisibility(View.VISIBLE); // или View.GONE если предпочитаете скрыть
        }
    }

    private String formatMillisToTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)); // Не ограничиваем 24 часами, т.к. это общее время
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d ч %02d мин", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%d мин %02d сек", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%d сек", seconds);
        }
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDueDate;
        private TextView textViewTaskAssignee;
        private TextView textViewStatus;
        private TextView textViewPriority;
        private TextView textViewTimeSpent;
        private android.widget.ImageButton buttonPlayPauseTimer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_task_title);
            textViewDueDate = itemView.findViewById(R.id.text_view_task_due_date);
            textViewTaskAssignee = itemView.findViewById(R.id.text_view_task_assignee);
            textViewStatus = itemView.findViewById(R.id.text_view_task_status);
            textViewPriority = itemView.findViewById(R.id.text_view_task_priority);
            textViewTimeSpent = itemView.findViewById(R.id.text_view_time_spent);
            buttonPlayPauseTimer = itemView.findViewById(R.id.button_play_pause_timer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setUserList(List<User> users) {
        this.userList = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String getUserNameById(String userId) {
        if (userId == null) return "";
        for (User user : userList) {
            if (userId.equals(user.getUserId())) {
                return user.getName();
            }
        }
        return userId; // Если не найдено, возвращаем ID
    }
} 