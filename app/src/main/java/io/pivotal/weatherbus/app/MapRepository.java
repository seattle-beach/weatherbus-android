package io.pivotal.weatherbus.app;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import rx.Observable;
import rx.Subscriber;

public class MapRepository {
    public Observable<GoogleMap> create(final MapFragment mapFragment) {
        return Observable.create(new Observable.OnSubscribe<GoogleMap>() {
            @Override
            public void call(final Subscriber<? super GoogleMap> subscriber) {
                OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
                    public void onMapReady(GoogleMap googleMap) {
                        subscriber.onNext(googleMap);
                    }
                };
                mapFragment.getMapAsync(mapReadyCallback);
            }
        });
    }
}
