package io.pivotal.weatherbus.app;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

public class GoogleMapWrapper {
    GoogleMap map;

    public GoogleMapWrapper(GoogleMap map) {
        this.map = map;
    }

    public void moveCamera(LatLng latLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public LatLngBounds getLatLngBounds() {
        return map.getProjection().getVisibleRegion().latLngBounds;
    }

    public MarkerWrapper addMarker(MarkerOptions options) {
        return new MarkerWrapper(map.addMarker(options));
    }

    public void setMyLocationEnabled(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    public void setPadding(int left, int top, int right, int bottom) {
        map.setPadding(left,top,right,bottom);
    }

    public class MarkerWrapper {
        Marker marker;

        public MarkerWrapper(Marker marker) {
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
