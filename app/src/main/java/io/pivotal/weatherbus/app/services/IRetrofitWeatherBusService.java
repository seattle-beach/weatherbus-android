package io.pivotal.weatherbus.app.services;

import io.pivotal.weatherbus.app.services.response.DeparturesResponse;
import io.pivotal.weatherbus.app.services.response.MultipleStopResponse;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface IRetrofitWeatherBusService {
    @GET("/api/v1/stops")
    Observable<MultipleStopResponse> getStopsForLocation(@Query("lat") double lat,
                                                         @Query("lng") double lng,
                                                         @Query("latSpan") double latSpan,
                                                         @Query("lngSpan") double lngSpan);
    @GET("/api/v1/stops/{stop}")
    Observable<DeparturesResponse> getDepartures(@Path("stop") String stopId);
}