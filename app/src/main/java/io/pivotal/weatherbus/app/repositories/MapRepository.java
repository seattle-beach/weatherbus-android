package io.pivotal.weatherbus.app.repositories;

import android.app.Activity;
import android.location.Location;
import android.widget.ListView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import io.pivotal.weatherbus.app.*;
import io.pivotal.weatherbus.app.map.*;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

public class MapRepository {

    LocationRepository locationRepository;
    private Observable<WeatherBusMap> cache;

    public MapRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Observable<WeatherBusMap> getOnMapReadyObservable(final MapFragmentAdapter mapFragment) {
        if(cache == null) {
            create(mapFragment);
        }
        return cache;
    }

    public Observable<WeatherBusMap> getOnCenteredMapObservable(final MapFragmentAdapter mapFragment, Observable<Location> location) {
        if(cache == null) {
            create(mapFragment);
        }

        return Observable.zip(cache, location, new Func2<WeatherBusMap, Location, WeatherBusMap>() {
            @Override
            public WeatherBusMap call(WeatherBusMap googleMap, Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                ListView stops = (ListView) activity.findViewById(R.id.stopList);
//                googleMap.setPadding(0, 0 ,0, stops.getTop());
                googleMap.moveCamera(latLng);
                return googleMap;
            }
        });
    }

    public Observable<WeatherBusMarker> getOnMarkerClickObservable(final MapFragmentAdapter mapFragment) {
        if(cache == null) {
            create(mapFragment);
        }
        return cache.flatMap(new Func1<WeatherBusMap, Observable<WeatherBusMarker>>() {
            @Override
            public Observable<WeatherBusMarker> call(final WeatherBusMap weatherBusMap) {
                return Observable.create(new Observable.OnSubscribe<WeatherBusMarker>() {
                    @Override
                    public void call(final Subscriber<? super WeatherBusMarker> subscriber) {
                        weatherBusMap.setOnMarkerClickListener(new OnWeatherBusMarkerClick() {
                            @Override
                            public boolean onMarkerClick(WeatherBusMarker marker) {
                                subscriber.onNext(marker);
                                return false;
                            }
                        });
                    }
                });
            }
        });
    }

    private void create(final MapFragmentAdapter mapFragment) {
       cache = Observable.create(new Observable.OnSubscribe<WeatherBusMap>() {
           @Override
           public void call(final Subscriber<? super WeatherBusMap> subscriber) {
               mapFragment.getMapAsync(new OnWeatherBusMapReadyCallback() {
                   @Override
                   public void onMapReady(WeatherBusMap map) {
                       subscriber.onNext(map);
                   }
               });
           }
       });
    }
}
