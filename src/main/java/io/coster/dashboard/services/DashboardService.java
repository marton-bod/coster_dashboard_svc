package io.coster.dashboard.services;

import io.coster.dashboard.domain.Expense;
import io.coster.dashboard.domain.ExpenseCategory;
import io.coster.dashboard.repositories.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;

    public Map<String, Double> getDailyTotalsForMonth(String userId, int year, int month) {
        List<Expense> expenses = expenseRepository.findAllByUserIdAndYearAndMonth(userId, year, month);
        Map<String, Double> dailyExpenses = new HashMap<>();
        for (Expense expense : expenses) {
            String dayOfMonth = expense.getDate().getDayOfMonth() + "";
            if (dailyExpenses.containsKey(dayOfMonth)) {
                Double oldAmount = dailyExpenses.get(dayOfMonth);
                dailyExpenses.put(dayOfMonth, oldAmount + expense.getAmount());
            } else {
                dailyExpenses.put(dayOfMonth, expense.getAmount());
            }
        }
        return dailyExpenses;
    }

    public DoubleSummaryStatistics getSummaryStatsForMonth(String userId, int year, int month) {
        Map<String, Double> dailyTotals = getDailyTotalsForMonth(userId, year, month);
        return dailyTotals.values().stream().mapToDouble(i -> i).summaryStatistics();
    }

    public Map<ExpenseCategory, Double> getTotalByCategoriesForMonth(String userId, int year, int month) {
        List<Expense> expenses = expenseRepository.findAllByUserIdAndYearAndMonth(userId, year, month);
        Map<ExpenseCategory, Double> expensesByCategory = new HashMap<>();
        for (Expense expense : expenses) {
            ExpenseCategory category = expense.getCategory();
            if (expensesByCategory.containsKey(category)) {
                Double oldAmount = expensesByCategory.get(category);
                expensesByCategory.put(category, oldAmount + expense.getAmount());
            } else {
                expensesByCategory.put(category, expense.getAmount());
            }
        }
        return expensesByCategory;
    }
}
