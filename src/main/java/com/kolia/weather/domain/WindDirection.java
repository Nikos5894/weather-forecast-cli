package com.kolia.weather.domain;

import com.kolia.weather.api.dto.Hour;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class WindDirection {

    private static final String UNKNOWN = "n/a";

    private static final String[] COMPASS_POINTS = {
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    };

    private WindDirection(){

    }

    static String prevailing(List<Hour> hours) {
        if(hours == null || hours.isEmpty()){
            return UNKNOWN;
        }
        return mostFrequentLabel(hours);
    }
    private static String mostFrequentLabel(List<Hour> hours) {
        Map<String, Long> counts = hours.stream()
                .map(Hour::windDir)
                .filter(dir -> dir != null && !dir.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (counts.isEmpty()) {
            return UNKNOWN;
        }

        return counts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(UNKNOWN);
    }
}
