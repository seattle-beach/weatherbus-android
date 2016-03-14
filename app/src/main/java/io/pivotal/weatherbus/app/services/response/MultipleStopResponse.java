package io.pivotal.weatherbus.app.services.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class MultipleStopResponse {
    @SerializedName("data")
    private List<StopResponse> stops;
    private BusStopReference included;

    public MultipleStopResponse(List<StopResponse> stops, BusStopReference included) {
        this.stops = stops;
        this.included = included;
    }

    @Data
    public static class BusStopReference {
        private List<RouteReference> routes;

        public BusStopReference(List<RouteReference> routes) {
            this.routes = routes;
        }
    }
}