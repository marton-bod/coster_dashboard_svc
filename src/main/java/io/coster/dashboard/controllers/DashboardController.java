package io.coster.dashboard.controllers;

import io.coster.dashboard.domain.LineChartEntry;
import io.coster.dashboard.domain.PieChartEntry;
import io.coster.dashboard.services.AuthenticationService;
import io.coster.dashboard.services.DashboardService;
import io.coster.dashboard.utilities.ParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthenticationService authService;

    @GetMapping("/daily")
    public List<LineChartEntry> getDailyTotals(@RequestHeader(value = "auth_token") String token,
                                               @RequestHeader(value = "auth_id") String userId,
                                               @RequestParam(value = "month") String month) {
        checkAuthCredentials(token, userId);
        ParserUtil.YearAndMonth result = ParserUtil.parseYearAndMonth(month);
        return dashboardService.getDailyTotalsForMonth(userId, result.getYear(), result.getMonth())
                .entrySet().stream()
                .map(entry -> new LineChartEntry(entry.getKey(), entry.getValue()))
                .sorted(comparing(a -> Integer.valueOf(a.getX())))
                .collect(toList());
    }

    @GetMapping("/total")
    public DoubleSummaryStatistics getMonthlyStats(@RequestHeader(value = "auth_token") String token,
                                                   @RequestHeader(value = "auth_id") String userId,
                                                   @RequestParam(value = "month") String month) {
        checkAuthCredentials(token, userId);
        ParserUtil.YearAndMonth result = ParserUtil.parseYearAndMonth(month);
        return dashboardService.getSummaryStatsForMonth(userId, result.getYear(), result.getMonth());
    }

    @GetMapping("/category")
    public List<PieChartEntry> getCategoryTotals(@RequestHeader(value = "auth_token") String token,
                                                  @RequestHeader(value = "auth_id") String userId,
                                                  @RequestParam(value = "month") String month) {
        checkAuthCredentials(token, userId);
        ParserUtil.YearAndMonth result = ParserUtil.parseYearAndMonth(month);
        return dashboardService.getTotalByCategoriesForMonth(userId, result.getYear(), result.getMonth())
                .entrySet().stream()
                .map(entry -> new PieChartEntry(entry.getValue(), entry.getKey().name(), randomColor()))
                .collect(toList());
    }

    @GetMapping("/monthly")
    public List<LineChartEntry> getMonthlyTotals(@RequestHeader(value = "auth_token") String token,
                                               @RequestHeader(value = "auth_id") String userId) {
        checkAuthCredentials(token, userId);
        LocalDate date = LocalDate.now();
        int month = date.getMonthValue();
        int year = date.getYear();
        List<LineChartEntry> entries = new ArrayList<>();
        for (int i = month; i > 0; i--) {
            String x = year + "-" + i;
            double y = dashboardService.getSummaryStatsForMonth(userId, year, i).getSum();
            entries.add(new LineChartEntry(x, y));
        }
        for (int i = 0; i < 12 - month; i++) {
            String x = (year - 1) + "-" + (12 - i);
            double y = dashboardService.getSummaryStatsForMonth(userId, year - 1, 12 - i).getSum();
            entries.add(new LineChartEntry(x, y));
        }
        Collections.reverse(entries);
        return entries;
    }

    private String randomColor() {
        Random random = new Random();
        int nextInt = random.nextInt(0xffffff + 1);
        return String.format("#%06x", nextInt);
    }


    private void checkAuthCredentials(String token, String userId) {
        boolean isValid = authService.isUserAndTokenValid(userId, token);
        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials!");
        }
    }
}
