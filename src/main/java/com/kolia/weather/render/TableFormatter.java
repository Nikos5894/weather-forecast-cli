package com.kolia.weather.render;

import com.kolia.weather.domain.CityForecast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TableFormatter {

    private static final DateTimeFormatter DATE_HEADER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US);

    private static final String[] METRICS = {"Min C", "Max C", "Humidity %", "Wind kph", "Wind dir"};
    private static final String CITY_HEADER = "City";
    private static final String MISSING = "-";
    private static final String SEPARATOR = "  ";

    public String format(List<CityForecast> forecasts) {
        if (forecasts.isEmpty()) {
            return "No forecast data available.";
        }

        List<LocalDate> dates = forecasts.stream()
                .map(CityForecast::date)
                .distinct()
                .sorted()
                .toList();

        List<String> cities = forecasts.stream()
                .map(CityForecast::city)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        Map<String, CityForecast> index = forecasts.stream()
                .collect(Collectors.toMap(
                        f -> key(f.city(), f.date()),
                        f -> f,
                        (first, second) -> first,
                        LinkedHashMap::new));

        int cityWidth = Math.max(CITY_HEADER.length(),
                cities.stream().mapToInt(String::length).max().orElse(0));

        int[] metricWidths = metricWidths(cities, dates, index);

        StringBuilder table = new StringBuilder();
        appendDateHeader(table, dates, cityWidth, metricWidths);
        appendMetricHeader(table, dates, cityWidth, metricWidths);
        appendDivider(table, dates, cityWidth, metricWidths);

        for (String city : cities) {
            appendRow(table, city, dates, index, cityWidth, metricWidths);
        }
        return table.toString();
    }

    private int[] metricWidths(List<String> cities, List<LocalDate> dates,
                               Map<String, CityForecast> index) {
        int[] widths = new int[METRICS.length];
        for (int i = 0; i < METRICS.length; i++) {
            widths[i] = METRICS[i].length();
        }
        for (String city : cities) {
            for (LocalDate date : dates) {
                String[] values = values(index.get(key(city, date)));
                for (int i = 0; i < values.length; i++) {
                    widths[i] = Math.max(widths[i], values[i].length());
                }
            }
        }
        return widths;
    }

    private void appendDateHeader(StringBuilder table, List<LocalDate> dates,
                                  int cityWidth, int[] metricWidths) {
        table.append(pad("", cityWidth));
        for (LocalDate date : dates) {
            table.append(SEPARATOR).append(center(date.format(DATE_HEADER), blockWidth(metricWidths)));
        }
        table.append(System.lineSeparator());
    }

    private void appendMetricHeader(StringBuilder table, List<LocalDate> dates,
                                    int cityWidth, int[] metricWidths) {
        table.append(pad(CITY_HEADER, cityWidth));
        for (int d = 0; d < dates.size(); d++) {
            for (int i = 0; i < METRICS.length; i++) {
                table.append(SEPARATOR).append(padLeft(METRICS[i], metricWidths[i]));
            }
        }
        table.append(System.lineSeparator());
    }

    private void appendDivider(StringBuilder table, List<LocalDate> dates,
                               int cityWidth, int[] metricWidths) {
        int total = cityWidth + dates.size() * (blockWidth(metricWidths) + SEPARATOR.length());
        table.append("-".repeat(total)).append(System.lineSeparator());
    }

    private void appendRow(StringBuilder table, String city, List<LocalDate> dates,
                           Map<String, CityForecast> index, int cityWidth, int[] metricWidths) {
        table.append(pad(city, cityWidth));
        for (LocalDate date : dates) {
            String[] values = values(index.get(key(city, date)));
            for (int i = 0; i < values.length; i++) {
                table.append(SEPARATOR).append(padLeft(values[i], metricWidths[i]));
            }
        }
        table.append(System.lineSeparator());
    }

    private String[] values(CityForecast forecast) {
        if (forecast == null) {
            return new String[]{MISSING, MISSING, MISSING, MISSING, MISSING};
        }
        return new String[]{
                String.format(Locale.US, "%.1f", forecast.minTempC()),
                String.format(Locale.US, "%.1f", forecast.maxTempC()),
                String.format(Locale.US, "%.0f", forecast.humidityPercent()),
                String.format(Locale.US, "%.1f", forecast.windKph()),
                forecast.windDirection()
        };
    }

    private int blockWidth(int[] metricWidths) {
        int width = 0;
        for (int metricWidth : metricWidths) {
            width += metricWidth + SEPARATOR.length();
        }
        return width - SEPARATOR.length();
    }

    private String key(String city, LocalDate date) {
        return city + "|" + date;
    }

    private String pad(String value, int width) {
        return String.format("%-" + width + "s", value);
    }

    private String padLeft(String value, int width) {
        return String.format("%" + width + "s", value);
    }

    private String center(String value, int width) {
        int left = Math.max(0, (width - value.length()) / 2);
        int right = Math.max(0, width - value.length() - left);
        return " ".repeat(left) + value + " ".repeat(right);
    }

}
