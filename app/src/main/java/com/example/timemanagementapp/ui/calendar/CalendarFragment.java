package com.example.timemanagementapp.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.Task;
import com.example.timemanagementapp.ui.adapters.TaskAdapter;
import com.example.timemanagementapp.ui.tasks.TaskEditFragment;
import com.example.timemanagementapp.ui.tasks.TaskViewModel;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayOwner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.format.DateFormat;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CalendarFragment extends Fragment {
    private CalendarView calendarView;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private List<Task> allTasks = new ArrayList<>();
    private Calendar selectedDate = null;
    private DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tasksRecyclerView = view.findViewById(R.id.recycler_view_calendar_tasks);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupRecyclerView();
        setupCalendarView();
        observeTasks();
        
        // Настройка FAB для добавления задачи на выбранную дату
        FloatingActionButton fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> {
            if (selectedDate != null) {
                openTaskEditFragmentWithDate(null, selectedDate);
            } else {
                selectedDate = Calendar.getInstance();
                openTaskEditFragmentWithDate(null, selectedDate);
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter();
        tasksRecyclerView.setAdapter(taskAdapter);
        
        // Добавляем слушатель клика на задачу
        taskAdapter.setOnItemClickListener(task -> {
            openTaskEditFragmentWithDate(task.getTaskId(), null);
        });
    }

    private void setupCalendarView() {
        // Настройка календаря
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(6);
        YearMonth lastMonth = currentMonth.plusMonths(6);
        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
        
        // Настройка отображения дней
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                TextView textView = container.textView;
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                
                // Используем обёртку CalendarDayWrapper для получения владельца дня
                CalendarDayWrapper dayWrapper = new CalendarDayWrapper(day);
                
                if (dayWrapper.getOwner() == DayOwner.THIS_MONTH) {
                    // Показываем текущий месяц с нормальной непрозрачностью
                    textView.setAlpha(1f);
                    
                    // Получаем Calendar для этого дня
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.setTime(Date.from(day.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    
                    // Проверяем, является ли день сегодняшним
                    boolean isToday = day.getDate().equals(LocalDate.now());
                    
                    // Проверяем, выбран ли день
                    boolean isSelected = selectedDate != null && isSameDay(dayCalendar, selectedDate);
                    
                    // Проверяем, есть ли задачи на этот день
                    boolean hasTasksForDay = hasTasks(dayCalendar);
                    
                    if (isSelected) {
                        // Выбранный день
                        textView.setBackgroundResource(R.drawable.selected_day_background);
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                    } else if (isToday) {
                        // Сегодняшний день
                        textView.setBackgroundResource(R.drawable.current_day_background);
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                    } else {
                        // Обычный день
                        textView.setBackground(null);
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                    }
                    
                    // Отображение индикатора задач
                    if (container.eventIndicator != null) {
                        container.eventIndicator.setVisibility(hasTasksForDay ? View.VISIBLE : View.INVISIBLE);
                    }
                    
                    // Обработчик клика по дате
                    container.view.setOnClickListener(v -> {
                        // Сохраняем предыдущую выбранную дату для обновления UI
                        Calendar oldSelectedDate = selectedDate;
                        
                        // Обновляем выбранную дату
                        selectedDate = dayCalendar;
                        
                        // Обновляем список задач для выбранной даты
                        filterTasksForDate(selectedDate);
                        
                        // Обновляем внешний вид календаря
                        if (oldSelectedDate != null) {
                            LocalDate oldDate = LocalDate.of(
                                oldSelectedDate.get(Calendar.YEAR),
                                oldSelectedDate.get(Calendar.MONTH) + 1,
                                oldSelectedDate.get(Calendar.DAY_OF_MONTH)
                            );
                            calendarView.notifyDateChanged(oldDate);
                        }
                        
                        calendarView.notifyDateChanged(day.getDate());
                    });
                } else {
                    // Дни из других месяцев показываем с меньшей непрозрачностью
                    textView.setAlpha(0.3f);
                    textView.setBackground(null);
                    
                    // Скрываем индикатор задач
                    if (container.eventIndicator != null) {
                        container.eventIndicator.setVisibility(View.INVISIBLE);
                    }
                    
                    // Убираем обработчик клика
                    container.view.setOnClickListener(null);
                }
            }
        });
        
        // Настройка заголовка месяца
        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, CalendarMonth month) {
                container.textView.setText(month.getYearMonth().format(monthTitleFormatter));
            }
        });

        // Инициализация календаря
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);
    }

    private boolean hasTasks(Calendar date) {
        if (allTasks == null || allTasks.isEmpty()) {
            return false;
        }
        
        for (Task task : allTasks) {
            if (task.getDueDate() != null && isSameDay(task.getDueDate(), date)) {
                return true;
            }
        }
        return false;
    }

    private void observeTasks() {
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                allTasks = tasks;
                
                // Уведомляем календарь об обновлении данных
                calendarView.notifyCalendarChanged();
                
                // Отображение задач для выбранной даты (если выбрана)
                if (selectedDate != null) {
                    filterTasksForDate(selectedDate);
                }
            }
        });
    }

    private void filterTasksForDate(Calendar date) {
        if (date == null || allTasks == null) {
            taskAdapter.submitList(new ArrayList<>());
            return;
        }
        
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getDueDate() != null && isSameDay(task.getDueDate(), date)) {
                filteredTasks.add(task);
            }
        }
        
        taskAdapter.submitList(filteredTasks);
    }

    // Вспомогательный метод для проверки, совпадают ли две даты по дню
    private boolean isSameDay(Date date1, Calendar date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        return cal1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR);
    }
    
    private boolean isSameDay(Calendar date1, Calendar date2) {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
               date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Открывает фрагмент для редактирования или создания задачи с предустановленной датой
     * @param taskId ID задачи для редактирования или null для создания новой
     * @param date Дата, которую нужно установить для новой задачи, или null если не нужно устанавливать
     */
    private void openTaskEditFragmentWithDate(String taskId, Calendar date) {
        TaskEditFragment fragment = new TaskEditFragment();
        
        Bundle args = new Bundle();
        
        if (taskId != null) {
            // Если редактируем существующую задачу, передаем ее ID
            args.putString(TaskEditFragment.ARG_TASK_ID, taskId);
        }
        
        if (date != null) {
            // Если создаем задачу на конкретную дату, передаем дату в миллисекундах
            args.putLong(TaskEditFragment.ARG_DUE_DATE, date.getTimeInMillis());
        }
        
        if (!args.isEmpty()) {
            fragment.setArguments(args);
        }
        
        // Заменяем текущий фрагмент на TaskEditFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null) // Чтобы можно было вернуться назад
                .commit();
    }
    
    // ViewContainer для дней
    public static class DayViewContainer extends ViewContainer {
        TextView textView;
        View eventIndicator;
        View view;

        public DayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            this.textView = view.findViewById(R.id.calendarDayText);
            this.eventIndicator = view.findViewById(R.id.eventIndicator);
        }
    }
    
    // ViewContainer для заголовка месяца
    public static class MonthViewContainer extends ViewContainer {
        TextView textView;

        public MonthViewContainer(@NonNull View view) {
            super(view);
            this.textView = view.findViewById(R.id.headerMonthText);
        }
    }
} 