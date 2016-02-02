package io.pivotal.weatherbus.app;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

    public Marker addMarker(MarkerOptions options) {
        return map.addMarker(options);
    }

    public void setMyLocationEnabled(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    public void setPadding(int left, int top, int right, int bottom) {
        map.setPadding(left,top,right,bottom);
    }
}
