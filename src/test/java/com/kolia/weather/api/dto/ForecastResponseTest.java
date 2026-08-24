package com.kolia.weather.api.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastResponseTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void parsesLocation() throws IOException {
        ForecastResponse response = readFixture("/kyiv_forecast.json");

        assertThat(response.location().name()).isEqualTo("Kyiv");
        assertThat(response.location().localTime()).startsWith("2026-");
    }

    @Test
    void parsesTwoForecastDays() throws IOException {
        ForecastResponse response = readFixture("/kyiv_forecast.json");

        assertThat(response.forecast().forecastday()).hasSize(2);
        assertThat(response.forecast().forecastday().get(1).date()).isNotBlank();
    }

    @Test
    void parsesDailyMetrics() throws IOException {
        ForecastResponse response = readFixture("/kyiv_forecast.json");

        Day day = response.forecast().forecastday().get(1).day();

        assertThat(day.minTempC()).isLessThan(day.maxTempC());
        assertThat(day.avgHumidity()).isBetween(0.0, 100.0);
        assertThat(day.maxWindKph()).isPositive();
    }

    @Test
    void parsesTwentyFourHours() throws IOException {
        ForecastResponse response = readFixture("/kyiv_forecast.json");

        var hours = response.forecast().forecastday().get(1).hour();

        assertThat(hours).hasSize(24);
        assertThat(hours.getFirst().windDir()).isNotBlank();
        assertThat(hours.getFirst().windKph()).isNotNegative();
    }

    private ForecastResponse readFixture(String path) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertThat(stream).as("fixture %s must exist", path).isNotNull();
            return mapper.readValue(stream, ForecastResponse.class);
        }
    }
}
