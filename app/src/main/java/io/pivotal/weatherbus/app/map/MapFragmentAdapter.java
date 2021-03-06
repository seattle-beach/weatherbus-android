package io.pivotal.weatherbus.app.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MapFragmentAdapter {
    private MapFragment mapFragment;

    public MapFragmentAdapter(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public void getMapAsync(final WeatherBusMap.OnWeatherBusMapReadyCallback callback) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                callback.onMapReady(new WeatherBusMap(googleMap));
            }
        });
    }

}
