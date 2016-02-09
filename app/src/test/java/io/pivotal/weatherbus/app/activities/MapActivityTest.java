package io.pivotal.weatherbus.app.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.*;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.OnWeatherBusMapReadyCallback;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.MapRepository;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import rx.subjects.PublishSubject;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;


@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapActivityTest {
    @Inject
    WeatherBusService service;

    @Inject
    MapRepository mapRepository;

    @Inject
    LocationRepository locationRepository;

    @Inject
    SavedStops savedStops;

    @Mock
    WeatherBusMap googleMap;

    @Mock
    WeatherBusMarker marker;

    @Mock
    Location location;

    MapActivity subject;

    PublishSubject<StopForLocationResponse> stopEmitter;
    PublishSubject<Location> locationEmitter;
    PublishSubject<WeatherBusMap> mapEmitter;
    PublishSubject<WeatherBusMap> centeredMapEmitter;

    StopForLocationResponse response;

    @Before
    public void setUp() throws Exception {
        mapEmitter = PublishSubject.create();
        locationEmitter = PublishSubject.create();
        centeredMapEmitter = PublishSubject.create();
        when(location.getLatitude()).thenReturn(5.0);
        when(location.getLongitude()).thenReturn(5.0);
        when(locationRepository.create(any(MapActivity.class))).thenReturn(locationEmitter);
        when(mapRepository.getOnMapReadyObservable(any(MapFragmentAdapter.class))).thenReturn(mapEmitter);
        when(mapRepository.getOnCenteredMapObservable(any(MapFragmentAdapter.class), eq(locationEmitter))).thenReturn(centeredMapEmitter);


        stopEmitter = PublishSubject.create();

        subject = Robolectric.setupActivity(MapActivity.class);

        LatLngBounds bounds = new LatLngBounds(new LatLng(4,4), new LatLng(6,6));
        when(googleMap.getLatLngBounds()).thenReturn(bounds);
        when(service.getStopsForLocation(4.0, 6.0, 2.0, 2.0)).thenReturn(stopEmitter);
        when(googleMap.addMarker(any(MarkerOptions.class))).thenReturn(marker);
        when(googleMap.getMarker(any(String.class))).thenReturn(marker);

        response = new StopForLocationResponse() {{
            setStops(new ArrayList<BusStopResponse>() {{
                add(new BusStopResponse());
                get(0).setId("1_1234");
                get(0).setName("STOP 0");
                get(0).setLatitude(4.2);
                get(0).setLongitude(4.3);
                add(new BusStopResponse());
                get(1).setId("1_2234");
                get(1).setName("STOP 1");
                get(1).setLatitude(4.4);
                get(1).setLongitude(4.5);
            }});
        }};
    }

    @Test
    public void shouldCenterMap() {

    }

    @Test
    public void onNextMap_shouldGetNearbyStops_usingCurrentLocation() {
        mapEmitter.onNext(googleMap);
        locationEmitter.onNext(location);
        centeredMapEmitter.onNext(googleMap);

        verify(service,times(1)).getStopsForLocation(5.0, 5.0, 2.0, 2.0);
    }

    @Test
    public void onCreate_shouldShowProgressBar() {
        assertThat(subject.findViewById(R.id.progressBar).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onNextListStops_shouldShowNearbyStops() {
        centeredMapEmitter.onNext(googleMap);
        stopEmitter.onNext(response);
        stopEmitter.onCompleted();

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = lv.getAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);

        String stopResponse = ((TextView) (lv.getChildAt(0))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 0: (4.2, 4.3)");
        stopResponse = ((TextView) (lv.getChildAt(1))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 1: (4.4, 4.5)");

        verify(googleMap, times(2)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onNextListStops_shouldRemoveProgressBar() {
        fulfillRequests();

        assertThat(subject.findViewById(R.id.progressBar).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onNextListStops_ifStopIsFavorite_ShouldShowAStarAndColorMarker() {
        when(savedStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});

        fulfillRequests();

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        String firstStop = ((TextView) (lv.getChildAt(0))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isEqualTo('*');
        verify(googleMap,times(2)).addMarker(any(MarkerOptions.class));
        verify(marker,times(1)).setFavorite(false);
        verify(marker,times(1)).setFavorite(true);
    }

    @Test
    public void onLongClick_whenIsNotFavoriteStop_shouldAddToFavoriteStops() {
        fulfillRequests();
        ListView stopList = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(stopList).populateItems();
        Adapter adapter = stopList.getAdapter();

        assertThat(stopList.getOnItemLongClickListener().
                onItemLongClick(stopList,stopList.getChildAt(0),0,adapter.getItemId(0))).isEqualTo(true);

        String busStopId = ((BusStop) adapter.getItem(0)).getResponse().getId();
        verify(savedStops,times(1)).addSavedStop(busStopId);
        String firstStop = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isEqualTo('*');
        verify(marker,times(1)).setFavorite(true);
    }

    @Test
    public void onLongClick_shouldRemoveFavoriteStops() {

        when(savedStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
            add("1_2234");
        }});

        fulfillRequests();
        ListView stopList = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(stopList).populateItems();
        Adapter adapter = stopList.getAdapter();

        assertThat(stopList.getOnItemLongClickListener().
                onItemLongClick(stopList,stopList.getChildAt(0),0,adapter.getItemId(0))).isEqualTo(true);

        String busStopId = ((BusStop) adapter.getItem(0)).getResponse().getId();
        verify(savedStops,times(1)).deleteSavedStop(busStopId);
        String firstStop = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isNotEqualTo('*');
        verify(marker,times(1)).setFavorite(false);
    }

    @Test
    public void onClick_itShouldOpenBusStopActivity() {
        fulfillRequests();
        ListView stopList = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(stopList).populateItems();
        Adapter adapter = stopList.getAdapter();
        stopList.performItemClick(stopList.getChildAt(0), 0, adapter.getItemId(0));
        Intent intent = shadowOf(subject).peekNextStartedActivityForResult().intent;
        assertThat(intent.getStringExtra("stopId")).isEqualTo("1_1234");
        assertThat(intent.getStringExtra("stopName")).isEqualTo("STOP 0");
        assertThat(intent.getComponent()).isEqualTo(new ComponentName(subject, BusStopActivity.class));

    }

    private void fulfillRequests() {
        mapEmitter.onNext(googleMap);
        locationEmitter.onNext(location);
        centeredMapEmitter.onNext(googleMap);
        stopEmitter.onNext(response);
        stopEmitter.onCompleted();
    }
}