package io.pivotal.weatherbus.app;

import dagger.Module;
import dagger.Provides;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.view.BusRoutesFragmentTest;
import io.pivotal.weatherbus.app.view.InfoContentsAdapter;
import io.pivotal.weatherbus.app.view.MapStopsFragmentTest;
import io.pivotal.weatherbus.app.view.WeatherBusActivityTest;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

@Module(includes = ApplicationModule.class,
        overrides = true,
        library = true,
        injects = {
                WeatherBusActivityTest.class,
                MapStopsFragmentTest.class,
                WeatherBusActivityTest.FakeWeatherBusActivity.class,
                BusRoutesFragmentTest.class
        }
)
public class TestApplicationModule {
    @Provides
    @Singleton
    WeatherBusService getWeatherBusService() {
        return mock(WeatherBusService.class);
    }

    @Provides
    @Singleton
    LocationRepository getLocationRepository() {
        return mock(LocationRepository.class);
    }

    @Provides
    @Singleton
    WeatherBusMapRepository getMapRepository() {
        return mock(WeatherBusMapRepository.class);
    }

    @Provides
    @Singleton
    SavedStops getSavedStops() {
        return mock(SavedStops.class);
    }

    @Provides
    @Singleton
    InfoContentsAdapter getInfoContentsAdapter() {
        return mock(InfoContentsAdapter.class);
    }
}
