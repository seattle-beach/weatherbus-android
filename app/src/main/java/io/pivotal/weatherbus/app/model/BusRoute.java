package io.pivotal.weatherbus.app.model;

import lombok.Data;

@Data
public class BusRoute {
    private String routeNumber;
    private String routeName;
    private long predictedTime;
    private long scheduledTime;
    private double temperature;

    public BusRoute(String routeNumber, String routeName, long predictedTime, long scheduledTime, double temperature) {
        this.routeNumber = routeNumber;
        this.routeName = routeName;
        this.predictedTime = predictedTime;
        this.scheduledTime = scheduledTime;
        this.temperature = temperature;
    }
}
