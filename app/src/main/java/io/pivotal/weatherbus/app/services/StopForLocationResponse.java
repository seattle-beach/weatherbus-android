package io.pivotal.weatherbus.app.services;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class StopForLocationResponse {
    @SerializedName("data")
    private List<BusStopResponse> stops;

    private BusStopReference included;

    @Data
    public class BusStopResponse {
        private String id;
        private String name;
        private String direction;
        private double latitude;
        private double longitude;
        private List<String> routeIds;

        public BusStopResponse(String id, String name, String direction, double latitude, double longitude, List<String> routeIds) {
            this.id = id;
            this.name = name;
            this.direction = direction;
            this.latitude = latitude;
            this.longitude = longitude;
            this.routeIds = routeIds;
        }
    }

    @Data
    public static class BusStopReference {
        private List<RouteReference> routes;

        public BusStopReference(List<RouteReference> routes) {
            this.routes = routes;
        }

        @Data
        public static class RouteReference {

            private String id;
            private String longName;
            private String shortName;

            public RouteReference(String id, String longName, String shortName) {
                this.id = id;
                this.longName = longName;
                this.shortName = shortName;
            }
        }
    }
}