package io.pivotal.weatherbus.app.view;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.adapter.BusRouteAdapter;
import io.pivotal.weatherbus.app.model.BusRoute;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.services.response.DeparturesResponse;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class BusRoutesFragment extends Fragment {

    @Inject WeatherBusService service;

    @Bind(R.id.busList) ListView busList;
    @Bind(R.id.emptyRouteMessage) TextView message;

    private String stopId = "";

    BusRouteAdapter adapter;

    public BusRoutesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeatherBusApplication.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus_routes, container, false);
        ButterKnife.bind(this, view);
        adapter = new BusRouteAdapter(getActivity());
        busList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void setStopId(String stopId) {
        if(!this.stopId.equals(stopId)) {
            adapter.clear();
            this.stopId = stopId;
            showDepartures();
        }
    }

    private void showDepartures() {
        service.getStopInformation(stopId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DeparturesResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), "Failed to get bus routes", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(DeparturesResponse departuresResponse) {
                        List<DeparturesResponse.StopData.Departure> departures = departuresResponse.getData().getDepartures();
                        if (departures.size() == 0) {
                            message.setVisibility(View.VISIBLE);
                        }
                        for (DeparturesResponse.StopData.Departure departure : departures) {
                            BusRoute busRoute = new BusRoute(departure.getBusNumber(),
                                    departure.getBusName(),
                                    departure.getPredictedTime(),
                                    departure.getScheduledTime(),
                                    departure.getTemp());
                            adapter.add(busRoute);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
