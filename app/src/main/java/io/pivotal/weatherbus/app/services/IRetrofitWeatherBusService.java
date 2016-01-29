package io.pivotal.weatherbus.app.services;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

import java.util.List;

public interface IRetrofitWeatherBusService {
    @GET("/users/stops")
    Observable<List<StopForUserResponse>> getStopsForUser(@Query("username") String username);

    @GET("/buses/stops")
    Observable<List<StopForLocationResponse>> getStopsForLocation(@Query("lat") double lat,
                                                                  @Query("lng") double lng,
                                                                  @Query("latSpan") double latSpan,
                                                                  @Query("lngSpan") double lngSpan);
}