package io.pivotal.weatherbus.app.view;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.dynamic.zzd;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.adapter.InfoContentsAdapter;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.model.IconOptions;
import io.pivotal.weatherbus.app.repositories.FavoriteStopsRepository;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.MarkerIconRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.services.response.MultipleStopResponse;
import io.pivotal.weatherbus.app.services.response.RouteReference;
import io.pivotal.weatherbus.app.services.response.StopResponse;
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
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapStopsFragmentTest {

    @Inject WeatherBusService service;
    @Inject WeatherBusMapRepository weatherBusMapRepository;
    @Inject LocationRepository locationRepository;
    @Inject
    FavoriteStopsRepository favoriteStops;
    @Inject
    InfoContentsAdapter infoContentsAdapter;
    @Inject MarkerIconRepository markerIconRepository;

    @Mock WeatherBusMap weatherBusMap;
    @Mock WeatherBusMarker markerZero;
    @Mock WeatherBusMarker markerOne;
    @Mock WeatherBusMarker markerTwo;
    @Mock WeatherBusMarker markerThree;
    @Mock Location location;

    MapStopsFragment subject;

    PublishSubject<MultipleStopResponse> stopEmitter;
    ReplaySubject<Location> locationEmitter;
    BehaviorSubject<WeatherBusMap> mapEmitter;
    PublishSubject<WeatherBusMarker> markerClick;
    PublishSubject<WeatherBusMarker> infoWindowClick;
    PublishSubject<LatLngBounds> cameraChange;

    MultipleStopResponse response;
    PublishSubject<MultipleStopResponse> newStopEmitter;

    StopResponse stopZero;
    StopResponse stopOne;
    StopResponse stopTwo;
    StopResponse stopThree;
    MarkerOptions optionsZero;
    MarkerOptions optionsOne;

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

        stopZero = new StopResponse("1_1234", "STOP 0", "S", 9 , 10, new ArrayList<>(
                Arrays.asList("route_0", "route_1")));
        stopOne = new StopResponse("1_2234", "STOP 1", "", 11 , 11, new ArrayList<>(
                Arrays.asList("route_1", "route_2")));
        stopTwo = new StopResponse("2_2234", "STOP 2", "NE", 18, 17, new ArrayList<String>());
        stopThree = new StopResponse("3_2234", "STOP 3", "W", 19, 16, new ArrayList<String>());

        when(weatherBusMap.getLatLngBounds()).thenReturn(new LatLngBounds(new LatLng(25,30), new LatLng(27,32)));

        when(weatherBusMap.addMarker(argThat(matchesPositionOf(stopZero)))).then(new Answer<WeatherBusMarker>() {
            @Override
            public WeatherBusMarker answer(InvocationOnMock invocationOnMock) throws Throwable {
                optionsZero = (MarkerOptions) invocationOnMock.getArguments()[0];
                return markerZero;
            }
        });
        when(weatherBusMap.addMarker(argThat(matchesPositionOf(stopOne)))).then(new Answer<WeatherBusMarker>() {
            @Override
            public WeatherBusMarker answer(InvocationOnMock invocationOnMock) throws Throwable {
                optionsOne = (MarkerOptions) invocationOnMock.getArguments()[0];
                return markerOne;
            }
        });

        when(weatherBusMap.moveCamera(any(LatLng.class))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                LatLng newCenter = (LatLng) invocationOnMock.getArguments()[0];
                LatLngBounds newBounds = new LatLngBounds(new LatLng(newCenter.latitude - 5, newCenter.longitude - 5),
                        new LatLng(newCenter.latitude + 5, newCenter.longitude + 5));
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
        when(service.getStopsForLocation(location.getLatitude(), location.getLongitude(), 10.0, 10.0)).thenReturn(stopEmitter);
        when(weatherBusMapRepository.getOnCameraChangeObservable(any(MapFragmentAdapter.class))).thenReturn(cameraChange);

        response = new MultipleStopResponse(
                new ArrayList<>(Arrays.asList(stopZero, stopOne)),
                new MultipleStopResponse.BusStopReference(new ArrayList<>(
                        Arrays.asList(new RouteReference("route_0", "THIS IS MY 0 ROUTE", "0"),
                                new RouteReference("route_1", "THIS IS MY 1 ROUTE", ""),
                                new RouteReference("route_2", "", "")))));
        when(markerZero.getPosition()).thenReturn(new LatLng(9,10));
        when(markerOne.getPosition()).thenReturn(new LatLng(11, 11));
        subject = new MapStopsFragment();
        FragmentTestUtil.startFragment(subject, MockActivity.class);
    }

    @Test
    public void onNextMap_shouldSetupMap() {
        mapEmitter.onNext(weatherBusMap);
        verify(weatherBusMap).setMyLocationEnabled(true);
        verify(infoContentsAdapter).setContext(subject.getActivity());
        verify(weatherBusMap).setInfoWindowAdapter(infoContentsAdapter);
    }

    @Test
    public void onNextMapAndLocation_shouldCenterMap() {
        mapEmitter.onNext(weatherBusMap);
        locationEmitter.onNext(location);
        locationEmitter.onCompleted();

        verify(weatherBusMap).moveCamera(new LatLng(location.getLatitude(),location.getLatitude()));
        verify(service).getStopsForLocation(location.getLatitude(), location.getLatitude(), 10.0, 10.0);
    }

    @Test
    public void onNextListStops_shouldAddMarkersToMap() {
        fulfillRequests();
        verify(weatherBusMap, times(2)).addMarker(any(MarkerOptions.class));
        verify(weatherBusMap).addMarker(optionsZero);
        verify(weatherBusMap).addMarker(optionsOne);
    }

    @Test
    public void onNextListStops_shouldSetMarkerOptionsAppropriately() {
        fulfillRequests();
        assertThat(optionsZero.getTitle()).isEqualTo("STOP 0 (S)");
        assertThat(optionsZero.getSnippet()).isEqualTo("Routes: 0, THIS IS MY 1 ROUTE");
        assertThat(optionsOne.getTitle()).isEqualTo("STOP 1");
        assertThat(optionsOne.getSnippet()).isEqualTo("Routes: THIS IS MY 1 ROUTE, route_2");
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
        BitmapDescriptor descriptorZero = new BitmapDescriptor(mock(zzd.class));
        BitmapDescriptor descriptorOne = new BitmapDescriptor(mock(zzd.class));
        when(markerIconRepository.get(new IconOptions(stopZero.getDirection(), true))).
                thenReturn(descriptorZero);
        when(markerIconRepository.get(new IconOptions(stopOne.getDirection(), false))).
                thenReturn(descriptorOne);

        fulfillRequests();

        verify(weatherBusMap,times(2)).addMarker(any(MarkerOptions.class));
        assertThat(optionsZero.getIcon()).isEqualTo(descriptorZero);
        assertThat(optionsOne.getIcon()).isEqualTo(descriptorOne);
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

        BitmapDescriptor favoriteIcon = new BitmapDescriptor(mock(zzd.class));
        BitmapDescriptor regularIcon = new BitmapDescriptor(mock(zzd.class));
        when(markerIconRepository.get(new IconOptions(stopZero.getDirection(), true))).
                thenReturn(favoriteIcon);
        when(markerIconRepository.get(new IconOptions(stopZero.getDirection(), false))).
                thenReturn(regularIcon);

        markerClick.onNext(markerZero);
        subject.setSelectedFavorite(true);
        verify(markerZero).setIcon(favoriteIcon);

        subject.setSelectedFavorite(false);
        verify(markerZero).setIcon(regularIcon);

        moveMap();
        subject.setSelectedFavorite(true);
        verify(markerZero, times(2)).setIcon(favoriteIcon);
    }


    @Test
    public void onCameraChange_shouldReloadStops() {
        fulfillRequests();
        moveMap();
        verify(service).getStopsForLocation(15, 15, 10, 10);

    }

    @Test
    public void onCameraChange_shouldOnlyRemoveNonVisibleMarkers() {
        fulfillRequests();
        moveMap();
        verify(markerZero).remove();
        verify(markerOne, never()).remove();
    }

    @Test
    public void onCameraChange_shouldNotRemoveSelectedMarker() {
        fulfillRequests();
        markerClick.onNext(markerZero);
        moveMap();

        verify(markerZero, never()).remove();
    }

    @Test
    public void onCameraChange_shouldOnlyAddMarkersForNewStops() {
        fulfillRequests();
        moveMap();
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
        when(weatherBusMap.addMarker(argThat(matchesPositionOf(stopOne)))).thenReturn(markerOne);
        when(weatherBusMap.addMarker(argThat(matchesPositionOf(stopTwo)))).thenReturn(markerTwo);
        when(weatherBusMap.addMarker(argThat(matchesPositionOf(stopThree)))).thenReturn(markerThree);
        cameraChange.onNext(new LatLngBounds(new LatLng(10, 10), new LatLng(20, 20)));

        MultipleStopResponse response = new MultipleStopResponse(
                new ArrayList<>(Arrays.asList(stopOne, stopTwo, stopThree)),
                new MultipleStopResponse.BusStopReference(new ArrayList<>(
                        Arrays.asList(new RouteReference("route_1", "THIS IS MY 1 ROUTE", ""),
                                new RouteReference("route_2", "", "")))));

        newStopEmitter.onNext(response);
    }

    private ArgumentMatcher<MarkerOptions> matchesPositionOf(final StopResponse busStop) {
        return new ArgumentMatcher<MarkerOptions>() {
            @Override
            public boolean matches(Object argument) {
                if(argument == null) return false;
                LatLng position = ((MarkerOptions) argument).getPosition();
                return position.latitude == busStop.getLatitude() &&
                        position.longitude == busStop.getLongitude();
            }
        };
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