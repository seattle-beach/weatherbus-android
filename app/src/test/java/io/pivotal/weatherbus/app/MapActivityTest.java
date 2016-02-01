package io.pivotal.weatherbus.app;

import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
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
import java.util.List;

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

    @Mock
    GoogleMapWrapper googleMap;

    MapActivity subject;

    PublishSubject<List<StopForLocationResponse>> stopEmitter;
    PublishSubject<GoogleMapWrapper> mapEmitter;

    @Before
    public void setUp() throws Exception {
        mapEmitter = PublishSubject.create();
        when(mapRepository.create(any(MapFragment.class), any(MapActivity.class))).thenReturn(mapEmitter);

        stopEmitter = PublishSubject.create();

        subject = Robolectric.setupActivity(MapActivity.class);
    }

    @Test
    public void onCreate_shouldGetCurrentLocation() {
        LatLngBounds bounds = new LatLngBounds(new LatLng(4,4), new LatLng(6,6));
        when(googleMap.getLatLngBounds()).thenReturn(bounds);
        mapEmitter.onNext(googleMap);


        TextView header = (TextView)subject.findViewById(R.id.currentLocation);
        assertThat(header).isNotNull();
        assertThat(header.getText().toString()).isEqualTo("(5.0, 5.0)");

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = ((HeaderViewListAdapter)lv.getAdapter()).getWrappedAdapter();
        assertThat(adapter.getCount()).isEqualTo(1);
        assertThat(adapter.getItem(0)).isEqualTo("Hello");
    }

    @Test
    public void onCreate_shouldShowNearbyStops() {
        LatLngBounds bounds = new LatLngBounds(new LatLng(4,4), new LatLng(6,6));
        when(googleMap.getLatLngBounds()).thenReturn(bounds);
        when(service.getStopsForLocation(5.0, 5.0, 2.0, 2.0)).thenReturn(stopEmitter);
        mapEmitter.onNext(googleMap);

        List<StopForLocationResponse> nearbyStops = new ArrayList<StopForLocationResponse>() {{
            add(new StopForLocationResponse());
            get(0).setId("1_1234");
            get(0).setName("STOP 0");
            get(0).setLatitude(4.2);
            get(0).setLongitude(4.3);
            add(new StopForLocationResponse());
            get(1).setId("1_2234");
            get(1).setName("STOP 1");
            get(1).setLatitude(4.4);
            get(1).setLongitude(4.5);
        }};
        stopEmitter.onNext(nearbyStops);
        stopEmitter.onCompleted();

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = ((HeaderViewListAdapter)lv.getAdapter()).getWrappedAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);

        String stopResponse = (String)adapter.getItem(0);
        assertThat(stopResponse).isEqualTo("STOP 0: (4.2, 4.3)");
        stopResponse = (String)adapter.getItem(1);
        assertThat(stopResponse).isEqualTo("STOP 1: (4.4, 4.5)");

        verify(googleMap, times(2)).addMarker(any(MarkerOptions.class));
    }
}