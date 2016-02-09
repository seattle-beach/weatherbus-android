//package io.pivotal.weatherbus.app.subscribers;
//
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.inject.Inject;
//import io.pivotal.weatherbus.app.map.WeatherBusMap;
//import io.pivotal.weatherbus.app.map.WeatherBusMarker;
//import io.pivotal.weatherbus.app.model.BusStop;
//import io.pivotal.weatherbus.app.services.StopForLocationResponse;
//import io.pivotal.weatherbus.app.services.WeatherBusService;
//import rx.Subscriber;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//
//import java.util.List;
//
//public class GoogleMapSubscriber extends Subscriber<WeatherBusMap> {
//
//    @Inject
//    WeatherBusService service;
//
//    @Override
//    public void onCompleted() {
//
//    }
//
//    @Override
//    public void onError(Throwable e) {
//        //Toast.makeText(getApplicationContext(), "Failed to load maps!", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onNext(WeatherBusMap googleMap) {
//        googleMap.setMyLocationEnabled(true);
//
//        LatLngBounds bounds = googleMap.getLatLngBounds();
//
//        LatLng center = bounds.getCenter();
//        double left = bounds.northeast.latitude - bounds.southwest.latitude;
//        double right = bounds.northeast.longitude - bounds.southwest.longitude;
//        service.getStopsForLocation(center.latitude, center.longitude, left, right)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(new StopForLocationResponsesSubscriber());
//    }
//
//    private class StopForLocationResponsesSubscriber extends Subscriber<StopForLocationResponse> {
//        @Override
//        public void onCompleted() {
//
//        }
//
//        @Override
//        public void onError(Throwable e) {
//            //Toast.makeText(getApplicationContext(), "Failed to get stops near location!", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onNext(StopForLocationResponse stopForLocationResponse) {
//            adapter.clear();
//            List<String> favoriteStops = savedStops.getSavedStops();
//            for (StopForLocationResponse.BusStopResponse stopResponse : stopForLocationResponse.getStops()) {
//                BusStop busStop = new BusStop(stopResponse);
//                boolean isFavorite = favoriteStops.contains(stopResponse.getId());
//                busStop.setFavorite(isFavorite);
//                adapter.add(busStop);
//                LatLng stopPosition = new LatLng(stopResponse.getLatitude(),stopResponse.getLongitude());
//                WeatherBusMarker marker = googleMap.addMarker(new MarkerOptions()
//                        .position(stopPosition)
//                        .title(busStop.getResponse().getName()));
//                marker.setFavorite(isFavorite);
//                markerIds.put(busStop,marker);
//            }
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 0, 1);
//            stopList.setLayoutParams(params);
//            progressBar.setVisibility(View.GONE);
//        }
//    }
//}
