package io.pivotal.weatherbus.app.repositories;

import android.app.Activity;
import android.location.Location;
import android.widget.ListView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.WeatherBusMap;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;

public class MapRepository {

    LocationRepository locationRepository;

    public MapRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Observable<WeatherBusMap> create(final MapFragment fragment, final Activity activity) {
        Observable<WeatherBusMap> googleMap = createMap(fragment);
        Observable<Location> location = locationRepository.create(activity);
        return Observable.zip(googleMap, location, new Func2<WeatherBusMap, Location, WeatherBusMap>() {
            @Override
            public WeatherBusMap call(WeatherBusMap googleMap, Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                ListView stops = (ListView) activity.findViewById(R.id.stopList);
                googleMap.setPadding(0, 0 ,0, stops.getTop());
                googleMap.moveCamera(latLng);
                return googleMap;
            }
        });
    }

    public Observable<WeatherBusMap> createMap(final MapFragment mapFragment) {
        return Observable.create(new Observable.OnSubscribe<WeatherBusMap>() {
            @Override
            public void call(final Subscriber<? super WeatherBusMap> subscriber) {
                OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
                    public void onMapReady(GoogleMap googleMap) {
                        subscriber.onNext(new WeatherBusMap(googleMap));
                    }
                };
                mapFragment.getMapAsync(mapReadyCallback);
            }
        });
    }
}
