package io.pivotal.weatherbus.app.map;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

public class WeatherBusMarker {
    private Marker marker;

    public WeatherBusMarker(Marker marker) {
        this.marker = marker;
    }

    public void remove() {
        marker.remove();
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

    public String getTitle() {
        return marker.getTitle();
    }

    public String getSnippet() {
        return marker.getSnippet();
    }

    public void setTitle(String title) {
        marker.setTitle(title);
    }

    public void setSnippet(String snippet) {
        marker.setSnippet(snippet);
    }
}
