package io.pivotal.weatherbus.app;

import dagger.Module;
import dagger.Provides;
import io.pivotal.weatherbus.app.activities.BusStopActivityTest;
import io.pivotal.weatherbus.app.activities.MapActivityTest;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.WeatherBusService;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

@Module(includes = ApplicationModule.class,
        overrides = true,
        library = true,
        injects = {
                BusStopActivityTest.class,
                MapActivityTest.class
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
}
