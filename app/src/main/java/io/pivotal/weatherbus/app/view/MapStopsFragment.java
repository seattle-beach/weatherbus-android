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
import com.google.android.gms.maps.model.*;
import com.google.common.base.Joiner;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.adapter.InfoContentsAdapter;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.model.MarkerImageOptions;
import io.pivotal.weatherbus.app.repositories.FavoriteStopsRepository;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.MarkerIconRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.services.response.MultipleStopResponse;
import io.pivotal.weatherbus.app.services.response.RouteReference;
import io.pivotal.weatherbus.app.services.response.StopResponse;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapStopsFragment extends Fragment {
    @Inject WeatherBusMapRepository weatherBusMapRepository;
    @Inject LocationRepository locationRepository;
    @Inject
    FavoriteStopsRepository favoriteStops;
    @Inject MarkerIconRepository markerIconRepository;
    @Inject WeatherBusService service;
    @Inject
    InfoContentsAdapter infoContentsAdapter;

    private MapFragmentAdapter mapFragmentAdapter;
    private MapFragment mapFragment;
    private CompositeSubscription subscriptions;
    private BusStop selectedStop;
    private WeatherBusMap weatherBusMap;
    private Map<BusStop, WeatherBusMarker> busStopMarkers;
    private View fragmentView;

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
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_map_stops, container, false);
            infoContentsAdapter.setContext(getActivity());
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.mapContainer, mapFragment)
                    .commit();
        }
        return fragmentView;
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
                        .zoom(16)
                        .target(new LatLng(47.5989625,-122.3359992))
                        .build()));

        mapFragmentAdapter = new MapFragmentAdapter(mapFragment);
    }

    private void initializeSubscriptions() {

        subscriptions = new CompositeSubscription();

        Observable<WeatherBusMap> onMapReady = weatherBusMapRepository.getOnMapReadyObservable(mapFragmentAdapter);
        Observable<LatLngBounds> onCameraChange = weatherBusMapRepository.getOnCameraChangeObservable(mapFragmentAdapter);
        Observable<WeatherBusMarker> onMarkerClick = weatherBusMapRepository.getOnMarkerClickObservable(mapFragmentAdapter);

        subscriptions.add(onMapReady
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WeatherBusMap>() {
                    @Override
                    public void call(WeatherBusMap weatherBusMap) {
                        weatherBusMap.setMyLocationEnabled(true);
                        weatherBusMap.setInfoWindowAdapter(infoContentsAdapter);
                    }
                }));

        subscriptions.add(onMarkerClick
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextMarkerClick()));


        subscriptions.add(onMapReady
                .doOnNext(new Action1<WeatherBusMap>() {
                    @Override
                    public void call(WeatherBusMap weatherBusMap) {
                        MapStopsFragment.this.weatherBusMap = weatherBusMap;
                    }
                }).zipWith(locationRepository.fetch(getActivity()), new Func2<WeatherBusMap, Location, LatLngBounds>() {
                    @Override
                    public LatLngBounds call(WeatherBusMap weatherBusMap, Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        weatherBusMap.moveCamera(latLng);
                        return weatherBusMap.getLatLngBounds();
                    }
                }).mergeWith(onCameraChange)
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

    private class StopForLocationResponsesSubscriber extends Subscriber<MultipleStopResponse> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getActivity(), "Failed to get stops near location!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(MultipleStopResponse multipleStopResponse) {
            Map<String, String> routeNames = new HashMap<>();
            for(RouteReference route : multipleStopResponse.getIncluded().getRoutes()) {
                String name;
                if (!route.getShortName().isEmpty()) {
                    name = route.getShortName();
                } else if (!route.getLongName().isEmpty()) {
                    name = route.getLongName();
                } else {
                    name = route.getId();
                }
                routeNames.put(route.getId(), name);
            }

            List<String> favoriteStops = MapStopsFragment.this.favoriteStops.getSavedStops();
            for (StopResponse stopResponse : multipleStopResponse.getStops()) {
                BusStop busStop = new BusStop(stopResponse);
                if (!busStopMarkers.containsKey(busStop)) {
                    String title = createLabelTitle(busStop);
                    String snippet = createLabelSnippet(busStop, routeNames);
                    boolean isFavorite = favoriteStops.contains(stopResponse.getId());
                    busStop.setFavorite(isFavorite);
                    LatLng position = new LatLng(stopResponse.getLatitude(),stopResponse.getLongitude());
                    BitmapDescriptor icon = markerIconRepository.get(new MarkerImageOptions(busStop.getDirection(), busStop.isFavorite()));
                    WeatherBusMarker marker = weatherBusMap.addMarker(new MarkerOptions()
                            .position(position)
                            .snippet(snippet)
                            .icon(icon)
                            .title(title));
                    busStopMarkers.put(busStop,marker);
                }
            }
            ((FragmentListener) getActivity()).onStopsLoaded();
        }

        private String createLabelSnippet(BusStop busStop, Map<String, String> routeNames) {
            List<String> routes = new ArrayList<>();
            for (String routeId : busStop.getRouteIds()) {
                routes.add(routeNames.get(routeId));
            }
            return "Routes: " + Joiner.on(", ").join(routes);
        }

        private String createLabelTitle(BusStop busStop) {
            String title = busStop.getName();
            if (!busStop.getDirection().isEmpty()) {
                title += " (" + busStop.getDirection() + ")";
            }
            return title;
        }
    }

    public void setSelectedFavorite(boolean isFavorite) {
        BitmapDescriptor icon = markerIconRepository.get(new MarkerImageOptions(selectedStop.getDirection(), isFavorite));
        busStopMarkers.get(selectedStop).setIcon(icon);
    }

    private class StopsFromLatLngBoundsFunction implements Func1<LatLngBounds, Observable<MultipleStopResponse>> {
        @Override
        public Observable<MultipleStopResponse> call(LatLngBounds bounds) {
            List<WeatherBusMarker> removedMarkers = new ArrayList<>();
            WeatherBusMarker selectedMarker = busStopMarkers.get(selectedStop);
            for (WeatherBusMarker marker : busStopMarkers.values()) {
                if (!bounds.contains(marker.getPosition()) && marker != selectedMarker) {
                    marker.remove();
                    removedMarkers.add(marker);
                }
            }
            for (WeatherBusMarker marker : removedMarkers) {
                busStopMarkers.values().remove(marker);
            }
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
}
