package io.pivotal.weatherbus.app.view;

import io.pivotal.weatherbus.app.model.BusStop;

public interface FragmentListener {
    void onStopsLoaded();
    void onStopSelected(BusStop busStop);
}
