package io.pivotal.weatherbus.app.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class WeatherBusMap {
    GoogleMap map;
    Map<String, WeatherBusMarker> markers;

    public WeatherBusMap(GoogleMap map) {
        this.map = map;
        this.markers = new HashMap<String, WeatherBusMarker>();
    }

    public void moveCamera(LatLng latLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public LatLngBounds getLatLngBounds() {
        return map.getProjection().getVisibleRegion().latLngBounds;
    }

    public WeatherBusMarker addMarker(MarkerOptions options) {
        WeatherBusMarker marker = new WeatherBusMarker(map.addMarker(options));
        markers.put(marker.getId(), marker);
        return marker;
    }

    public WeatherBusMarker getMarker(String id) {
        return markers.get(id);
    }

    public void setMyLocationEnabled(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    public void setPadding(int left, int top, int right, int bottom) {
        map.setPadding(left,top,right,bottom);
    }

    public Void setOnMarkerClickListener(final OnWeatherBusMarkerClick listener) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return listener.onMarkerClick(markers.get(marker.getId()));
            }
        });
        return null;
    }
}
