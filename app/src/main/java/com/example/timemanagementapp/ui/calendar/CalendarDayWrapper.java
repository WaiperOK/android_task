package com.example.timemanagementapp.ui.calendar;

import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayOwner;
import com.kizitonwose.calendar.core.DayPosition;

import java.time.LocalDate;

/**
 * Класс-обёртка для CalendarDay из библиотеки kizitonwose.calendar версии 2.3.0
 * Добавляет метод getOwner(), который отсутствует в новой версии библиотеки
 */
public class CalendarDayWrapper {
    private final CalendarDay calendarDay;

    public CalendarDayWrapper(CalendarDay calendarDay) {
        this.calendarDay = calendarDay;
    }

    /**
     * Возвращает владельца дня, определяя его на основе DayPosition.
     * @return DayOwner.THIS_MONTH, DayOwner.PREVIOUS_MONTH или DayOwner.NEXT_MONTH
     */
    public DayOwner getOwner() {
        DayPosition position = calendarDay.getPosition();

        if (position == DayPosition.MonthDate) {
            return DayOwner.THIS_MONTH;
        } else if (position == DayPosition.InDate) {
            return DayOwner.PREVIOUS_MONTH;
        } else if (position == DayPosition.OutDate) {
            return DayOwner.NEXT_MONTH;
        }
        // На случай, если DayPosition будет иметь другие значения в будущем,
        // хотя текущая версия библиотеки (2.3.0) определяет только эти три.
        // Можно бросить исключение или вернуть значение по умолчанию.
        // В данном контексте, если day.getPosition() возвращает что-то иное, это будет неожиданно.
        // Для безопасности, можно вернуть THIS_MONTH или null, в зависимости от требований.
        // Однако, стандартные DayPosition должны покрывать все случаи.
        // Вернем null, чтобы явно указать на проблему, если такое случится.
        return null; 
    }

    /**
     * Делегирует вызов к оригинальному CalendarDay
     */
    public LocalDate getDate() {
        return calendarDay.getDate();
    }
}