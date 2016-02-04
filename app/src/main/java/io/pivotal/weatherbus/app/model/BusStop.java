package io.pivotal.weatherbus.app.model;

import io.pivotal.weatherbus.app.GoogleMapWrapper;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import lombok.Data;

@Data
public class BusStop {
    StopForLocationResponse.BusStopResponse response;
    private boolean favorite;
    GoogleMapWrapper.MarkerWrapper marker;

    public BusStop(StopForLocationResponse.BusStopResponse response) {
        this.response = response;
    }
}
