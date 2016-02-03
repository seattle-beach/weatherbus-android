package io.pivotal.weatherbus.app;

import android.view.View;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.model.BusStop;
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
    SavedStops savedStops;

    @Mock
    GoogleMapWrapper googleMap;

    MapActivity subject;

    PublishSubject<StopForLocationResponse> stopEmitter;
    PublishSubject<GoogleMapWrapper> mapEmitter;

    StopForLocationResponse response;

    @Before
    public void setUp() throws Exception {
        mapEmitter = PublishSubject.create();
        when(mapRepository.create(any(MapFragment.class), any(MapActivity.class))).thenReturn(mapEmitter);

        stopEmitter = PublishSubject.create();

        subject = Robolectric.setupActivity(MapActivity.class);

        LatLngBounds bounds = new LatLngBounds(new LatLng(4,4), new LatLng(6,6));
        when(googleMap.getLatLngBounds()).thenReturn(bounds);
        when(service.getStopsForLocation(5.0, 5.0, 2.0, 2.0)).thenReturn(stopEmitter);

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
    public void onCreate_shouldShowProgressBar() {
        assertThat(subject.findViewById(R.id.progressBar).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onNextGoogleMap_shouldSetListHeaderToLocation() {
        mapEmitter.onNext(googleMap);

        TextView header = (TextView)subject.findViewById(R.id.currentLocation);
        assertThat(header).isNotNull();
        assertThat(header.getText().toString()).isEqualTo("(5.0, 5.0)");
    }

    @Test
    public void onNextListStops_shouldShowNearbyStops() {
        fullfillRequests();

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = ((HeaderViewListAdapter)lv.getAdapter()).getWrappedAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);

        String stopResponse = ((TextView) (lv.getChildAt(1))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 0: (4.2, 4.3)");
        stopResponse = ((TextView) (lv.getChildAt(2))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 1: (4.4, 4.5)");

        verify(googleMap, times(2)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onNextListStops_shouldRemoveProgressBar() {
        fullfillRequests();

        assertThat(subject.findViewById(R.id.progressBar).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onNextListStops_ifStopIsFavorite_ShouldShowAStar() {
        when(savedStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
            add("1_2234");
        }});

        fullfillRequests();

        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        String firstStop = ((TextView) (lv.getChildAt(1))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isEqualTo('*');
    }

    @Test
    public void onTapStop_shouldAddToFavoriteStops() {
        fullfillRequests();
        ListView lv = (ListView)subject.findViewById(R.id.stopList);
        shadowOf(lv).populateItems();
        Adapter adapter = ((HeaderViewListAdapter)lv.getAdapter()).getWrappedAdapter();
        lv.performItemClick(lv.getChildAt(0),1,adapter.getItemId(0));

        String busStopId = ((BusStop) adapter.getItem(0)).getResponse().getId();
        verify(savedStops,times(1)).addSavedStop(busStopId);
        String firstStop = ((TextView) (lv.getChildAt(1))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isEqualTo('*');
    }

    private void fullfillRequests() {
        mapEmitter.onNext(googleMap);
        stopEmitter.onNext(response);
        stopEmitter.onCompleted();
    }
}