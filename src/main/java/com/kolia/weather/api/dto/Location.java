package com.kolia.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Location(
        @JsonProperty("name") String name,
        @JsonProperty("localtime") String localTime
) {
}
