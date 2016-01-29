package io.pivotal.weatherbus.app;

import android.location.Location;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapActivityTest {
    @Inject
    WeatherBusService service;

    @Inject
    LocationRepository locationRepository;

    @Mock
    Location location;

    MapActivity subject;

    PublishSubject<Location> locationEmitter;
    PublishSubject<List<StopForLocationResponse>> stopEmitter;

    @Before
    public void setUp() throws Exception {
        locationEmitter = PublishSubject.create();
        when(locationRepository.create(any(MapActivity.class))).thenReturn(locationEmitter);
        subject = Robolectric.setupActivity(MapActivity.class);
        double lat = 12.3;
        double lng = 120.2;
        when(location.getLatitude()).thenReturn(lat);
        when(location.getLongitude()).thenReturn(lng);

        stopEmitter = PublishSubject.create();
        when(service.getStopsForLocation(eq(lat), eq(lng), any(Double.class), any(Double.class)))
                .thenReturn(stopEmitter);
    }

    @Test
    public void onCreate_shouldGetCurrentLocation() {
        locationEmitter.onNext(location);
        locationEmitter.onCompleted();

        TextView header = (TextView)subject.findViewById(R.id.currentLocation);
        assertThat(header).isNotNull();
        assertThat(header.getText().toString()).isEqualTo("(12.3, 120.2)");

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = ((HeaderViewListAdapter)lv.getAdapter()).getWrappedAdapter();
        assertThat(adapter.getCount()).isEqualTo(1);
        assertThat(adapter.getItem(0)).isEqualTo("Hello");
    }

    @Test
    public void onCreate_shouldShowNearbyStops() {
        locationEmitter.onNext(location);
        locationEmitter.onCompleted();

        List<StopForLocationResponse> nearbyStops = new ArrayList<StopForLocationResponse>() {{
            add(new StopForLocationResponse());
            get(0).setId("1_1234");
            get(0).setName("STOP 0");
            get(0).setLatitude(1.2);
            get(0).setLongitude(1.3);
            add(new StopForLocationResponse());
            get(1).setId("1_2234");
            get(1).setName("STOP 1");
            get(1).setLatitude(1.4);
            get(1).setLongitude(1.5);
        }};
        stopEmitter.onNext(nearbyStops);
        stopEmitter.onCompleted();

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = ((HeaderViewListAdapter)lv.getAdapter()).getWrappedAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);

        String stopResponse = (String)adapter.getItem(0);
        assertThat(stopResponse).isEqualTo("STOP 0: (1.2, 1.3)");
        stopResponse = (String)adapter.getItem(1);
        assertThat(stopResponse).isEqualTo("STOP 1: (1.4, 1.5)");
    }
}