package com.kolia.weather.domain;

import java.time.LocalDate;

public record CityForecast(
        String city,
        LocalDate date,
        double minTempC,
        double maxTempC,
        double humidityPercent,
        double windKph,
        String windDirection
) {

}
