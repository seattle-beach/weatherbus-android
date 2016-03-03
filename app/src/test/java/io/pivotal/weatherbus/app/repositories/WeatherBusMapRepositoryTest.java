package io.pivotal.weatherbus.app.repositories;

import android.location.Location;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.map.MapFragmentAdapter;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import io.pivotal.weatherbus.app.view.WeatherBusActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class WeatherBusMapRepositoryTest {

    @Mock
    WeatherBusMap weatherbusMap;

    @Mock
    WeatherBusActivity weatherBusActivity;

    @Mock
    Location location;

    WeatherBusMapRepository subject;

    MapFragmentAdapter fragmentAdapter;

    @Before
    public void setup() {
        subject = new WeatherBusMapRepository();
        fragmentAdapter = new StubMapFragmentAdapter();
    }

    @Test
    public void create_ShouldReturnMapObservable() {
        TestSubscriber<WeatherBusMap> subscriber = new TestSubscriber<WeatherBusMap>();
        subject.getOnMapReadyObservable(fragmentAdapter).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertReceivedOnNext(Arrays.asList(weatherbusMap));
    }

    @Test
    public void getOnMarkerClick_shouldEmitMarker_ifMarkerIsClicked() {
        final WeatherBusMarker marker = mock(WeatherBusMarker.class);

        when(weatherbusMap.setOnMarkerClickListener(any(WeatherBusMap.OnWeatherBusMarkerClick.class))).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                WeatherBusMap.OnWeatherBusMarkerClick listener = (WeatherBusMap.OnWeatherBusMarkerClick) invocation.getArguments()[0];
                listener.onMarkerClick(marker);
                return null;
            }
        });


        Observable<WeatherBusMarker> markerObservable = subject.getOnMarkerClickObservable(fragmentAdapter);
        TestSubscriber<WeatherBusMarker> subscriber = new TestSubscriber<WeatherBusMarker>();
        markerObservable.subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertReceivedOnNext(Arrays.asList(marker));
    }

    @Test
    public void getOnInfoWindowClick_shouldEmitMarker() {
        final WeatherBusMarker marker = mock(WeatherBusMarker.class);

        when(weatherbusMap.setOnInfoWindowClickListener(any(WeatherBusMap.OnWeatherBusInfoClickListener.class))).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                WeatherBusMap.OnWeatherBusInfoClickListener listener = (WeatherBusMap.OnWeatherBusInfoClickListener) invocation.getArguments()[0];
                listener.onInfoWindowClick(marker);
                return null;
            }
        });


        Observable<WeatherBusMarker> markerObservable = subject.getOnInfoWindowClickObservable(fragmentAdapter);
        TestSubscriber<WeatherBusMarker> subscriber = new TestSubscriber<WeatherBusMarker>();
        markerObservable.subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertReceivedOnNext(Arrays.asList(marker));
    }

    @Test
    public void getOnCameraChange_shouldEmitLatLngBounds() {
        final LatLngBounds latLngBounds = new LatLngBounds(new LatLng(5,2), new LatLng(10,11));
        final CameraPosition cameraPosition = new CameraPosition(new LatLng(3,3), 5.0f, 40, 40);

        when(weatherbusMap.setOnCameraChangeListener(any(WeatherBusMap.OnWeatherBusCameraChangeListener.class))).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                WeatherBusMap.OnWeatherBusCameraChangeListener listener = (WeatherBusMap.OnWeatherBusCameraChangeListener) invocation.getArguments()[0];
                listener.onCameraChange(cameraPosition);
                return null;
            }
        });
        when(weatherbusMap.getLatLngBounds()).thenReturn(latLngBounds);
        Observable<LatLngBounds> observable = subject.getOnCameraChangeObservable(fragmentAdapter);
        TestSubscriber<LatLngBounds> subscriber = new TestSubscriber<LatLngBounds>();
        observable.subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertReceivedOnNext(Arrays.asList(latLngBounds));
    }

    @Test
    public void onReset_itShouldReturnNewObservable() {
        Observable<WeatherBusMap> first = subject.getOnMapReadyObservable(fragmentAdapter);
        subject.reset();
        Observable<WeatherBusMap> second = subject.getOnMapReadyObservable(fragmentAdapter);
        assertFalse(first.equals(second));
    }

    @Test
    public void withoutReset_itShouldReturnSameObservable() {
        Observable<WeatherBusMap> first = subject.getOnMapReadyObservable(fragmentAdapter);
        Observable<WeatherBusMap> second = subject.getOnMapReadyObservable(fragmentAdapter);
        assertTrue(first.equals(second));
    }

    @Test
    public void cacheIsValidatedAndInvalidatedAsRequested() throws Exception {
        Field isCacheValidField = subject.getClass().getDeclaredField("isCacheValid");
        isCacheValidField.setAccessible(true);
        assertFalse(isCacheValidField.getBoolean(subject));

        subject.getOnMapReadyObservable(fragmentAdapter);
        assertTrue(isCacheValidField.getBoolean(subject));
        subject.reset();
        assertFalse(isCacheValidField.getBoolean(subject));

        subject.getOnMarkerClickObservable(fragmentAdapter);
        assertTrue(isCacheValidField.getBoolean(subject));
        subject.reset();
        assertFalse(isCacheValidField.getBoolean(subject));

        subject.getOnInfoWindowClickObservable(fragmentAdapter);
        assertTrue(isCacheValidField.getBoolean(subject));
        subject.reset();
        assertFalse(isCacheValidField.getBoolean(subject));

        subject.getOnCameraChangeObservable(fragmentAdapter);
        assertTrue(isCacheValidField.getBoolean(subject));
        subject.reset();
        assertFalse(isCacheValidField.getBoolean(subject));
    }

    private class StubMapFragmentAdapter extends MapFragmentAdapter {
        public StubMapFragmentAdapter() {
            super(null);
        }

        @Override
        public void getMapAsync(WeatherBusMap.OnWeatherBusMapReadyCallback callback) {
            callback.onMapReady(WeatherBusMapRepositoryTest.this.weatherbusMap);
        }
    }
}