package io.pivotal.weatherbus.app.services;

import io.pivotal.weatherbus.app.services.response.DeparturesResponse;
import io.pivotal.weatherbus.app.services.response.MultipleStopResponse;
import rx.Observable;

public class WeatherBusService {
    IRetrofitWeatherBusService weatherBusService;

    public WeatherBusService(IRetrofitWeatherBusService weatherBusService) {
        this.weatherBusService = weatherBusService;
    }

    public Observable<MultipleStopResponse> getStopsForLocation(double latitude,
                                                                double longitude,
                                                                double latitudeSpan,
                                                                double longitudeSpan) {
        return weatherBusService.getStopsForLocation(latitude, longitude, latitudeSpan, longitudeSpan);
    }

    public Observable<DeparturesResponse> getStopInformation(String stopId) {
        return weatherBusService.getDepartures(stopId);
    }
}
