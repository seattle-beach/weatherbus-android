package io.pivotal.weatherbus.app;

import android.util.Log;
import com.google.inject.Inject;
import com.sdoward.rxgooglemap.MapObservableProvider;
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

import java.util.List;


@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapActivityTest {

    @Mock
    MapObservableProvider provider;

    @Inject
    WeatherBusService service;

    @InjectMocks
    MapActivity subject;

    PublishSubject<List<StopForLocationResponse>> publishSubject;

    @Before
    public void setUp() throws Exception {
        subject = Robolectric.setupActivity(MapActivity.class);
        publishSubject = PublishSubject.create();
    }

    @Test
    public void onMapReady_shouldDropNearbyPins() throws Exception {
        Robolectric.flushBackgroundThreadScheduler();
        Log.d("Hi","");
    }

    @Test
    public void onCreate_shouldGetCurrentLocation() {
        Log.d("Hi","");
    }
}