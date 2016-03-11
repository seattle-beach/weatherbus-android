package io.pivotal.weatherbus.app.services;

import rx.Observable;

public class WeatherBusService {
    IRetrofitWeatherBusService weatherBusService;

    public WeatherBusService(IRetrofitWeatherBusService weatherBusService) {
        this.weatherBusService = weatherBusService;
    }

    public Observable<StopForLocationResponse> getStopsForLocation(double latitude,
                                                                         double longitude,
                                                                         double latitudeSpan,
                                                                         double longitudeSpan) {
        return weatherBusService.getStopsForLocation(latitude, longitude, latitudeSpan, longitudeSpan);
    }

    public Observable<StopResponse> getStopInformation(String stopId) {
        return weatherBusService.getDepartures(stopId);
    }
}
