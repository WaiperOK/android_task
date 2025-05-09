package com.example.timemanagementapp.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.timemanagementapp.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayOwner;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.YearMonth;

/**
 * Простой класс для тестирования библиотеки календаря
 */
public class SimpleCalendarTest extends Fragment {

    private CalendarView calendarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        
        // Простая настройка календаря
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(6);
        YearMonth lastMonth = currentMonth.plusMonths(6);
        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
        
        // Создаем простой биндер, который будет только отображать номера дней
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                TextView textView = container.textView;
                // Просто отображаем день месяца
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                
                // Используем обёртку CalendarDayWrapper для получения владельца дня
                CalendarDayWrapper dayWrapper = new CalendarDayWrapper(day);
                
                // Устанавливаем различную непрозрачность в зависимости от принадлежности
                if (dayWrapper.getOwner() == DayOwner.THIS_MONTH) {
                    textView.setAlpha(1f); // Текущий месяц
                } else {
                    textView.setAlpha(0.3f); // Другие месяцы
                }
            }
        });
        
        // Настройка календаря
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        return view;
    }
    
    /**
     * Контейнер для представления дня
     */
    private static class DayViewContainer extends ViewContainer {
        TextView textView;

        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
} 