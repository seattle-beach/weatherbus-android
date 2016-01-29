package io.pivotal.weatherbus.app.services;

import lombok.Data;

@Data
public class StopForLocationResponse {
    String id;
    String name;
    double latitude;
    double longitude;
}
