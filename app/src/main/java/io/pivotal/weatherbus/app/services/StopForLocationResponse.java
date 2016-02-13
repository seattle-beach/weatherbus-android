package io.pivotal.weatherbus.app.services;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class StopForLocationResponse {
    @SerializedName("data")
    private List<BusStopResponse> stops;

    @Data
    public class BusStopResponse {
        private String id;
        private String name;
        private double latitude;
        private double longitude;

        public BusStopResponse(String id, String name, double latitude, double longitude) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}