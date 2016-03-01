package io.pivotal.weatherbus.app.model;

import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusStop {
    final private String id;
    final private String name;
    final private String direction;
    final private double latitude;
    final private double longitude;

    private boolean favorite;

    public BusStop(StopForLocationResponse.BusStopResponse response) {
        this.id = response.getId();
        this.name = response.getName();
        this.latitude = response.getLatitude();
        this.longitude = response.getLongitude();
        this.direction = response.getDirection();
    }
}
