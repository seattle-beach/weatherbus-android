package io.pivotal.weatherbus.app.services;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

import java.util.List;

public interface IRetrofitWeatherBusService {
    @GET("/users/stops")
    Observable<List<StopForUserResponse>> getStops(@Query("username") String username);
}