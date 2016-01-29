package io.pivotal.weatherbus.app;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.List;

public class MapActivity extends RoboActivity{
    MapFragment mapFragment;
    private CompositeSubscription subscriptions = Subscriptions.from();

    @InjectView(R.id.stopList) ListView stopList;
    ArrayAdapter<String> adapter;

    TextView currentLocationHeader;

    @Inject
    WeatherBusService service;

    @Inject
    LocationRepository locationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        LayoutInflater inflater = getLayoutInflater();
        currentLocationHeader = (TextView) inflater.inflate(R.layout.current_location, stopList, false);
        stopList.addHeaderView(currentLocationHeader);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter.clear();
        stopList.setAdapter(adapter);
        adapter.add("Hello");

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        MapRepository mapRepository = new MapRepository();
        subscriptions.add(mapRepository.create(mapFragment).subscribe(new GoogleMapSubscriber()));
        subscriptions.add(locationRepository.create(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new LocationSubscriber()));
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

    private class GoogleMapSubscriber extends Subscriber<GoogleMap> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(), "Failed to load maps!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(GoogleMap googleMap) {

        }
    }

    private class LocationSubscriber extends Subscriber<Location> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(), "Failed to get location!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(Location location) {
            String text = String.format("(%.1f, %.1f)", location.getLatitude(), location.getLongitude());
            currentLocationHeader.setText(text);
            subscriptions.add(service.getStopsForLocation(location.getLatitude(), location.getLongitude(), 0.02, 0.02)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new StopForLocationResponsesSubscriber()));
        }

        private class StopForLocationResponsesSubscriber extends Subscriber<List<StopForLocationResponse>> {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Failed to get stops near location!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(List<StopForLocationResponse> stopForLocationResponses) {
                adapter.clear();
                for (StopForLocationResponse stop : stopForLocationResponses) {
                    String text = String.format("%s: (%.1f, %.1f)", stop.getName(), stop.getLatitude(), stop.getLongitude());
                    adapter.add(text);
                }
            }
        }
    }
}
