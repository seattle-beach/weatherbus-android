package io.pivotal.weatherbus.app.services.response;

import lombok.Data;

@Data
public class RouteReference {

    private String id;
    private String longName;
    private String shortName;

    public RouteReference(String id, String longName, String shortName) {
        this.id = id;
        this.longName = longName;
        this.shortName = shortName;
    }
}
