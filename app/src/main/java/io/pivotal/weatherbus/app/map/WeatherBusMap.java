package io.pivotal.weatherbus.app.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.util.HashMap;
import java.util.Map;

public class WeatherBusMap {
    GoogleMap googleMap;
    Map<String, WeatherBusMarker> weatherBusMarkers;

    public WeatherBusMap(GoogleMap map) {
        this.googleMap = map;
        this.weatherBusMarkers = new HashMap<>();
    }

    public Void moveCamera(LatLng latLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        return null;
    }

    public LatLngBounds getLatLngBounds() {
        return googleMap.getProjection().getVisibleRegion().latLngBounds;
    }

    public WeatherBusMarker addMarker(MarkerOptions options) {
        WeatherBusMarker marker = new WeatherBusMarker(googleMap.addMarker(options));
        weatherBusMarkers.put(marker.getId(), marker);
        return marker;
    }

    public void setMyLocationEnabled(boolean enabled) {
        googleMap.setMyLocationEnabled(enabled);
    }

    public void setPadding(int left, int top, int right, int bottom) {
        googleMap.setPadding(left,top,right,bottom);
    }

    public void clear() {
        googleMap.clear();
    }

    public Void setOnMarkerClickListener(final OnWeatherBusMarkerClick listener) {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return listener.onMarkerClick(weatherBusMarkers.get(marker.getId()));
            }
        });
        return null;
    }

    public Void setOnInfoWindowClickListener(final OnWeatherBusInfoClickListener listener) {
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                listener.onInfoWindowClick(weatherBusMarkers.get(marker.getId()));
            }
        });
        return null;
    }

    public Void setOnCameraChangeListener(final OnWeatherBusCameraChangeListener listener) {
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                listener.onCameraChange(cameraPosition);
            }
        });
        return null;
    }

    public interface OnWeatherBusMapReadyCallback {
        void onMapReady(WeatherBusMap map);
    }

    public interface OnWeatherBusInfoClickListener {
        void onInfoWindowClick(WeatherBusMarker marker);
    }

    public interface OnWeatherBusMarkerClick {
        boolean onMarkerClick(WeatherBusMarker marker);
    }

    public interface OnWeatherBusCameraChangeListener {
        void onCameraChange(CameraPosition position);
    }
}
