package io.pivotal.weatherbus.app.map;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class WeatherBusMarker {
    private Marker marker;

    public WeatherBusMarker(Marker marker) {
        this.marker = marker;
    }

    public void remove() {
        marker.remove();
    }

    public String getId() {
        return marker.getId();
    }

    public String getTitle() {
        return marker.getTitle();
    }

    public String getSnippet() {
        return marker.getSnippet();
    }

    public void setTitle(String title) {
        marker.setTitle(title);
    }

    public LatLng getPosition() {
        return marker.getPosition();
    }

    public void setSnippet(String snippet) {
        marker.setSnippet(snippet);
    }

    public void setIcon(BitmapDescriptor icon) {
        marker.setIcon(icon);
    }
}
