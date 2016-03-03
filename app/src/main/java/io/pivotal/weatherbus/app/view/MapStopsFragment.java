package io.pivotal.weatherbus.app.view;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.SavedStops;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapStopsFragment extends Fragment {
    @Inject WeatherBusMapRepository weatherBusMapRepository;
    @Inject LocationRepository locationRepository;
    @Inject SavedStops favoriteStops;
    @Inject WeatherBusService service;

    private MapFragmentAdapter mapFragmentAdapter;
    private MapFragment mapFragment;
    private CompositeSubscription subscriptions;
    private BusStop selectedStop;
    private WeatherBusMap weatherBusMap;
    private Map<BusStop, WeatherBusMarker> busStopMarkers;
    private View view;

    public MapStopsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            WeatherBusApplication.inject(this);
            busStopMarkers = new HashMap<>();

            initializeMap();
            initializeSubscriptions();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_map_stops, container, false);
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.mapContainer, mapFragment)
                    .commit();
        }
        return view;
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        weatherBusMapRepository.reset();
        super.onDestroy();
    }

    private void initializeMap() {
        mapFragment = MapFragment.newInstance(new GoogleMapOptions()
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false)
                .camera(new CameraPosition.Builder()
                        .zoom(15)
                        .target(new LatLng(47.5989625,-122.3359992))
                        .build()));

        mapFragmentAdapter = new MapFragmentAdapter(mapFragment);
    }

    private void initializeSubscriptions() {

        subscriptions = new CompositeSubscription();

        subscriptions.add(weatherBusMapRepository.getOnMarkerClickObservable(mapFragmentAdapter)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextMarkerClick()));

        subscriptions.add(weatherBusMapRepository.getOnMapReadyObservable(mapFragmentAdapter)
                .doOnNext(new Action1<WeatherBusMap>() {
                    @Override
                    public void call(WeatherBusMap weatherBusMap) {
                        weatherBusMap.setMyLocationEnabled(true);
                        MapStopsFragment.this.weatherBusMap = weatherBusMap;
                    }
                }).zipWith(locationRepository.fetch(getActivity()), new Func2<WeatherBusMap, Location, LatLngBounds>() {
                    @Override
                    public LatLngBounds call(WeatherBusMap weatherBusMap, Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        weatherBusMap.moveCamera(latLng);
                        return weatherBusMap.getLatLngBounds();
                    }
                }).mergeWith(weatherBusMapRepository.getOnCameraChangeObservable(mapFragmentAdapter))
                .flatMap(new StopsFromLatLngBoundsFunction())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StopForLocationResponsesSubscriber()));
    }

    private class OnNextMarkerClick implements Action1<WeatherBusMarker> {
        @Override
        public void call(WeatherBusMarker weatherBusMarker) {
            selectedStop = findBusStop(weatherBusMarker);
            if (selectedStop != null) {
                ((FragmentListener) getActivity()).onStopSelected(selectedStop);
            }
        }
    }

    private class StopForLocationResponsesSubscriber extends Subscriber<StopForLocationResponse> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getActivity(), "Failed to get stops near location!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(StopForLocationResponse stopForLocationResponse) {
            WeatherBusMarker selectedMarker = busStopMarkers.get(selectedStop);
            for (WeatherBusMarker marker : busStopMarkers.values()) {
                if (marker != selectedMarker) {
                    marker.remove();
                }
            }
            busStopMarkers.clear();
            if (selectedMarker != null) {
                busStopMarkers.put(selectedStop, selectedMarker);
            }
            List<String> favoriteStops = MapStopsFragment.this.favoriteStops.getSavedStops();
            for (StopForLocationResponse.BusStopResponse stopResponse : stopForLocationResponse.getStops()) {
                BusStop busStop;
                WeatherBusMarker marker;
                if (selectedStop != null && selectedStop.getId().equals(stopResponse.getId())) {
                    busStop = selectedStop;
                    marker = selectedMarker;
                } else {
                    busStop = new BusStop(stopResponse);
                    boolean isFavorite = favoriteStops.contains(stopResponse.getId());
                    busStop.setFavorite(isFavorite);
                    LatLng stopPosition = new LatLng(stopResponse.getLatitude(),stopResponse.getLongitude());
                    marker = weatherBusMap.addMarker(new MarkerOptions()
                            .position(stopPosition)
                            .title(busStop.getName()));
                    marker.setFavorite(isFavorite);
                }
                busStopMarkers.put(busStop,marker);
            }
            ((FragmentListener) getActivity()).onStopsLoaded();
        }
    }

    public void setSelectedFavorite(boolean isFavorite) {
        busStopMarkers.get(selectedStop).setFavorite(isFavorite);
    }

    private class StopsFromLatLngBoundsFunction implements Func1<LatLngBounds, Observable<StopForLocationResponse>> {
        @Override
        public Observable<StopForLocationResponse> call(LatLngBounds bounds) {
            LatLng center = bounds.getCenter();
            double latitudeSpan = bounds.northeast.latitude - bounds.southwest.latitude;
            double longitudeSpan = bounds.northeast.longitude - bounds.southwest.longitude;
            return service.getStopsForLocation(center.latitude, center.longitude,
                    latitudeSpan, longitudeSpan);
        }
    }

    private BusStop findBusStop(WeatherBusMarker weatherBusMarker) {
        for(Map.Entry<BusStop, WeatherBusMarker> entry : busStopMarkers.entrySet()) {
            if (entry.getValue() == weatherBusMarker) {
                return entry.getKey();
            }
        }
        return null;
    }

    public interface FragmentListener {
        void onStopsLoaded();
        void onStopSelected(BusStop busStop);
    }
}
