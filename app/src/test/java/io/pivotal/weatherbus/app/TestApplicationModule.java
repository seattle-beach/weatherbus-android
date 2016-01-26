package io.pivotal.weatherbus.app;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
}
