package io.pivotal.weatherbus.app;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

public class WeatherBusMap {
    GoogleMap map;

    public WeatherBusMap(GoogleMap map) {
        this.map = map;
    }

    public void moveCamera(LatLng latLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public LatLngBounds getLatLngBounds() {
        return map.getProjection().getVisibleRegion().latLngBounds;
    }

    public WeatherBusMarker addMarker(MarkerOptions options) {
        return new WeatherBusMarker(map.addMarker(options));
    }

    public void setMyLocationEnabled(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    public void setPadding(int left, int top, int right, int bottom) {
        map.setPadding(left,top,right,bottom);
    }

    public class WeatherBusMarker {
        Marker marker;

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
    }
}
