package io.pivotal.weatherbus.app.repositories;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import io.pivotal.weatherbus.app.GoogleMapWrapper;
import io.pivotal.weatherbus.app.MapActivity;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;

public class MapRepository {

    LocationRepository locationRepository;

    public MapRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Observable<GoogleMapWrapper> create(final MapFragment fragment, Context context) {
        Observable<GoogleMapWrapper> googleMap = createMap(fragment);
        Observable<Location> location = locationRepository.create(context);
        return Observable.zip(googleMap, location, new Func2<GoogleMapWrapper, Location, GoogleMapWrapper>() {
            @Override
            public GoogleMapWrapper call(GoogleMapWrapper googleMapWrapper, Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMapWrapper.moveCamera(latLng);
                return googleMapWrapper;
            }
        });
    }

    public Observable<GoogleMapWrapper> createMap(final MapFragment mapFragment) {
        return Observable.create(new Observable.OnSubscribe<GoogleMapWrapper>() {
            @Override
            public void call(final Subscriber<? super GoogleMapWrapper> subscriber) {
                OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
                    public void onMapReady(GoogleMap googleMap) {
                        subscriber.onNext(new GoogleMapWrapper(googleMap));
                    }
                };
                mapFragment.getMapAsync(mapReadyCallback);
            }
        });
    }
}
