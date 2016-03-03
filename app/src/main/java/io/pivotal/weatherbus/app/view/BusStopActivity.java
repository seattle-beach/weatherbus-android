package io.pivotal.weatherbus.app.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.model.BusRoute;
import io.pivotal.weatherbus.app.model.BusRouteAdapter;
import io.pivotal.weatherbus.app.services.StopResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.util.List;

public class BusStopActivity extends Activity {

    @Inject
    WeatherBusService service;

    @Bind(R.id.busList) ListView busList;
    @Bind(R.id.emptyRouteMessage) TextView message;

    String stopName;
    String stopId;

    BusRouteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop);
        ButterKnife.bind(this);
        WeatherBusApplication.inject(this);

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
}
