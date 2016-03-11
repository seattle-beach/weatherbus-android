package io.pivotal.weatherbus.app.services;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface IRetrofitWeatherBusService {
    @GET("/api/v1/stops")
    Observable<StopForLocationResponse> getStopsForLocation(@Query("lat") double lat,
                                                                  @Query("lng") double lng,
                                                                  @Query("latSpan") double latSpan,
                                                                  @Query("lngSpan") double lngSpan);
    @GET("/api/v1/stops/{stop}")
    Observable<StopResponse> getDepartures(@Path("stop") String stopId);
}