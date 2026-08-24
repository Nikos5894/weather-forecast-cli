package com.kolia.weather;

import com.kolia.weather.api.WeatherApi;
import com.kolia.weather.api.WeatherApiFactory;
import com.kolia.weather.domain.CityForecast;
import com.kolia.weather.domain.ForecastMapper;
import com.kolia.weather.domain.ForecastService;
import com.kolia.weather.render.TableFormatter;

import java.util.List;

public final class Main {

    private static final String API_KEY_ENV = "WEATHER_API_KEY";
    private static final List<String> DEFAULT_CITIES =
            List.of("Chisinau", "Madrid", "Kyiv", "Amsterdam");

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_BAD_CONFIG = 2;

    public static void main(String[] args) {
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Environment variable " + API_KEY_ENV + " is not set.");
            System.err.println("Get a free key at https://www.weatherapi.com/ and export it.");
            System.exit(EXIT_BAD_CONFIG);
            return;
        }

        List<String> cities = args.length > 0 ? List.of(args) : DEFAULT_CITIES;

        WeatherApi api = WeatherApiFactory.create();
        ForecastService service = new ForecastService(api, new ForecastMapper(), apiKey);
        TableFormatter formatter = new TableFormatter();

        try {
            List<CityForecast> forecasts = service.forecastFor(cities);
            System.out.println(formatter.format(forecasts));
            System.exit(EXIT_SUCCESS);
        } catch (RuntimeException e) {
            System.err.println("Failed to fetch forecasts: " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.err.println("  also: " + suppressed.getMessage());
            }
            System.exit(EXIT_FAILURE);
        }
    }
}