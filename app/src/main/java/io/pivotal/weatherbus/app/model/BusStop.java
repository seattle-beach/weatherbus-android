package io.pivotal.weatherbus.app.model;

import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BusStop {
    final private String id;
    final private String name;
    final private String direction;
    final private double latitude;
    final private double longitude;
    final private List<String> routeIds;

    private boolean favorite;

    public BusStop(StopForLocationResponse.BusStopResponse response) {
        this.id = response.getId();
        this.name = response.getName();
        this.latitude = response.getLatitude();
        this.longitude = response.getLongitude();
        this.direction = response.getDirection();
        this.routeIds = response.getRouteIds();
    }

    public BusStop(String id, String name, String direction, double latitude, double longitude, List<String> routeIds) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.latitude = latitude;
        this.longitude = longitude;
        this.routeIds = routeIds;
    }
}
