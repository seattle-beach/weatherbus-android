package io.pivotal.weatherbus.app.repositories;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import io.pivotal.weatherbus.app.map.*;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;

public class MapRepository {

    LocationRepository locationRepository;
    private BehaviorSubject<WeatherBusMap> behaviorSubject;

    public MapRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Observable<WeatherBusMap> getOnMapReadyObservable(final MapFragmentAdapter mapFragment) {
        if(behaviorSubject == null) {
            behaviorSubject = create(mapFragment);
        }
        return behaviorSubject;
    }

    public Observable<WeatherBusMarker> getOnMarkerClickObservable(final MapFragmentAdapter mapFragment) {
        if(behaviorSubject == null) {
            behaviorSubject = create(mapFragment);
        }
        return behaviorSubject.flatMap(new Func1<WeatherBusMap, Observable<WeatherBusMarker>>() {
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

    public void reset() {
        behaviorSubject = null;
    }

    private BehaviorSubject<WeatherBusMap> create(final MapFragmentAdapter mapFragment) {
        BehaviorSubject<WeatherBusMap> subject = BehaviorSubject.create();
        Observable.create(new Observable.OnSubscribe<WeatherBusMap>() {
            @Override
            public void call(final Subscriber<? super WeatherBusMap> subscriber) {
                mapFragment.getMapAsync(new OnWeatherBusMapReadyCallback() {
                    @Override
                    public void onMapReady(WeatherBusMap map) {
                        subscriber.onNext(map);
                    }
                });
            }
        }).subscribe(subject);
        return subject;
    }
}
