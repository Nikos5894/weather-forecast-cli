package com.kolia.weather.api;

import com.kolia.weather.api.dto.ForecastResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("v1/forecast.json")
    Call<ForecastResponse> forecast(
            @Query("key") String apiKey,
            @Query("q") String city,
            @Query("days") int days,
            @Query("aqi") String aqi,
            @Query("alerts") String alerts
    );
}