package com.example.timemanagementapp.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.Project;
import com.example.timemanagementapp.data.local.entity.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TaskEditFragment extends Fragment {

    public static final String ARG_TASK_ID = "taskId";
    public static final String ARG_DUE_DATE = "dueDate";

    private TextInputEditText editTextTaskTitle, editTextTaskDescription;
    private Button buttonSetDueDate, buttonStartTime, buttonPauseTimer, buttonStopTimer;
    private TextView textViewDueDate, textViewTotalTimeSpent;
    private Chronometer chronometerTask;
    private FloatingActionButton fabSaveTask;
    private Spinner spinnerPriority, spinnerStatus, spinnerReminderTime, spinnerProject;

    private TaskViewModel taskViewModel;
    private Task currentTaskForEdit; // Используем для хранения копии задачи при редактировании
    private boolean isEditMode = false;
    private final Calendar calendar = Calendar.getInstance();
    private Long initialDueDateMillis = null; // Для хранения переданной даты для новой задачи

    private List<Project> projectListInternal = new ArrayList<>();
    private ArrayAdapter<String> projectSpinnerAdapter;

    private long timeWhenPaused = 0;
    private long accumulatedTimeSpentSessionUi = 0; // Время, накопленное в UI в текущей сессии редактирования

    // Внутренний класс для опций напоминаний
    private static class ReminderOption {
        private final String displayName;
        private final Long offsetMillis;

        public ReminderOption(String displayName, Long offsetMillis) {
            this.displayName = displayName;
            this.offsetMillis = offsetMillis;
        }

        public Long getOffsetMillis() {
            return offsetMillis;
        }

        @NonNull
        @Override
        public String toString() {
            return displayName;
        }
    }

    private final List<ReminderOption> reminderOptions = Arrays.asList(
            new ReminderOption("Не напоминать", null),
            new ReminderOption("В день события (в 09:00)", 0L),
            new ReminderOption("За 15 минут", TimeUnit.MINUTES.toMillis(15)),
            new ReminderOption("За 30 минут", TimeUnit.MINUTES.toMillis(30)),
            new ReminderOption("За 1 час", TimeUnit.HOURS.toMillis(1)),
            new ReminderOption("За 1 день", TimeUnit.DAYS.toMillis(1))
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        if (getArguments() != null && getArguments().containsKey("taskId")) {
            String taskId = getArguments().getString("taskId");
            if (taskId != null) {
                isEditMode = true;
                taskViewModel.getTaskById(taskId).observe(this, task -> {
                    if (task != null) {
                        currentTaskForEdit = new Task(task); // Работаем с копией
                        if (getView() != null) {
                            loadTaskDataToUI();
                        }
                    }
                });
            }
        } else {
            currentTaskForEdit = null; // Новая задача
            isEditMode = false;
            if (getArguments() != null && getArguments().containsKey(ARG_DUE_DATE)) {
                initialDueDateMillis = getArguments().getLong(ARG_DUE_DATE);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_edit, container, false);

        editTextTaskTitle = view.findViewById(R.id.edit_text_title);
        editTextTaskDescription = view.findViewById(R.id.edit_text_task_description);
        buttonSetDueDate = view.findViewById(R.id.button_set_due_date);
        textViewDueDate = view.findViewById(R.id.text_view_due_date);
        spinnerPriority = view.findViewById(R.id.spinner_priority);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);
        spinnerProject = view.findViewById(R.id.spinner_project);
        chronometerTask = view.findViewById(R.id.chronometer_task_timer);
        buttonStartTime = view.findViewById(R.id.button_start_timer);
        buttonPauseTimer = view.findViewById(R.id.button_pause_timer);
        buttonStopTimer = view.findViewById(R.id.button_stop_timer);
        textViewTotalTimeSpent = view.findViewById(R.id.text_view_total_time_spent);
        fabSaveTask = view.findViewById(R.id.fab_save_task);

        setupSpinners();
        setupDateTimePickers();
        setupTimerControls();
        setupSaveFab();

        if (!isEditMode) {
            resetToDefaultState();
        } else if (currentTaskForEdit != null && getView() != null) {
            // Если данные уже есть (например, после смены конфигурации), загружаем их
             if (editTextTaskTitle.getText().toString().isEmpty()) { // Проверка, что UI не заполнен
                loadTaskDataToUI();
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateToolbarTitle();
        observeProjects(); // Наблюдение за проектами начинаем здесь

        // Если currentTaskForEdit уже загружен (например, из onCreate), но UI еще не обновлен
        if (isEditMode && currentTaskForEdit != null && editTextTaskTitle.getText().toString().isEmpty()) {
            loadTaskDataToUI();
        } else if (!isEditMode && editTextTaskTitle.getText().toString().isEmpty()){
             resetToDefaultState(); // Для новой задачи, если onCreateView не успел
        }
    }

    private void updateToolbarTitle() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(isEditMode ? "Редактировать задачу" : "Создать задачу");
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }

    private void resetToDefaultState() {
        editTextTaskTitle.setText("");
        editTextTaskDescription.setText("");
        if (initialDueDateMillis != null) {
            Date initialDate = new Date(initialDueDateMillis);
            updateDueDateDisplay(initialDate);
            calendar.setTime(initialDate); // Устанавливаем календарь на эту дату для DatePicker
        } else {
            updateDueDateDisplay(null);
        }
        spinnerPriority.setSelection(1); // Medium priority
        spinnerStatus.setSelection(0);   // "To Do"
        spinnerReminderTime.setSelection(0); // "Не напоминать"

        if (projectSpinnerAdapter != null && projectSpinnerAdapter.getCount() > 0) {
            spinnerProject.setSelection(0); // "Без проекта"
        }

        chronometerTask.stop();
        chronometerTask.setBase(SystemClock.elapsedRealtime());
        accumulatedTimeSpentSessionUi = 0;
        timeWhenPaused = 0;
        updateTotalTimeSpentDisplay(0); // Общее время 0 для новой задачи
        buttonStartTime.setEnabled(true);
        buttonPauseTimer.setEnabled(false);
        buttonStopTimer.setEnabled(false);
    }

    private void loadTaskDataToUI() {
        if (currentTaskForEdit == null) return;

        editTextTaskTitle.setText(currentTaskForEdit.getTitle());
        editTextTaskDescription.setText(currentTaskForEdit.getDescription());
        updateDueDateDisplay(currentTaskForEdit.getDueDate());

        spinnerPriority.setSelection(currentTaskForEdit.getPriority() - 1);

        String[] statusArray = getResources().getStringArray(R.array.task_statuses_values);
        for (int i = 0; i < statusArray.length; i++) {
            if (statusArray[i].equals(currentTaskForEdit.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        selectReminderOptionInSpinner(currentTaskForEdit.getReminderOffsetMillisBeforeDueDate());
        selectProjectInSpinner(); // Выбор проекта

        // Логика отображения времени таймера
        if (currentTaskForEdit.getTimeTrackingStartTimeMillis() != null && currentTaskForEdit.getTimeTrackingStartTimeMillis() > 0) {
            long trackedMillis = System.currentTimeMillis() - currentTaskForEdit.getTimeTrackingStartTimeMillis();
            chronometerTask.setBase(SystemClock.elapsedRealtime() - trackedMillis);
            chronometerTask.start();
            buttonStartTime.setEnabled(false);
            buttonPauseTimer.setEnabled(true);
            buttonStopTimer.setEnabled(true);
            accumulatedTimeSpentSessionUi = trackedMillis; // Накапливаем то, что уже натикало из БД
        } else {
            chronometerTask.stop();
            chronometerTask.setBase(SystemClock.elapsedRealtime()); // Сброс для UI
            accumulatedTimeSpentSessionUi = 0; // Сброс UI-счетчика сессии
            buttonStartTime.setEnabled(true);
            buttonPauseTimer.setEnabled(false);
            buttonStopTimer.setEnabled(false);
        }
        updateTotalTimeSpentDisplay(currentTaskForEdit.getTimeSpentMillis());
    }

    private void setupSpinners() {
        // Priority
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.priority_levels, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // Status
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.task_statuses_display, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Reminder Time
        ArrayAdapter<ReminderOption> reminderAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, reminderOptions);
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminderTime.setAdapter(reminderAdapter);

        // Project
        setupProjectSpinner();
    }

    private void setupProjectSpinner() {
        List<String> initialProjectNames = new ArrayList<>();
        initialProjectNames.add("Без проекта");
        projectSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, initialProjectNames);
        projectSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(projectSpinnerAdapter);
    }

    private void observeProjects() {
        taskViewModel.getAllProjects().observe(getViewLifecycleOwner(), projects -> {
            if (projects != null) {
                projectListInternal.clear();
                projectListInternal.addAll(projects);

                List<String> projectDisplayNames = new ArrayList<>();
                projectDisplayNames.add("Без проекта");
                for (Project project : projectListInternal) {
                    projectDisplayNames.add(project.getName());
                }
                projectSpinnerAdapter.clear();
                projectSpinnerAdapter.addAll(projectDisplayNames);
                projectSpinnerAdapter.notifyDataSetChanged();

                if (isEditMode && currentTaskForEdit != null) {
                    selectProjectInSpinner();
                } else if (!isEditMode && spinnerProject.getSelectedItemPosition() != 0) {
                    // Для новой задачи, если вдруг что-то выбралось, сбросить на "Без проекта"
                    spinnerProject.setSelection(0);
                }
            }
        });
    }

    private void selectReminderOptionInSpinner(Long reminderOffset) {
        for (int i = 0; i < reminderOptions.size(); i++) {
            if (Objects.equals(reminderOptions.get(i).getOffsetMillis(), reminderOffset)) {
                spinnerReminderTime.setSelection(i);
                return;
            }
        }
        spinnerReminderTime.setSelection(0); // "Не напоминать" по умолчанию
    }

    private void selectProjectInSpinner() {
        if (projectSpinnerAdapter == null || projectSpinnerAdapter.getCount() == 0) return;

        if (currentTaskForEdit != null && currentTaskForEdit.getProjectId() != null && !projectListInternal.isEmpty()) {
            for (int i = 0; i < projectListInternal.size(); i++) {
                if (projectListInternal.get(i).getProjectId().equals(currentTaskForEdit.getProjectId())) {
                    spinnerProject.setSelection(i + 1); // +1 из-за "Без проекта"
                    return;
                }
            }
        }
        spinnerProject.setSelection(0); // "Без проекта"
    }

    private void setupDateTimePickers() {
        buttonSetDueDate.setOnClickListener(v -> {
            Calendar calToShow = Calendar.getInstance();
            if (currentTaskForEdit != null && currentTaskForEdit.getDueDate() != null) {
                calToShow.setTime(currentTaskForEdit.getDueDate());
            } else if (textViewDueDate.getTag() instanceof Date) {
                 calToShow.setTime((Date) textViewDueDate.getTag());
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, monthOfYear, dayOfMonth) -> {
                        calendar.set(year, monthOfYear, dayOfMonth,0,0,0); // Время сбрасываем в 00:00:00
                        calendar.set(Calendar.MILLISECOND, 0);
                        updateDueDateDisplay(calendar.getTime());
                    }, calToShow.get(Calendar.YEAR), calToShow.get(Calendar.MONTH), calToShow.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        textViewDueDate.setOnClickListener(v -> buttonSetDueDate.performClick());
    }

    private void updateDueDateDisplay(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            textViewDueDate.setText(sdf.format(date));
            textViewDueDate.setTag(date);
        } else {
            textViewDueDate.setText("Дата не установлена");
            textViewDueDate.setTag(null);
        }
    }

    private void setupTimerControls() {
        buttonStartTime.setOnClickListener(v -> {
            chronometerTask.setBase(SystemClock.elapsedRealtime() - accumulatedTimeSpentSessionUi);
            chronometerTask.start();
            timeWhenPaused = 0;
            buttonStartTime.setEnabled(false);
            buttonPauseTimer.setEnabled(true);
            buttonStopTimer.setEnabled(true);
        });

        buttonPauseTimer.setOnClickListener(v -> {
            timeWhenPaused = SystemClock.elapsedRealtime();
            chronometerTask.stop();
            accumulatedTimeSpentSessionUi = SystemClock.elapsedRealtime() - chronometerTask.getBase();
            buttonStartTime.setEnabled(true);
            buttonPauseTimer.setEnabled(false);
        });

        buttonStopTimer.setOnClickListener(v -> {
            chronometerTask.stop();
            accumulatedTimeSpentSessionUi = SystemClock.elapsedRealtime() - chronometerTask.getBase();
            // Здесь важно: accumulatedTimeSpentSessionUi теперь хранит время *этой сессии в UI*.
            // Не прибавляем его к currentTaskForEdit.getTimeSpentMillis() напрямую,
            // это сделает ViewModel при вызове stopTrackingTime или при сохранении.
            chronometerTask.setBase(SystemClock.elapsedRealtime()); // Сброс базы для UI
            // Сбрасываем accumulatedTimeSpentSessionUi только если это полностью новая задача
            // или если хотим, чтобы каждая остановка UI таймера сбрасывала сессионный счетчик UI
            // Пока оставим accumulatedTimeSpentSessionUi для накопления в рамках этого экрана,
            // оно будет учтено при сохранении, если таймер был активен.
            timeWhenPaused = 0;
            buttonStartTime.setEnabled(true);
            buttonPauseTimer.setEnabled(false);
            buttonStopTimer.setEnabled(false);
            updateTotalTimeSpentDisplay((currentTaskForEdit != null ? currentTaskForEdit.getTimeSpentMillis() : 0) + accumulatedTimeSpentSessionUi);
        });
    }

    private void updateTotalTimeSpentDisplay(long baseTimeSpentMillis) {
        // baseTimeSpentMillis - это время, уже сохраненное в БД.
        // accumulatedTimeSpentSessionUi - это время, натикавшее в UI в текущей сессии редактирования и еще не сохраненное.
        // Если chronometer активен, его текущее значение также нужно учесть.
        long currentChronometerMillis = 0;
        if (buttonPauseTimer.isEnabled() || buttonStopTimer.isEnabled()) { // Если таймер запущен или на паузе (но был запущен)
             currentChronometerMillis = SystemClock.elapsedRealtime() - chronometerTask.getBase();
        } else { // Если таймер остановлен кнопкой Stop или не запускался в UI
            currentChronometerMillis = accumulatedTimeSpentSessionUi;
        }

        long totalDisplayedTime = baseTimeSpentMillis + currentChronometerMillis;

        long hours = TimeUnit.MILLISECONDS.toHours(totalDisplayedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalDisplayedTime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalDisplayedTime) % 60;
        textViewTotalTimeSpent.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void setupSaveFab() {
        fabSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = editTextTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Название задачи не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }
        String description = editTextTaskDescription.getText().toString().trim();
        Date dueDate = textViewDueDate.getTag() instanceof Date ? (Date) textViewDueDate.getTag() : null;
        int priority = spinnerPriority.getSelectedItemPosition() + 1;
        String[] statusValues = getResources().getStringArray(R.array.task_statuses_values);
        String status = statusValues[spinnerStatus.getSelectedItemPosition()];

        Long reminderOffset = null;
        if (spinnerReminderTime.getSelectedItemPosition() > 0) {
            ReminderOption selectedOption = (ReminderOption) spinnerReminderTime.getSelectedItem();
            reminderOffset = selectedOption.getOffsetMillis();
        }

        String selectedProjectId = null;
        int projectPosition = spinnerProject.getSelectedItemPosition();
        if (projectPosition > 0 && !projectListInternal.isEmpty() && projectPosition <= projectListInternal.size()) {
            selectedProjectId = projectListInternal.get(projectPosition - 1).getProjectId();
        }

        if (isEditMode && currentTaskForEdit != null) {
            // Обновляем копию задачи
            currentTaskForEdit.setTitle(title);
            currentTaskForEdit.setDescription(description);
            currentTaskForEdit.setDueDate(dueDate);
            currentTaskForEdit.setPriority(priority);
            currentTaskForEdit.setStatus(status);
            currentTaskForEdit.setReminderOffsetMillisBeforeDueDate(reminderOffset);
            currentTaskForEdit.setProjectId(selectedProjectId);
            currentTaskForEdit.setUpdatedAt(new Date());

            // Логика сохранения времени:
            // Если таймер в UI был активен (или на паузе после активности),
            // и currentTaskForEdit.timeTrackingStartTimeMillis НЕ null (т.е. ViewModel запустил таймер),
            // то ViewModel.stopTrackingTime сам посчитает время.
            // Если currentTaskForEdit.timeTrackingStartTimeMillis ЕСТЬ null, но таймер в UI тикал,
            // то это время accumulatedTimeSpentSessionUi нужно добавить к общему.
            // Но ViewModel.update не примет это время.
            // Это сложный момент. Пока положимся на то, что start/stop ViewModel - основные.
            // Для простоты, если таймер был запущен (buttonPauseTimer.isEnabled()), то предполагаем, что он был запущен через ViewModel
            // и ViewModel.update сам его обработает при вызове stopTrackingTime, который должен вызываться перед сохранением.
            // Если ViewModel не знает о запущенном таймере, то время, натикавшее в UI (accumulatedTimeSpentSessionUi)
            // не будет автоматически добавлено к timeSpentMillis в БД через этот update.

            // Если таймер был запущен ViewModel (timeTrackingStartTimeMillis не null)
            if (currentTaskForEdit.getTimeTrackingStartTimeMillis() != null) {
                 // ViewModel должен был быть вызван для остановки таймера перед этим.
                 // Если нет, то время будет посчитано некорректно.
                 // Предположим, что пользователь нажал "Стоп" в UI адаптера или здесь.
                 // В TaskViewModel.update() вызывается scheduleOrCancelReminder, но не stopTrackingTime.
                 // Это значит, что если таймер не был остановлен явно (через stopTrackingTime), то при update()
                 // время последнего сеанса не добавится.
                 // Это нужно исправить либо в ViewModel.update, либо требовать явный stop.
                 // Пока, если таймер тикает в UI, остановим его здесь для сохранения.
                if (buttonPauseTimer.isEnabled() || buttonStopTimer.isEnabled()){ // Если таймер UI активен
                     long sessionTime = SystemClock.elapsedRealtime() - chronometerTask.getBase();
                     currentTaskForEdit.setTimeSpentMillis(currentTaskForEdit.getTimeSpentMillis() + sessionTime);
                     // Это изменение времени не будет учтено если не вызвать stopTrackingTime в ViewModel
                     // currentTaskForEdit.setTimeTrackingStartTimeMillis(null); // Это должен делать ViewModel
                }
            } else {
                // Таймер ViewModel не был активен. Учтем то, что натикало в UI
                // currentTaskForEdit.setTimeSpentMillis(currentTaskForEdit.getTimeSpentMillis() + accumulatedTimeSpentSessionUi);
                // Эта логика тоже не идеальна, т.к. accumulatedTimeSpentSessionUi может накапливаться не с нуля.
                // Оставляем обработку времени в TaskViewModel через start/stopTrackingTime
            }

            taskViewModel.update(currentTaskForEdit);
            Toast.makeText(requireContext(), "Задача обновлена", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: Получить реальный ID пользователя
            String creatorId = "default_user_id";
            Task newTask = new Task(title, creatorId);
            newTask.setDescription(description);
            newTask.setDueDate(dueDate);
            newTask.setPriority(priority);
            newTask.setStatus(status);
            newTask.setReminderOffsetMillisBeforeDueDate(reminderOffset);
            newTask.setProjectId(selectedProjectId);
            // newTask.setTimeSpentMillis(accumulatedTimeSpentSessionUi); // Для новой задачи это будет единственное время
            // Новая задача: время = 0. Таймер запускается после создания и сохранения.
            taskViewModel.insert(newTask);
            Toast.makeText(requireContext(), "Задача создана", Toast.LENGTH_SHORT).show();
        }

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.task_edit_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete_task);
        if (deleteItem != null) {
            deleteItem.setVisible(isEditMode);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_delete_task) {
            if (isEditMode && currentTaskForEdit != null) {
                taskViewModel.delete(currentTaskForEdit);
                Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
            return true;
        } else if (itemId == android.R.id.home) {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 