package io.coster.dashboard.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ParserUtil {

    public static YearAndMonth parseYearAndMonth(String param) {
        int y, m;
        try {
            String[] yearAndMonth = param.split("-");
            y = Integer.parseInt(yearAndMonth[0]);
            m = Integer.parseInt(yearAndMonth[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Year and month cannot be parsed into valid values: " + param);
        }
        if (2015 <= y && y <= 2030 && 1 <= m && m <= 12) {
            return new YearAndMonth(y, m);
        } else {
            throw new IllegalArgumentException("Year must be between 2015-2030, month must be between 1-12.");
        }
    }

    @Getter
    @AllArgsConstructor
    public static class YearAndMonth {
        int year;
        int month;
    }
}
