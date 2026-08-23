package com.kolia.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ForecastDay(
        @JsonProperty("date") String date,
        @JsonProperty("day") Day day,
        @JsonProperty("hour") List<Hour> hour
) {
}
