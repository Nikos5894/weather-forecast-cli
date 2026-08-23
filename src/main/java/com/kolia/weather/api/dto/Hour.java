package com.kolia.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Hour(
        @JsonProperty("time") String time,
        @JsonProperty("wind_kph") double windKph,
        @JsonProperty("wind_dir") String windDir
) {

}
