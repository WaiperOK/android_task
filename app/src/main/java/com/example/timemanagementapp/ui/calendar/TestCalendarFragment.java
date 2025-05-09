package com.example.timemanagementapp.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.timemanagementapp.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayOwner;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Тестовый фрагмент для проверки работы календаря
 */
public class TestCalendarFragment extends Fragment {

    private CalendarView calendarView;
    private LocalDate selectedDate = null;
    private DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        
        // Настройка календаря
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(6);
        YearMonth endMonth = currentMonth.plusMonths(6);
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
                    
                    // Проверяем, является ли день сегодняшним
                    boolean isToday = day.getDate().equals(LocalDate.now());
                    
                    // Проверяем, является ли день выбранным
                    boolean isSelected = selectedDate != null && day.getDate().equals(selectedDate);
                    
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
                    
                    // Обработчик клика по дате
                    container.view.setOnClickListener(v -> {
                        // Сохраняем ранее выбранную дату
                        LocalDate oldDate = selectedDate;
                        
                        // Обновляем выбранную дату
                        selectedDate = day.getDate();
                        
                        // Обновляем UI
                        calendarView.notifyDateChanged(selectedDate);
                        
                        if (oldDate != null) {
                            calendarView.notifyDateChanged(oldDate);
                        }
                    });
                } else {
                    // Дни из других месяцев показываем с меньшей непрозрачностью
                    textView.setAlpha(0.3f);
                    textView.setBackground(null);
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
        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        return view;
    }
    
    // ViewContainer для дней
    public static class DayViewContainer extends ViewContainer {
        TextView textView;
        View view;

        public DayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            this.textView = view.findViewById(R.id.calendarDayText);
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