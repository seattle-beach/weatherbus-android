package io.pivotal.weatherbus.app.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.SavedStops;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.model.BusStopAdapter;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends RoboActivity {
    MapFragmentAdapter mapFragment;
    private CompositeSubscription subscriptions;

    @InjectView(R.id.stopList) ListView stopList;
    @InjectView(R.id.progressBar) ProgressBar progressBar;

    @Inject
    WeatherBusService service;

    @Inject
    SavedStops savedStops;

    @Inject
    WeatherBusMapRepository weatherBusMapRepository;

    @Inject
    LocationRepository locationRepository;

    private WeatherBusMap weatherBusMap;
    private BusStopAdapter adapter;
    private Map<BusStop, WeatherBusMarker> busStopMarkers;

    private BehaviorSubject<Boolean> windowFocusedSubject = BehaviorSubject.create();
    private BusStop selectedStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        subscriptions = Subscriptions.from();
        busStopMarkers = new HashMap<BusStop, WeatherBusMarker>();

        adapter = new BusStopAdapter(this, android.R.layout.simple_list_item_1);
        stopList.setAdapter(adapter);
        stopList.setOnItemClickListener(new OnStopClickListener());
        stopList.setOnItemLongClickListener(new OnStopLongClickListener());

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
                        MapActivity.this.weatherBusMap = weatherBusMap;
                    }
                }).zipWith(windowFocusedSubject, new Func2<WeatherBusMap, Boolean, WeatherBusMap>() {
                    @Override
                    public WeatherBusMap call(WeatherBusMap weatherBusMap, Boolean aBoolean) {
                        weatherBusMap.setPadding(0, 0, 0, stopList.getTop());
                        weatherBusMap.setMyLocationEnabled(true);
                        return weatherBusMap;
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            windowFocusedSubject.onNext(true);
        }
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        weatherBusMapRepository.reset();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private class OnStopClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BusStop busStop = adapter.getItem(position);
            startBusStopActivity(busStop.getId(),busStop.getName());
        }
    }

    private class OnStopLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            BusStop busStop = adapter.getItem(position);
            String stopId = busStop.getId();

            boolean isFavorite = busStop.isFavorite();
            if (isFavorite) {
                savedStops.deleteSavedStop(stopId);
            } else {
                savedStops.addSavedStop(stopId);
            }
            busStop.setFavorite(!isFavorite);
            WeatherBusMarker marker = busStopMarkers.get(busStop);
            marker.setFavorite(!isFavorite);
            adapter.notifyDataSetChanged();
            return true;
        }
    }

    private class OnNextMarkerClick implements Action1<WeatherBusMarker> {
        @Override
        public void call(WeatherBusMarker weatherBusMarker) {
            selectedStop = findBusStop(weatherBusMarker);
            if (selectedStop == null) {
                return;
            }
            adapter.highlightStop(selectedStop.getId());
            adapter.notifyDataSetChanged();
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
            adapter.clear();
            if (selectedStop != null) {
                adapter.add(selectedStop);
            }
            WeatherBusMarker selectedMarker = busStopMarkers.get(selectedStop);
            for (WeatherBusMarker marker : busStopMarkers.values()) {
                if (marker != selectedMarker) {
                    marker.remove();
                }
            }
            busStopMarkers.clear();
            List<String> favoriteStops = savedStops.getSavedStops();
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
                    adapter.add(busStop);
                }
                busStopMarkers.put(busStop,marker);
            }
            stopList.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
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

