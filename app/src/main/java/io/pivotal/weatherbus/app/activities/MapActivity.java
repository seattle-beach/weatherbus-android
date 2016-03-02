package io.pivotal.weatherbus.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.maps.MapFragment;
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
import rx.subscriptions.Subscriptions;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends Activity {
    MapFragmentAdapter mapFragment;
    private CompositeSubscription subscriptions;

    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.toolbar_title) TextView toolbarTitle;
    @Bind(R.id.bus_info) View toolbarInfo;
    @Bind(R.id.toolbar_favorite_button) ImageButton favoriteButton;

    @Inject WeatherBusService service;
    @Inject SavedStops favoriteStops;
    @Inject WeatherBusMapRepository weatherBusMapRepository;
    @Inject LocationRepository locationRepository;

    private WeatherBusMap weatherBusMap;
    private Map<BusStop, WeatherBusMarker> busStopMarkers;

    private BusStop selectedStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        WeatherBusApplication.inject(this);

        subscriptions = Subscriptions.from();
        busStopMarkers = new HashMap<>();
        toolbarTitle.setText("SELECT A BUS STOP");

        mapFragment = new MapFragmentAdapter((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        Observable<Location> locationObservable = locationRepository.fetch(this);

        subscriptions.add(weatherBusMapRepository.getOnMarkerClickObservable(mapFragment)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextMarkerClick()));

        subscriptions.add(weatherBusMapRepository.getOnInfoWindowClickObservable(mapFragment)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextInfoWindowClick()));

        subscriptions.add(weatherBusMapRepository.getOnMapReadyObservable(mapFragment)
                .doOnNext(new Action1<WeatherBusMap>() {
                    @Override
                    public void call(WeatherBusMap weatherBusMap) {
                        weatherBusMap.setMyLocationEnabled(true);
                        MapActivity.this.weatherBusMap = weatherBusMap;
                    }
                }).zipWith(locationObservable, new Func2<WeatherBusMap, Location, LatLngBounds>() {
                    @Override
                    public LatLngBounds call(WeatherBusMap weatherBusMap, Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        weatherBusMap.moveCamera(latLng);
                        return weatherBusMap.getLatLngBounds();
                    }
                }).mergeWith(weatherBusMapRepository.getOnCameraChangeObservable(mapFragment))
                .flatMap(new StopsFromLatLngBoundsFunction())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StopForLocationResponsesSubscriber()));
    }

    @OnClick(R.id.toolbar_favorite_button)
    public void toggleFavorite() {
        if (selectedStop != null) {
            if (selectedStop.isFavorite()) {
                selectedStop.setFavorite(false);
                busStopMarkers.get(selectedStop).setFavorite(false);
                favoriteStops.deleteSavedStop(selectedStop.getId());
                favoriteButton.setColorFilter(null);
            } else {
                selectedStop.setFavorite(true);
                busStopMarkers.get(selectedStop).setFavorite(true);
                favoriteStops.addSavedStop(selectedStop.getId());
                favoriteButton.setColorFilter(ContextCompat.getColor(this,android.R.color.holo_red_dark));
            }
        }
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        weatherBusMapRepository.reset();
        super.onDestroy();
    }

    private BusStop findBusStop(WeatherBusMarker weatherBusMarker) {
        for(Map.Entry<BusStop, WeatherBusMarker> entry : busStopMarkers.entrySet()) {
            if (entry.getValue() == weatherBusMarker) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void startBusStopActivity(String stopId, String stopName) {
        Intent intent = new Intent(MapActivity.this, BusStopActivity.class);
        intent.putExtra("stopId", stopId);
        intent.putExtra("stopName", stopName);
        startActivity(intent);
    }

    private class OnNextMarkerClick implements Action1<WeatherBusMarker> {
        @Override
        public void call(WeatherBusMarker weatherBusMarker) {
            selectedStop = findBusStop(weatherBusMarker);
            if (selectedStop != null) {
                toolbarTitle.setText(selectedStop.getName() + " (" + selectedStop.getDirection() + ")");
                favoriteButton.setVisibility(View.VISIBLE);
                if (selectedStop.isFavorite()) {
                    favoriteButton.setColorFilter(ContextCompat.getColor(MapActivity.this, android.R.color.holo_red_dark));
                } else {
                    favoriteButton.setColorFilter(null);
                }
            }
        }
    }

    private class OnNextInfoWindowClick implements Action1<WeatherBusMarker> {
        @Override
        public void call(WeatherBusMarker weatherBusMarker) {
            BusStop selectedStop = findBusStop(weatherBusMarker);
            if (selectedStop == null) {
                return;
            }
            startBusStopActivity(selectedStop.getId(), selectedStop.getName());
        }
    }

    private class StopForLocationResponsesSubscriber extends Subscriber<StopForLocationResponse> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(), "Failed to get stops near location!", Toast.LENGTH_SHORT).show();
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
            List<String> favoriteStops = MapActivity.this.favoriteStops.getSavedStops();
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
            progressBar.setVisibility(View.GONE);
            toolbarInfo.setVisibility(View.VISIBLE);
        }
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
}

