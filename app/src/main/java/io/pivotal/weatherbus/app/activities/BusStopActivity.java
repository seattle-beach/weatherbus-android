package io.pivotal.weatherbus.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.model.BusRoute;
import io.pivotal.weatherbus.app.model.BusRouteAdapter;
import io.pivotal.weatherbus.app.services.StopResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.List;

public class BusStopActivity extends RoboActivity {

    @Inject
    WeatherBusService service;

    @InjectView(R.id.busList) ListView busList;
    @InjectView(R.id.emptyRouteMessage) TextView message;

    String stopName;
    String stopId;

    BusRouteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop);
        stopId = getIntent().getStringExtra("stopId");
        stopName = getIntent().getStringExtra("stopName");
        this.setTitle(stopName);
        Observable<StopResponse> response = service.getStopInformation(stopId);
        adapter = new BusRouteAdapter(this);
        busList.setAdapter(adapter);

        response.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StopResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Failed to get bus routes", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(StopResponse stopResponse) {
                List<StopResponse.StopData.Departure> departures = stopResponse.getData().getDepartures();
                if (departures.size() == 0) {
                    message.setVisibility(View.VISIBLE);
                }
                for (StopResponse.StopData.Departure departure : departures) {
                    BusRoute busRoute = new BusRoute(departure.getBusNumber(),
                            departure.getBusName(),
                            departure.getPredictedTime(),
                            departure.getScheduledTime(),
                            departure.getTemp());
                    adapter.add(busRoute);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus_stop, menu);
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
