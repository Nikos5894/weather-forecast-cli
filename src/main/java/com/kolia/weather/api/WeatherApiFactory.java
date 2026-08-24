package com.kolia.weather.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

public class WeatherApiFactory {

    private static final String BASE_URL = "https://api.weatherapi.com/";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private WeatherApiFactory() {
    }

    public static WeatherApi create() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build()
                .create(WeatherApi.class);
    }
}
