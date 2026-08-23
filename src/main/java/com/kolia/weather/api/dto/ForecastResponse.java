package com.kolia.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ForecastResponse(
        @JsonProperty("location") Location location,
        @JsonProperty("forecast") Forecast forecast
) {
}
