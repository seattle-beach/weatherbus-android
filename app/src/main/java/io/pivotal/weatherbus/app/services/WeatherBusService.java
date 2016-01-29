package io.pivotal.weatherbus.app.services;

import rx.Observable;

import java.util.List;

public class WeatherBusService {
    IRetrofitWeatherBusService weatherBusService;

    public WeatherBusService(IRetrofitWeatherBusService weatherBusService) {
        this.weatherBusService = weatherBusService;
    }

    public Observable<List<StopForUserResponse>> getStopForUser(String username) {
        return weatherBusService.getStopsForUser(username);
    }

    public Observable<List<StopForLocationResponse>> getStopsForLocation(double latitude,
                                                                         double longitude,
                                                                         double latitudeSpan,
                                                                         double longitudeSpan) {
        return weatherBusService.getStopsForLocation(latitude, longitude, latitudeSpan, longitudeSpan);
    }
}
