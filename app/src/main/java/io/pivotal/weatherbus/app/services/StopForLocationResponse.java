package io.pivotal.weatherbus.app.services;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class StopForLocationResponse {
    @SerializedName("data")
    List<BusStopResponse> stops;

    @Data
    public class BusStopResponse {
        String id;
        String name;
        double latitude;
        double longitude;
    }
}