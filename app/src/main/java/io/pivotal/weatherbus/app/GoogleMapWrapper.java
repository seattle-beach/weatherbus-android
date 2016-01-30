package io.pivotal.weatherbus.app;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

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
}
