package io.pivotal.weatherbus.app.services;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class StopForLocationResponse {
    @SerializedName("data")
    List<DataResponse> stops;

    @Data
    public class DataResponse {
        String id;
        String name;
        double latitude;
        double longitude;
    }
}