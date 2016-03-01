package io.pivotal.weatherbus.app.services;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class StopResponse {
    private StopData data;

    public StopResponse(StopData data) {
        this.data = data;
    }

    @Data
    public static class StopData {
        private List<Departure> departures;

        public StopData(List<Departure> departures) {
            this.departures = departures;
        }

        @Data
        public static class Departure {

            public Departure(String busNumber, String busName, String direction, long predictedTime, long scheduledTime, double temp) {
                this.busNumber = busNumber;
                this.busName = busName;
                this.direction = direction;
                this.predictedTime = predictedTime;
                this.scheduledTime = scheduledTime;
                this.temp = temp;
            }

            @SerializedName("routeShortName")
            private String busNumber;

            @SerializedName("headsign")
            private String busName;

            private String direction;

            private long predictedTime;
            private long scheduledTime;
            private double temp;
        }
    }
}