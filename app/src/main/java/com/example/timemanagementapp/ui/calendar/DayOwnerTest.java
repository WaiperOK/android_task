package com.example.timemanagementapp.ui.calendar;

import com.kizitonwose.calendar.core.DayOwner;

/**
 * Тестовый класс для проверки импорта DayOwner
 */
public class DayOwnerTest {

    /**
     * Проверяет работу с перечислением DayOwner
     */
    public void testDayOwner() {
        // Просто проверяем, можем ли мы получить доступ к константам
        DayOwner owner = DayOwner.THIS_MONTH;
        System.out.println("Day owner: " + owner.name());
        
        // Проверяем сравнение
        boolean isThisMonth = (owner == DayOwner.THIS_MONTH);
        System.out.println("Is this month: " + isThisMonth);
    }
} 