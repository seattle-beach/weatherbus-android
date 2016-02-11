package io.pivotal.weatherbus.app.repositories;

import io.pivotal.weatherbus.app.map.*;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class MapRepository {

    LocationRepository locationRepository;
    private BehaviorSubject<WeatherBusMap> behaviorSubject;
    private boolean isCacheValid = false;

    public MapRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Observable<WeatherBusMap> getOnMapReadyObservable(final MapFragmentAdapter mapFragment) {
        if(behaviorSubject == null || !isCacheValid) {
            behaviorSubject = create(mapFragment);
            isCacheValid = true;
        }
        return behaviorSubject;
    }

    public Observable<WeatherBusMarker> getOnMarkerClickObservable(final MapFragmentAdapter mapFragment) {
        if(behaviorSubject == null) {
            behaviorSubject = create(mapFragment);
        }
        return behaviorSubject.flatMap(new MarkerClickFunction());
    }

    public Observable<WeatherBusMarker> getOnInfoWindowClickObservable(final MapFragmentAdapter mapFragment) {
        if (behaviorSubject == null) {
            behaviorSubject = create(mapFragment);
        }

        return behaviorSubject.flatMap(new InfoWindowClickFunction());
    }

    public void reset() {
        isCacheValid = false;
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

    private class MarkerClickFunction implements Func1<WeatherBusMap, Observable<WeatherBusMarker>> {
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
    }

    private class InfoWindowClickFunction implements Func1<WeatherBusMap, Observable<WeatherBusMarker>> {
        @Override
        public Observable<WeatherBusMarker> call(final WeatherBusMap weatherBusMap) {
            return Observable.create((new Observable.OnSubscribe<WeatherBusMarker>() {
                @Override
                public void call(final Subscriber<? super WeatherBusMarker> subscriber) {
                    weatherBusMap.setOnInfoWindowClickListener(new OnWeatherBusInfoClickListener() {
                        @Override
                        public void onInfoWindowClick(WeatherBusMarker marker) {
                            subscriber.onNext(marker);
                        }
                    });
                }
            }));
        }
    }
}
