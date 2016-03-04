package io.pivotal.weatherbus.app.view;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import io.pivotal.weatherbus.app.BuildConfig;
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
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapStopsFragmentTest {

    @Inject
    WeatherBusService service;
    @Inject WeatherBusMapRepository weatherBusMapRepository;
    @Inject LocationRepository locationRepository;
    @Inject SavedStops favoriteStops;

    @Mock WeatherBusMap weatherBusMap;
    @Mock WeatherBusMarker markerZero;
    @Mock WeatherBusMarker markerOne;
    @Mock WeatherBusMarker markerTwo;
    @Mock WeatherBusMarker markerThree;
    @Mock Location location;

    MapStopsFragment subject;

    PublishSubject<StopForLocationResponse> stopEmitter;
    ReplaySubject<Location> locationEmitter;
    BehaviorSubject<WeatherBusMap> mapEmitter;
    PublishSubject<WeatherBusMarker> markerClick;
    PublishSubject<WeatherBusMarker> infoWindowClick;
    PublishSubject<LatLngBounds> cameraChange;

    StopForLocationResponse response;
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
        when(weatherBusMap.addMarker(argThat(new MatchesTitle("STOP 1")))).thenReturn(markerOne);
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

        when(locationRepository.fetch(any(WeatherBusActivity.class))).thenReturn(locationEmitter);
        when(weatherBusMapRepository.getOnMapReadyObservable(any(MapFragmentAdapter.class))).thenReturn(mapEmitter);
        when(weatherBusMapRepository.getOnMarkerClickObservable(any(MapFragmentAdapter.class))).thenReturn(markerClick);
        when(weatherBusMapRepository.getOnInfoWindowClickObservable(any(MapFragmentAdapter.class))).thenReturn(infoWindowClick);
        when(service.getStopsForLocation(location.getLatitude(), location.getLongitude(), 2.0, 2.0)).thenReturn(stopEmitter);
        when(weatherBusMapRepository.getOnCameraChangeObservable(any(MapFragmentAdapter.class))).thenReturn(cameraChange);


        response = new StopForLocationResponse() {{
            setStops(new ArrayList<BusStopResponse>() {{
                add(new BusStopResponse("1_1234", "STOP 0", "S", 4.2 , 4.3));
                add(new BusStopResponse("1_2234", "STOP 1", "NW", 4.4 , 4.5));
            }});
        }};
        subject = new MapStopsFragment();
        FragmentTestUtil.startFragment(subject, MockActivity.class);
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
    public void onNextMapAndLocationAndStops_shouldAddMarkersToMap() {
        fulfillRequests();
        verify(weatherBusMap, times(2)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onNextListStops_shouldAlertActivity() {
        fulfillRequests();
        assertThat(((MockActivity) subject.getActivity()).stopsLoaded).isEqualTo(true);
    }

    @Test
    public void onNextListStops_shouldAddMarkerAsFavorite() {
        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});

        fulfillRequests();

        verify(weatherBusMap,times(2)).addMarker(any(MarkerOptions.class));
        verify(markerZero).setFavorite(true);
        verify(markerOne).setFavorite(false);
    }

    @Test
    public void onMarkerClick_shouldAlertActivity() {
        fulfillRequests();
        markerClick.onNext(markerZero);
        assertThat(((MockActivity) subject.getActivity()).selectedStop.getId()).isEqualTo("1_1234");

        moveMap();
        assertThat(((MockActivity) subject.getActivity()).selectedStop.getId()).isEqualTo("1_1234");
    }

    @Test
    public void setSelectedFavorite_shouldSetMarkerToFavoriteOrNot() {
        fulfillRequests();
        reset(markerZero);
        markerClick.onNext(markerZero);
        subject.setSelectedFavorite(true);
        verify(markerZero).setFavorite(true);

        subject.setSelectedFavorite(false);
        verify(markerZero).setFavorite(false);

        moveMap();
        subject.setSelectedFavorite(true);
        verify(markerZero, times(2)).setFavorite(true);
    }


    @Test
    public void onCameraChange_shouldReloadStops() {
        fulfillRequests();
        moveMap();

        verify(service).getStopsForLocation(15, 15, 10, 10);
        verify(weatherBusMap, times(3)).addMarker(any(MarkerOptions.class));
    }

    @Test
    public void onCameraChange_shouldClearAllMarkersButSelected() {
        fulfillRequests();
        markerClick.onNext(markerOne);
        moveMap();

        verify(markerZero).remove();
        verify(markerOne,never()).remove();
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
                add(new BusStopResponse("1_2234", "STOP 1", "NW", 4.4, 4.5));
                add(new BusStopResponse("2_2234", "STOP 2", "SE", 2.2, 2.3));
                add(new BusStopResponse("3_2234", "STOP 3", "W", 3.2, 3.3));
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

    public static class MockActivity extends Activity implements FragmentListener {
        boolean stopsLoaded = false;
        BusStop selectedStop = null;

        public MockActivity() {
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onStopsLoaded() {
            stopsLoaded = true;
        }

        @Override
        public void onStopSelected(BusStop busStop) {
            selectedStop = busStop;
        }
    }
}