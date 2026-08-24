package com.kolia.weather.domain;

import com.kolia.weather.api.WeatherApi;
import com.kolia.weather.api.dto.ForecastResponse;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.*;

public class ForecastService {

    private static final int FORECAST_DAYS = 2;

    private final WeatherApi api;
    private final ForecastMapper mapper;
    private final String apiKey;

    public ForecastService(WeatherApi api, ForecastMapper mapper, String apiKey) {
        this.api = api;
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    public List<CityForecast> forecastFor(List<String> cities) {
        try (ExecutorService executor = newVirtualThreadPerTaskExecutor()) {
            List<Future<CityForecast>> futures = cities.stream()
                    .map(city -> executor.submit(() -> fetchCity(city)))
                    .toList();

            return collect(futures);
        }
    }

    private List<CityForecast> collect(List<Future<CityForecast>> futures) {
        List<CityForecast> forecasts = new ArrayList<>();
        RuntimeException failure = null;

        for (Future<CityForecast> future : futures) {
            try {
                forecasts.add(future.get());
            } catch (ExecutionException e) {
                RuntimeException current = new IllegalStateException(e.getCause().getMessage(), e.getCause());
                if (failure == null) {
                    failure = current;
                } else {
                    failure.addSuppressed(current);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while fetching forecasts", e);
            }
        }

        if (failure != null) {
            throw failure;
        }
        return forecasts;
    }

    private CityForecast fetchCity(String city) throws IOException {
        Response<ForecastResponse> response =
                api.forecast(apiKey, city, FORECAST_DAYS, "no", "no").execute();

        if (!response.isSuccessful()) {
            throw new IOException("API returned HTTP " + response.code() + " for " + city);
        }
        ForecastResponse body = response.body();
        if (body == null) {
            throw new IOException("Empty response body for " + city);
        }
        return mapper.toTomorrowForecast(body);
    }
}

