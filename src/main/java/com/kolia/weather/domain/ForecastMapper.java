package com.kolia.weather.domain;

import com.kolia.weather.api.dto.ForecastDay;
import com.kolia.weather.api.dto.ForecastResponse;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Converts the wire model into the domain model, resolving "tomorrow" per city. */
public final class ForecastMapper {

    private static final int LOCAL_DATE_LENGTH = 10;

    public CityForecast toTomorrowForecast(ForecastResponse response) {
        LocalDate tomorrow = cityLocalDate(response).plusDays(1);
        ForecastDay day = findDay(response, tomorrow);

        return new CityForecast(
                response.location().name(),
                tomorrow,
                day.day().minTempC(),
                day.day().maxTempC(),
                day.day().avgHumidity(),
                day.day().maxWindKph(),
                WindDirection.prevailing(day.hour())
        );
    }

    private LocalDate cityLocalDate(ForecastResponse response) {
        String localTime = response.location().localTime();
        if (localTime == null || localTime.length() < LOCAL_DATE_LENGTH) {
            throw new IllegalStateException("Unexpected localtime format: " + localTime);
        }
        try {
            return LocalDate.parse(localTime.substring(0, LOCAL_DATE_LENGTH));
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Cannot parse localtime: " + localTime, e);
        }
    }

    private ForecastDay findDay(ForecastResponse response, LocalDate target) {
        return response.forecast().forecastday().stream()
                .filter(day -> target.toString().equals(day.date()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No forecast for " + target + " in " + response.location().name()));
    }
}