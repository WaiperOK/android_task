package com.example.timemanagementapp.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.ui.adapters.TaskAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TaskListFragment extends Fragment {
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Сообщаем, что у фрагмента есть свое меню
        setHasOptionsMenu(true);
        // Инициализация ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Настройка RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Инициализация адаптера
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        // Наблюдение за LiveData из ViewModel
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
            // Здесь можно добавить логику для отображения пустого состояния, если tasks.isEmpty()
            // Также обновить состояние пунктов меню сортировки
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
            }
        });

        // Настройка слушателя клика по элементу
        adapter.setOnItemClickListener(task -> {
            // Открываем фрагмент редактирования с передачей ID задачи
            openTaskEditFragment(task.getTaskId());
        });

        // Настройка FAB для добавления новой задачи
        FloatingActionButton fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> {
            // Открываем фрагмент создания новой задачи
            openTaskEditFragment(null);
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.task_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Отмечаем текущий активный режим сортировки
        MenuItem sortByDueDateItem = menu.findItem(R.id.action_sort_by_due_date);
        MenuItem sortByPriorityItem = menu.findItem(R.id.action_sort_by_priority);

        if (taskViewModel.getCurrentSortMode() == TaskViewModel.SortMode.BY_PRIORITY) {
            sortByPriorityItem.setChecked(true);
            sortByDueDateItem.setChecked(false);
        } else {
            sortByDueDateItem.setChecked(true);
            sortByPriorityItem.setChecked(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sort_by_due_date) {
            taskViewModel.setSortMode(TaskViewModel.SortMode.BY_DUE_DATE);
            return true;
        } else if (itemId == R.id.action_sort_by_priority) {
            taskViewModel.setSortMode(TaskViewModel.SortMode.BY_PRIORITY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Открывает фрагмент для редактирования или создания задачи
     * @param taskId ID задачи для редактирования, или null для создания новой
     */
    private void openTaskEditFragment(String taskId) {
        TaskEditFragment fragment = new TaskEditFragment();
        
        if (taskId != null) {
            // Если редактируем существующую задачу, передаем ее ID
            Bundle args = new Bundle();
            args.putString(TaskEditFragment.ARG_TASK_ID, taskId);
            fragment.setArguments(args);
        }
        
        // Заменяем текущий фрагмент на TaskEditFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null) // Чтобы можно было вернуться назад
                .commit();
    }
} 