package io.pivotal.weatherbus.app.repositories;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class WeatherBusMapRepository {

    private BehaviorSubject<WeatherBusMap> behaviorSubject;
    private boolean isCacheValid = false;

    public Observable<WeatherBusMap> getOnMapReadyObservable(final MapFragmentAdapter mapFragment) {
        if(behaviorSubject == null || !isCacheValid) {
            behaviorSubject = create(mapFragment);
            isCacheValid = true;
        }
        return behaviorSubject;
    }

    public Observable<WeatherBusMarker> getOnMarkerClickObservable(final MapFragmentAdapter mapFragment) {
        if(behaviorSubject == null || !isCacheValid) {
            behaviorSubject = create(mapFragment);
            isCacheValid = true;
        }
        return behaviorSubject.flatMap(new MarkerClickFunction());
    }

    public Observable<WeatherBusMarker> getOnInfoWindowClickObservable(final MapFragmentAdapter mapFragment) {
        if (behaviorSubject == null || !isCacheValid) {
            behaviorSubject = create(mapFragment);
            isCacheValid = true;
        }
        return behaviorSubject.flatMap(new InfoWindowClickFunction());
    }

    public Observable<LatLngBounds> getOnCameraChangeObservable(final MapFragmentAdapter mapFragment) {
        if (behaviorSubject == null || !isCacheValid) {
            behaviorSubject = create(mapFragment);
            isCacheValid = true;
        }
        return behaviorSubject.flatMap(new CameraChangeFunction());
    }

    public void reset() {
        isCacheValid = false;
    }

    private BehaviorSubject<WeatherBusMap> create(final MapFragmentAdapter mapFragment) {
        BehaviorSubject<WeatherBusMap> subject = BehaviorSubject.create();
        Observable.create(new Observable.OnSubscribe<WeatherBusMap>() {
            @Override
            public void call(final Subscriber<? super WeatherBusMap> subscriber) {
                mapFragment.getMapAsync(new WeatherBusMap.OnWeatherBusMapReadyCallback() {
                    @Override
                    public void onMapReady(WeatherBusMap weatherBusMap) {
                        subscriber.onNext(weatherBusMap);
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
                    weatherBusMap.setOnMarkerClickListener(new WeatherBusMap.OnMarkerClickListener() {
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
                    weatherBusMap.setOnInfoWindowClickListener(new WeatherBusMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(WeatherBusMarker marker) {
                            subscriber.onNext(marker);
                        }
                    });
                }
            }));
        }
    }

    private class CameraChangeFunction implements Func1<WeatherBusMap, Observable<LatLngBounds>> {
        @Override
        public Observable<LatLngBounds> call(final WeatherBusMap weatherBusMap) {
            return Observable.create(new Observable.OnSubscribe<LatLngBounds>() {
                @Override
                public void call(final Subscriber<? super LatLngBounds> subscriber) {
                    weatherBusMap.setOnCameraChangeListener(new WeatherBusMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition position) {
                            subscriber.onNext(weatherBusMap.getLatLngBounds());
                        }
                    });
                }
            });
        }
    }
}
