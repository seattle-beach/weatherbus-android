package io.pivotal.weatherbus.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.SavedStops;
import io.pivotal.weatherbus.app.WeatherBusMap;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.model.BusStopAdapter;
import io.pivotal.weatherbus.app.repositories.MapRepository;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends RoboActivity {
    MapFragment mapFragment;
    private CompositeSubscription subscriptions = Subscriptions.from();

    @InjectView(R.id.stopList) ListView stopList;
    @InjectView(R.id.progressBar) ProgressBar progressBar;
    BusStopAdapter adapter;

    TextView currentLocationHeader;

    Map<BusStop, String> markerIds;

    @Inject
    WeatherBusService service;

    @Inject
    SavedStops savedStops;

    @Inject
    MapRepository mapRepository;

    WeatherBusMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        markerIds = new HashMap<BusStop, String>();

        LayoutInflater inflater = getLayoutInflater();
        currentLocationHeader = (TextView) inflater.inflate(R.layout.current_location, stopList, false);
        stopList.addHeaderView(currentLocationHeader);
        adapter = new BusStopAdapter(this, android.R.layout.simple_list_item_1);
        adapter.clear();
        stopList.setAdapter(adapter);

        stopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MapActivity.this, BusStopActivity.class);
                BusStop busStop = adapter.getItem(position - 1);
                intent.putExtra("stopId", busStop.getResponse().getId());
                intent.putExtra("stopName", busStop.getResponse().getName());
                startActivity(intent);
            }
        });

        stopList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                BusStop busStop = adapter.getItem(position - 1);
                String stopId = busStop.getResponse().getId();

                boolean isFavorite = busStop.isFavorite();
                if (isFavorite) {
                    savedStops.deleteSavedStop(stopId);
                } else {
                    savedStops.addSavedStop(stopId);
                }
                busStop.setFavorite(!isFavorite);
                String markerId = markerIds.get(busStop);
                WeatherBusMap.WeatherBusMarker marker = googleMap.getMarker(markerId);
                marker.setFavorite(!isFavorite);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        Observable<WeatherBusMap> googleMap = mapRepository.create(mapFragment, this);

        subscriptions.add(googleMap
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new GoogleMapSubscriber()));
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
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

    private class GoogleMapSubscriber extends Subscriber<WeatherBusMap> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(), "Failed to load maps!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(WeatherBusMap googleMap) {
            MapActivity.this.googleMap = googleMap;
            googleMap.setMyLocationEnabled(true);

            LatLngBounds bounds = googleMap.getLatLngBounds();
            String text = String.format(Locale.getDefault(), "(%.1f, %.1f)", bounds.getCenter().latitude, bounds.getCenter().longitude);
            currentLocationHeader.setText(text);

            LatLng center = bounds.getCenter();
            double left = bounds.northeast.latitude - bounds.southwest.latitude;
            double right = bounds.northeast.longitude - bounds.southwest.longitude;
            service.getStopsForLocation(center.latitude, center.longitude, left, right)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new StopForLocationResponsesSubscriber());
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
                List<String> favoriteStops = savedStops.getSavedStops();
                for (StopForLocationResponse.BusStopResponse stopResponse : stopForLocationResponse.getStops()) {
                    BusStop busStop = new BusStop(stopResponse);
                    boolean isFavorite = favoriteStops.contains(stopResponse.getId());
                    busStop.setFavorite(isFavorite);
                    adapter.add(busStop);
                    LatLng stopPosition = new LatLng(stopResponse.getLatitude(),stopResponse.getLongitude());
                    WeatherBusMap.WeatherBusMarker marker = googleMap.addMarker(new MarkerOptions()
                            .position(stopPosition)
                            .title(busStop.getResponse().getName()));
                    marker.setFavorite(isFavorite);
                    markerIds.put(busStop,marker.getId());
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 0, 1);
                stopList.setLayoutParams(params);
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}

