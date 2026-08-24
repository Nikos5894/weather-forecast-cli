# WeatherAPI.com Code Challenge — next-day forecast

A console app that fetches tomorrow's forecast from [WeatherAPI.com](https://www.weatherapi.com/)
for Chisinau, Madrid, Kyiv and Amsterdam and prints it as a table.

```
                         Tue, 25 Aug 2026
City       Min C  Max C  Humidity %  Wind kph  Wind dir
-------------------------------------------------------
Amsterdam   11.8   25.8          56      20.5         E
Chisinau    15.9   30.1          53      21.2       SSE
Kyiv        14.0   24.3          39      10.8        NW
Madrid      15.0   25.3          49      22.0        SW
```

## Stack

- Java 21
- Gradle (Kotlin DSL) with the `application` plugin
- Retrofit 2 + OkHttp for HTTP
- Jackson for JSON, with Java `record` classes as DTOs
- JUnit 5 + AssertJ for tests

I used `record` instead of Lombok because it is part of the language and needs no
extra dependency or IDE plugin.

## How to run

You need a free API key from WeatherAPI.com. The app reads it from an environment
variable, so the key is not stored in the code or in git.

```bash
# Linux / macOS
export WEATHER_API_KEY=your_key_here
./gradlew run
```

```powershell
# Windows PowerShell
setx WEATHER_API_KEY "your_key_here"   # then open a new terminal
.\gradlew.bat run
```

You can also pass your own cities:

```bash
./gradlew run --args="Lisbon Warsaw Tbilisi"
```

The Gradle wrapper is in the repository, so Gradle does not need to be installed.

```bash
./gradlew test
./gradlew build
```

Exit codes: `0` on success, `1` if the API call fails, `2` if the API key is missing.
The table is printed to STDOUT and errors to STDERR.

## Project structure

```
com.kolia.weather
├── Main.java                      reads the key, wires everything, prints the table
├── api/
│   ├── WeatherApi.java            Retrofit interface
│   ├── WeatherApiFactory.java     builds Retrofit, OkHttp and Jackson
│   └── dto/                       classes that match the JSON response
│       ├── ForecastResponse.java
│       ├── Location.java
│       ├── Forecast.java
│       ├── ForecastDay.java
│       ├── Day.java
│       └── Hour.java
├── domain/
│   ├── CityForecast.java          one row of the table
│   ├── ForecastMapper.java        converts DTO to CityForecast
│   ├── WindDirection.java         finds the wind direction for a day
│   └── ForecastService.java       requests all cities in parallel
└── render/
    └── TableFormatter.java        builds the table as a String
```

I split the code into three parts. The `api` package knows about HTTP and about how
WeatherAPI names its fields. The `domain` package only knows about weather. The
`render` package only builds text and does not print anything itself, which makes it
easy to test.

The DTOs only declare the 16 fields I actually use. The response has about a hundred
fields, so Jackson is configured to ignore the ones I did not declare.

## Notes on the implementation

**Finding tomorrow.** The cities are in three different time zones, so "tomorrow" is
not the same moment everywhere. Instead of taking `forecastday[1]`, I read the city's
own local time from the response, add one day, and look for the forecast day with that
date. If it is not there, the app throws an error instead of showing the wrong day.

**Wind direction.** The API does not include a wind direction in the daily summary —
only in the hourly data. So I take the direction that appears most often in the 24
hourly values. If there is no hourly data, the app prints `n/a`.

This is not perfect. If the wind changes a lot during the day, the most common
direction may not match the hour when the maximum wind speed happened. I also looked
at taking the direction of the windiest hour, and at averaging the wind angles, but
the most common direction was the simplest to explain and gives a reasonable answer
for a normal day.

**Wind speed** is `maxwind_kph`, which is the only daily wind speed the API provides.

**Parallel requests.** The four cities are requested at the same time using virtual
threads (`Executors.newVirtualThreadPerTaskExecutor()`). Each request is a normal
blocking call, which keeps the code simple to read. If more than one city fails, all
errors are reported, not just the first one.

**Table formatting.** Column widths are calculated from the data, and the code loops
over all dates, so the table still works if more than one day is requested. I used
`Locale.US` in `String.format` so that numbers are printed as `25.8` and not `25,8`
on machines with a different locale.

## Tests

`ForecastResponseTest` checks that a saved API response is parsed correctly into the
DTO classes: the city name, the number of days, the daily values and the 24 hours.

The test checks things that stay true no matter what the weather is — minimum
temperature below maximum, humidity between 0 and 100, exactly 24 hours — so it does
not break when the saved forecast gets old. It still catches the main mistake I was
worried about: a wrong field name in `@JsonProperty`, which would leave a value at
`0.0` or `null`.

## What I would add next

- Tests for the mapper (time zones) and for the table formatter
- Retry when the API returns a server error
- CSV and JSON output options
