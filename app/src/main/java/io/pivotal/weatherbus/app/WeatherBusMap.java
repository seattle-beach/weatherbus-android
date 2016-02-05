package io.pivotal.weatherbus.app;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.util.HashMap;
import java.util.Map;

public class WeatherBusMap {
    GoogleMap map;
    GoogleMap.OnMarkerClickListener onMarkerClickListener;
    GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener;
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

    public Void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener listener) {
        map.setOnMarkerClickListener(listener);
        onMarkerClickListener = listener;
        return null;
    }

    public Void setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener listener) {
        map.setOnInfoWindowClickListener(listener);
        onInfoWindowClickListener = listener;
        return null;
    }

    public void performMarkerClick(WeatherBusMarker marker) {
        onMarkerClickListener.onMarkerClick(marker.marker);
    }

    public void performOnInfoWindowClick(WeatherBusMarker marker) {
        onInfoWindowClickListener.onInfoWindowClick(marker.marker);
    }

    public class WeatherBusMarker {
        private Marker marker;

        public WeatherBusMarker(Marker marker) {
            this.marker = marker;
        }

        public void setFavorite(boolean isFavorite) {
            if (isFavorite) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker());
            }
            else {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }
        }

        public String getId() {
            return marker.getId();
        }
    }
}
