package io.pivotal.weatherbus.app;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.IRetrofitWeatherBusService;
import io.pivotal.weatherbus.app.services.WeatherBusService;

import static org.mockito.Mockito.mock;

public class TestApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    WeatherBusService getWeatherBusService() {
        return mock(WeatherBusService.class);
    }

    @Provides
    @Singleton
    IRetrofitWeatherBusService getIRetrofitWeatherBusService() {
        return mock(IRetrofitWeatherBusService.class);
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
