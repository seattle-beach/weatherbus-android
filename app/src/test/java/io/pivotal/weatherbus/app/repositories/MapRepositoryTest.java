package io.pivotal.weatherbus.app.repositories;

import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.activities.MapActivity;
import io.pivotal.weatherbus.app.map.*;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Arrays;

import android.location.Location;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.plugins.RxJavaTestPlugins;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapRepositoryTest {

    @Inject
    LocationRepository locationRepository;

    @Mock
    WeatherBusMap weatherbusMap;

    @Mock
    MapActivity mapActivity;

    @Mock
    Location location;

    MapRepository subject;

    MapFragmentAdapter fragmentAdapter;

    @Before
    public void setup() {
        subject = new MapRepository(locationRepository);
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

        when(weatherbusMap.setOnMarkerClickListener(any(OnWeatherBusMarkerClick.class))).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                OnWeatherBusMarkerClick listener = (OnWeatherBusMarkerClick) invocation.getArguments()[0];
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

    private class StubMapFragmentAdapter extends MapFragmentAdapter {
        public StubMapFragmentAdapter() {
            super(null);
        }

        @Override
        public void getMapAsync(OnWeatherBusMapReadyCallback callback) {
            callback.onMapReady(MapRepositoryTest.this.weatherbusMap);
        }
    }
}