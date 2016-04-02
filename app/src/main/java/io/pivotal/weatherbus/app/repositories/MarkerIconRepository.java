package io.pivotal.weatherbus.app.repositories;

import com.google.android.gms.maps.model.BitmapDescriptor;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.map.MarkerImageFactory;
import io.pivotal.weatherbus.app.model.MarkerImageOptions;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MarkerIconRepository {
    @Inject
    MarkerImageFactory markerImageFactory;

    Map<MarkerImageOptions, BitmapDescriptor> cache;

    public MarkerIconRepository() {
        WeatherBusApplication.inject(this);
        cache = new HashMap<>();
    }

    public BitmapDescriptor get(MarkerImageOptions options) {
        if (!cache.containsKey(options)) {
            BitmapDescriptor descriptor = markerImageFactory.create(options).draw();
            cache.put(options, descriptor);
        }
        return cache.get(options);
    }
}
