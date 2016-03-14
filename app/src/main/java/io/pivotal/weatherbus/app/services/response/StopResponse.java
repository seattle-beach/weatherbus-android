package io.pivotal.weatherbus.app.services.response;

import lombok.Data;

import java.util.List;

@Data
public class StopResponse {
    private String id;
    private String name;
    private String direction;
    private double latitude;
    private double longitude;
    private List<String> routeIds;

    public StopResponse(String id, String name, String direction, double latitude, double longitude, List<String> routeIds) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.latitude = latitude;
        this.longitude = longitude;
        this.routeIds = routeIds;
    }
}
