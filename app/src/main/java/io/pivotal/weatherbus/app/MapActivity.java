package io.pivotal.weatherbus.app;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

        Subscriber<GoogleMap> googleMapSubscriber = new Subscriber<GoogleMap>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GoogleMap googleMap) {

            }
        };

        MapRepository mapRepository = new MapRepository();
        mapRepository.create(mapFragment).subscribe(googleMapSubscriber);

        final Subscriber<Location> locationSubscriber = new Subscriber<Location>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Location location) {
                String text = String.format("(%.1f, %.1f)", location.getLatitude(), location.getLongitude());
                currentLocationHeader.setText(text);

                Subscriber<List<StopForLocationResponse>> stopSubscriber = new Subscriber<List<StopForLocationResponse>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<StopForLocationResponse> stopForLocationResponses) {
                        adapter.clear();
                        for (StopForLocationResponse stop : stopForLocationResponses) {
                            String text = String.format("%s: (%.1f, %.1f)", stop.getName(), stop.getLatitude(), stop.getLongitude());
                            adapter.add(text);
                        }
                    }
                };

                service.getStopsForLocation(location.getLatitude(), location.getLongitude(), 0.02, 0.02)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(stopSubscriber);
            }
        };
        locationRepository.create(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(locationSubscriber);
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
}
