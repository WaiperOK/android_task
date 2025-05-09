package com.example.timemanagementapp.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TaskEditFragment extends Fragment {
    
    public static final String ARG_TASK_ID = "task_id";
    public static final String ARG_DUE_DATE = "due_date";
    
    private TaskViewModel taskViewModel;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private Button buttonPickDate;
    private TextView textViewSelectedDate;
    private AutoCompleteTextView autoCompletePriority;
    private AutoCompleteTextView autoCompleteStatus;
    private FloatingActionButton fabSaveTask;
    
    private Calendar selectedDueDate = null;
    private Task currentTask = null;
    private boolean isEditMode = false;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    
    // Массивы для выпадающих списков
    private final String[] priorities = new String[]{"Низкий", "Средний", "Высокий"};
    private final String[] statuses = new String[]{"Новая", "В процессе", "Завершена"};
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        setHasOptionsMenu(true);
        
        if (getArguments() != null) {
            // Проверка, открываем ли мы фрагмент для редактирования существующей задачи
            if (getArguments().containsKey(ARG_TASK_ID)) {
                isEditMode = true;
                String taskId = getArguments().getString(ARG_TASK_ID);
                
                // Загрузка задачи по ID
                taskViewModel.getTaskById(taskId).observe(this, task -> {
                    if (task != null) {
                        currentTask = task;
                        fillFormWithTaskData();
                    }
                });
            }
            
            // Проверка, передана ли дата для новой задачи
            if (getArguments().containsKey(ARG_DUE_DATE)) {
                long dueDateMillis = getArguments().getLong(ARG_DUE_DATE);
                selectedDueDate = Calendar.getInstance();
                selectedDueDate.setTimeInMillis(dueDateMillis);
            }
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_edit, container, false);
        
        // Настройка тулбара
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        activity.getSupportActionBar().setTitle(isEditMode ? "Редактирование задачи" : "Новая задача");
        
        // Инициализация компонентов интерфейса
        editTextTitle = view.findViewById(R.id.edit_text_title);
        editTextDescription = view.findViewById(R.id.edit_text_description);
        buttonPickDate = view.findViewById(R.id.button_pick_date);
        textViewSelectedDate = view.findViewById(R.id.text_view_selected_date);
        autoCompletePriority = view.findViewById(R.id.auto_complete_priority);
        autoCompleteStatus = view.findViewById(R.id.auto_complete_status);
        fabSaveTask = view.findViewById(R.id.fab_save_task);
        
        // Настройка выпадающих списков
        setupDropdownLists();
        
        // Обработчик клика на кнопку выбора даты
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog());
        
        // Обработчик клика на кнопку сохранения
        fabSaveTask.setOnClickListener(v -> saveTask());
        
        // Если дата была установлена через аргументы, показываем ее
        if (selectedDueDate != null) {
            textViewSelectedDate.setText(dateFormat.format(selectedDueDate.getTime()));
        }
        
        return view;
    }
    
    private void setupDropdownLists() {
        // Настройка выпадающего списка приоритетов
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_dropdown_item_1line, 
                priorities);
        autoCompletePriority.setAdapter(priorityAdapter);
        
        // По умолчанию устанавливаем средний приоритет
        autoCompletePriority.setText(priorities[1], false);
        
        // Настройка выпадающего списка статусов
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_dropdown_item_1line, 
                statuses);
        autoCompleteStatus.setAdapter(statusAdapter);
        
        // По умолчанию устанавливаем статус "Новая"
        autoCompleteStatus.setText(statuses[0], false);
    }
    
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate != null) {
            calendar = selectedDueDate;
        } else if (currentTask != null && currentTask.getDueDate() != null) {
            calendar.setTime(currentTask.getDueDate());
        }
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDueDate = Calendar.getInstance();
                    selectedDueDate.set(Calendar.YEAR, selectedYear);
                    selectedDueDate.set(Calendar.MONTH, selectedMonth);
                    selectedDueDate.set(Calendar.DAY_OF_MONTH, selectedDay);
                    textViewSelectedDate.setText(dateFormat.format(selectedDueDate.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }
    
    private void fillFormWithTaskData() {
        if (currentTask == null) return;
        
        editTextTitle.setText(currentTask.getTitle());
        editTextDescription.setText(currentTask.getDescription());
        
        if (currentTask.getDueDate() != null) {
            selectedDueDate = Calendar.getInstance();
            selectedDueDate.setTime(currentTask.getDueDate());
            textViewSelectedDate.setText(dateFormat.format(currentTask.getDueDate()));
        }
        
        // Установка приоритета
        int priorityIndex;
        switch (currentTask.getPriority()) {
            case 1:
                priorityIndex = 0; // Низкий
                break;
            case 3:
                priorityIndex = 2; // Высокий
                break;
            default:
                priorityIndex = 1; // Средний
        }
        autoCompletePriority.setText(priorities[priorityIndex], false);
        
        // Установка статуса
        int statusIndex;
        switch (currentTask.getStatus()) {
            case "in_progress":
                statusIndex = 1; // В процессе
                break;
            case "done":
                statusIndex = 2; // Завершена
                break;
            default:
                statusIndex = 0; // Новая
        }
        autoCompleteStatus.setText(statuses[statusIndex], false);
    }
    
    private void saveTask() {
        String title = Objects.requireNonNull(editTextTitle.getText()).toString().trim();
        String description = Objects.requireNonNull(editTextDescription.getText()).toString().trim();
        
        if (title.isEmpty()) {
            editTextTitle.setError("Введите название задачи");
            return;
        }
        
        // Получение приоритета
        int priority;
        String priorityText = autoCompletePriority.getText().toString();
        if (priorityText.equals(priorities[0])) {
            priority = 1; // Низкий
        } else if (priorityText.equals(priorities[2])) {
            priority = 3; // Высокий
        } else {
            priority = 2; // Средний
        }
        
        // Получение статуса
        String status;
        String statusText = autoCompleteStatus.getText().toString();
        if (statusText.equals(statuses[1])) {
            status = "in_progress"; // В процессе
        } else if (statusText.equals(statuses[2])) {
            status = "done"; // Завершена
        } else {
            status = "todo"; // Новая
        }
        
        // Установка даты
        Date dueDate = null;
        if (selectedDueDate != null) {
            dueDate = selectedDueDate.getTime();
        }
        
        if (isEditMode && currentTask != null) {
            // Обновление существующей задачи
            currentTask.setTitle(title);
            currentTask.setDescription(description);
            currentTask.setPriority(priority);
            currentTask.setStatus(status);
            currentTask.setDueDate(dueDate);
            currentTask.setUpdatedAt(new Date());
            
            taskViewModel.update(currentTask);
            Toast.makeText(requireContext(), "Задача обновлена", Toast.LENGTH_SHORT).show();
        } else {
            // Создание новой задачи
            Task newTask = new Task(title, "current_user"); // В реальном приложении здесь должен быть ID текущего пользователя
            newTask.setDescription(description);
            newTask.setPriority(priority);
            newTask.setStatus(status);
            newTask.setDueDate(dueDate);
            
            taskViewModel.insert(newTask);
            Toast.makeText(requireContext(), "Задача создана", Toast.LENGTH_SHORT).show();
        }
        
        // Возврат назад
        requireActivity().getSupportFragmentManager().popBackStack();
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_edit, menu);
        
        // Скрываем пункт меню "Удалить", если это режим создания новой задачи
        menu.findItem(R.id.action_delete_task).setVisible(isEditMode);
        
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            // Обработка нажатия кнопки "Назад" в тулбаре
            requireActivity().getSupportFragmentManager().popBackStack();
            return true;
        } else if (id == R.id.action_delete_task) {
            // Обработка нажатия кнопки "Удалить"
            if (currentTask != null) {
                taskViewModel.delete(currentTask);
                Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 