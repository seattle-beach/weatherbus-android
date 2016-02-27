package io.pivotal.weatherbus.app.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.SavedStops;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import javax.inject.Inject;
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
    WeatherBusMapRepository weatherBusMapRepository;

    @Inject
    LocationRepository locationRepository;

    @Inject
    SavedStops favoriteStops;

    @Mock
    WeatherBusMap weatherBusMap;

    @Mock
    WeatherBusMarker markerZero;
    @Mock
    WeatherBusMarker markerOne;
    @Mock
    WeatherBusMarker markerTwo;
    @Mock
    WeatherBusMarker markerThree;

    @Mock
    Location location;

    MapActivity subject;

    PublishSubject<StopForLocationResponse> stopEmitter;
    ReplaySubject<Location> locationEmitter;
    BehaviorSubject<WeatherBusMap> mapEmitter;
    PublishSubject<WeatherBusMarker> markerClick;
    PublishSubject<WeatherBusMarker> infoWindowClick;
    PublishSubject<LatLngBounds> cameraChange;

    StopForLocationResponse response;
    ListView stopList;
    private PublishSubject<StopForLocationResponse> newStopEmitter;

    @Before
    public void setUp() throws Exception {
        WeatherBusApplication.inject(this);
        mapEmitter = BehaviorSubject.create();
        locationEmitter = ReplaySubject.createWithSize(1);
        stopEmitter = PublishSubject.create();
        markerClick = PublishSubject.create();
        infoWindowClick = PublishSubject.create();
        cameraChange = PublishSubject.create();
        newStopEmitter = PublishSubject.create();

        when(weatherBusMap.getLatLngBounds()).thenReturn(new LatLngBounds(new LatLng(25,30), new LatLng(27,32)));
        when(weatherBusMap.addMarker(argThat(new MatchesTitle("STOP 0")))).thenReturn(markerZero);
        when(weatherBusMap.addMarker(argThat(new MatchesTitle("STOP 1")))).thenReturn(markerTwo);
        when(weatherBusMap.moveCamera(any(LatLng.class))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                LatLng newCenter = (LatLng) invocationOnMock.getArguments()[0];
                LatLngBounds newBounds = new LatLngBounds(new LatLng(newCenter.latitude - 1, newCenter.longitude - 1),
                        new LatLng(newCenter.latitude + 1, newCenter.longitude + 1));
                when(weatherBusMap.getLatLngBounds()).thenReturn(newBounds);
                return null;
            }
        });

        when(location.getLatitude()).thenReturn(5.0);
        when(location.getLongitude()).thenReturn(5.0);

        when(locationRepository.fetch(any(MapActivity.class))).thenReturn(locationEmitter);
        when(weatherBusMapRepository.getOnMapReadyObservable(any(MapFragmentAdapter.class))).thenReturn(mapEmitter);
        when(weatherBusMapRepository.getOnMarkerClickObservable(any(MapFragmentAdapter.class))).thenReturn(markerClick);
        when(weatherBusMapRepository.getOnInfoWindowClickObservable(any(MapFragmentAdapter.class))).thenReturn(infoWindowClick);
        when(service.getStopsForLocation(location.getLatitude(), location.getLongitude(), 2.0, 2.0)).thenReturn(stopEmitter);
        when(weatherBusMapRepository.getOnCameraChangeObservable(any(MapFragmentAdapter.class))).thenReturn(cameraChange);

        subject = Robolectric.setupActivity(MapActivity.class);
        subject.onWindowFocusChanged(true);

        response = new StopForLocationResponse() {{
            setStops(new ArrayList<BusStopResponse>() {{
                add(new BusStopResponse("1_1234","STOP 0", 4.2 , 4.3));
                add(new BusStopResponse("1_2234","STOP 1", 4.4 , 4.5));
            }});
        }};

        stopList = (ListView)subject.findViewById(R.id.stopList);
    }

    @Test
    public void onCreate_shouldShowProgressBar() {
        assertThat(subject.findViewById(R.id.progressBar).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onNextMap_shouldOffsetBottomOfMap_toTopOfListView() {
        mapEmitter.onNext(weatherBusMap);
        verify(weatherBusMap).setPadding(0, 0 , 0 , stopList.getTop());
    }

    @Test
    public void onNextMapAndLocation_shouldCenterMapAndEnableLocation() {
        mapEmitter.onNext(weatherBusMap);
        locationEmitter.onNext(location);
        locationEmitter.onCompleted();
        verify(weatherBusMap).setMyLocationEnabled(true);
        verify(weatherBusMap).moveCamera(new LatLng(location.getLatitude(),location.getLatitude()));
        verify(service).getStopsForLocation(location.getLatitude(), location.getLatitude(), 2.0, 2.0);
    }

    @Test
    public void onNextMapAndLocationAndStops_shouldShowNearbyStops() {
        fulfillRequests();

        shadowOf(stopList).populateItems();
        assertThat(stopList.getChildCount()).isEqualTo(2);

        String stopResponse = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 0: (4.2, 4.3)");
        stopResponse = ((TextView) (stopList.getChildAt(1))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 1: (4.4, 4.5)");

        verify(weatherBusMap, times(2)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onNextListStops_shouldRemoveProgressBar() {
        fulfillRequests();

        assertThat(subject.findViewById(R.id.progressBar).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onNextListStops_ifStopIsFavorite_ShouldShowAStarAndColorMarker() {
        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});

        fulfillRequests();

        shadowOf(stopList).populateItems();
        String firstStop = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isEqualTo('*');
        verify(weatherBusMap,times(2)).addMarker(any(MarkerOptions.class));
        verify(markerZero).setFavorite(true);
        verify(markerTwo).setFavorite(false);
    }

    @Test
    public void onLongClick_whenIsNotFavoriteStop_shouldAddToFavoriteStops() {
        fulfillRequests();
        shadowOf(stopList).populateItems();
        Adapter adapter = stopList.getAdapter();

        assertThat(stopList.getOnItemLongClickListener().
                onItemLongClick(stopList,stopList.getChildAt(0),0,adapter.getItemId(0))).isEqualTo(true);

        String busStopId = ((BusStop) adapter.getItem(0)).getId();
        verify(favoriteStops).addSavedStop(busStopId);
        String firstStop = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isEqualTo('*');
        verify(markerZero).setFavorite(true);
    }

    @Test
    public void onLongClick_shouldRemoveFavoriteStops() {
        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
            add("1_2234");
        }});

        fulfillRequests();
        shadowOf(stopList).populateItems();
        Adapter adapter = stopList.getAdapter();

        assertThat(stopList.getOnItemLongClickListener().
                onItemLongClick(stopList,stopList.getChildAt(0),0,adapter.getItemId(0))).isEqualTo(true);

        String busStopId = ((BusStop) adapter.getItem(0)).getId();
        verify(favoriteStops).deleteSavedStop(busStopId);
        String firstStop = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(firstStop.charAt(firstStop.length() - 1)).isNotEqualTo('*');
        verify(markerZero).setFavorite(false);
    }

    @Test
    public void onClick_itShouldOpenBusStopActivity() {
        fulfillRequests();
        shadowOf(stopList).populateItems();
        Adapter adapter = stopList.getAdapter();
        stopList.performItemClick(stopList.getChildAt(0), 0, adapter.getItemId(0));
        Intent intent = shadowOf(subject).peekNextStartedActivityForResult().intent;
        assertThat(intent.getStringExtra("stopId")).isEqualTo("1_1234");
        assertThat(intent.getStringExtra("stopName")).isEqualTo("STOP 0");
        assertThat(intent.getComponent()).isEqualTo(new ComponentName(subject, BusStopActivity.class));
    }

    @Test
    public void onCameraChange_shouldReloadStops() {
        fulfillRequests();
        moveMap();

        verify(service).getStopsForLocation(15, 15, 10, 10);

        shadowOf(stopList).populateItems();
        assertThat(stopList.getChildCount()).isEqualTo(3);

        String stopResponse = ((TextView) (stopList.getChildAt(0))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 1: (4.4, 4.5)");
        stopResponse = ((TextView) (stopList.getChildAt(1))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 2: (2.2, 2.3)");
        stopResponse = ((TextView) (stopList.getChildAt(2))).getText().toString();
        assertThat(stopResponse).isEqualTo("STOP 3: (3.2, 3.3)");

        verify(weatherBusMap, times(3)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onCameraChange_shouldClearAllMarkersButSelected() {
        fulfillRequests();
        shadowOf(stopList).populateItems();
        markerClick.onNext(markerTwo);
        moveMap();

        verify(markerZero).remove();
        verify(markerTwo,never()).remove();
        verify(weatherBusMap, times(2)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onDestroy_itShouldUnsubscribeFromObservables() {
        subject.onDestroy();
        verify(weatherBusMapRepository).reset();
        assertThat(locationEmitter.hasObservers()).isFalse();
        assertThat(mapEmitter.hasObservers()).isFalse();
        assertThat(stopEmitter.hasObservers()).isFalse();
        assertThat(markerClick.hasObservers()).isFalse();
        assertThat(infoWindowClick.hasObservers()).isFalse();
        assertThat(newStopEmitter.hasObservers()).isFalse();
        assertThat(cameraChange.hasObservers()).isFalse();
    }

    @Test
    public void onMarkerClick_itShouldShowSelectedStopFirst() {
        fulfillRequests();
        markerClick.onNext(markerZero);
        moveMap();
        shadowOf(stopList).populateItems();
        TextView textView;
        textView = (TextView) stopList.getChildAt(0);
        assertThat(textView.getText().toString()).contains("STOP 0");
        markerClick.onNext(markerThree);
        moveMap();
        shadowOf(stopList).populateItems();
        textView = (TextView) stopList.getChildAt(0);
        assertThat(textView.getText().toString()).contains("STOP 3");
    }

    @Test
    public void onInfoWindowClick_itShouldOpenBusStopActivity() {
        fulfillRequests();
        infoWindowClick.onNext(markerTwo);
        Intent intent = shadowOf(subject).peekNextStartedActivityForResult().intent;
        assertThat(intent.getComponent()).isEqualTo(new ComponentName(subject, BusStopActivity.class));
        assertThat(intent.getStringExtra("stopId")).isEqualTo("1_2234");
        assertThat(intent.getStringExtra("stopName")).isEqualTo("STOP 1");
    }

    private void fulfillRequests() {
        mapEmitter.onNext(weatherBusMap);
        locationEmitter.onNext(location);
        locationEmitter.onCompleted();
        stopEmitter.onNext(response);
        stopEmitter.onCompleted();
    }

    private void moveMap() {
        reset(service);
        reset(weatherBusMap);
        when(service.getStopsForLocation(15, 15, 10, 10)).thenReturn(newStopEmitter);
        when(weatherBusMap.addMarker(argThat(new MatchesTitle("STOP 1")))).thenReturn(markerOne);
        when(weatherBusMap.addMarker(argThat(new MatchesTitle("STOP 2")))).thenReturn(markerTwo);
        when(weatherBusMap.addMarker(argThat(new MatchesTitle("STOP 3")))).thenReturn(markerThree);
        cameraChange.onNext(new LatLngBounds(new LatLng(10, 10), new LatLng(20, 20)));
        StopForLocationResponse response = new StopForLocationResponse() {{
            setStops(new ArrayList<BusStopResponse>() {{
                add(new BusStopResponse("1_2234","STOP 1",4.4,4.5));
                add(new BusStopResponse("2_2234","STOP 2" ,2.2,2.3));
                add(new BusStopResponse("3_2234","STOP 3" ,3.2,3.3));
            }});
        }};

        newStopEmitter.onNext(response);
    }

    private class MatchesTitle extends ArgumentMatcher<MarkerOptions> {
        String name;
        public MatchesTitle(String name) {
            this.name = name;
        }
        @Override
        public boolean matches(Object argument) {
            if(argument == null) return false;
            return ((MarkerOptions) argument).getTitle().equals(name);
        }
    }
}