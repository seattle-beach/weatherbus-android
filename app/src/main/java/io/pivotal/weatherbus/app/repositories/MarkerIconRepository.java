package io.pivotal.weatherbus.app.repositories;

import com.google.android.gms.maps.model.BitmapDescriptor;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.model.IconOptions;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MarkerIconRepository {
    @Inject MarkerIconFactory markerIconFactory;

    Map<IconOptions, BitmapDescriptor> cache;

    public MarkerIconRepository() {
        WeatherBusApplication.inject(this);
        cache = new HashMap<>();
    }

    public BitmapDescriptor get(IconOptions options) {
        if (!cache.containsKey(options)) {
            BitmapDescriptor descriptor = markerIconFactory.create(options).draw();
            cache.put(options, descriptor);
        }
        return cache.get(options);
    }
}
