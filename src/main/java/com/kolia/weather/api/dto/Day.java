package com.kolia.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Day(
        @JsonProperty("mintemp_c") double minTempC,
        @JsonProperty("maxtemp_c") double maxTempC,
        @JsonProperty("avghumidity") double avgHumidity,
        @JsonProperty("maxwind_kph") double maxWindKph
) {
}
