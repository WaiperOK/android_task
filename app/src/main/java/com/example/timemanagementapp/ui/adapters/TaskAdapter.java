package com.example.timemanagementapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.Task;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {
    private OnItemClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTaskId().equals(newItem.getTaskId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    (oldItem.getDueDate() == null ? newItem.getDueDate() == null :
                            oldItem.getDueDate().equals(newItem.getDueDate()));
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
        holder.textViewTitle.setText(currentTask.getTitle());
        
        // Форматирование и отображение даты
        if (currentTask.getDueDate() != null) {
            holder.textViewDueDate.setVisibility(View.VISIBLE);
            holder.textViewDueDate.setText(dateFormat.format(currentTask.getDueDate()));
        } else {
            holder.textViewDueDate.setVisibility(View.GONE);
        }
        
        // Отображение статуса
        holder.textViewStatus.setText(currentTask.getStatus());
        
        // Отображение приоритета (можно использовать значки или цвета)
        String priorityText;
        switch (currentTask.getPriority()) {
            case 1:
                priorityText = "Низкий";
                break;
            case 3:
                priorityText = "Высокий";
                break;
            default:
                priorityText = "Средний";
        }
        holder.textViewPriority.setText(priorityText);
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDueDate;
        private TextView textViewStatus;
        private TextView textViewPriority;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_task_title);
            textViewDueDate = itemView.findViewById(R.id.text_view_task_due_date);
            textViewStatus = itemView.findViewById(R.id.text_view_task_status);
            textViewPriority = itemView.findViewById(R.id.text_view_task_priority);

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
} 