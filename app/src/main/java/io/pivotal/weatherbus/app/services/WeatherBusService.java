package io.pivotal.weatherbus.app.services;

import rx.Observable;

import java.util.List;

public class WeatherBusService {
    IRetrofitWeatherBusService weatherBusService;

    public WeatherBusService(IRetrofitWeatherBusService weatherBusService) {
        this.weatherBusService = weatherBusService;
    }

    public Observable<List<StopForUserResponse>> getStopIds(String username) {
        return weatherBusService.getStops(username);
    }
}
