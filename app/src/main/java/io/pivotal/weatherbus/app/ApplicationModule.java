package io.pivotal.weatherbus.app;

import android.content.SharedPreferences;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.IRetrofitWeatherBusService;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {

    }


    @Provides
    @Singleton
    WeatherBusService getService(IRetrofitWeatherBusService weatherBusService) {
        return new WeatherBusService(weatherBusService);
    }

    @Provides
    @Singleton
    IRetrofitWeatherBusService getIRetrofitWeatherBusService() {
        RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint("http://weatherbus-prime-dev.cfapps.io");
        builder.setClient(new OkClient());
        RestAdapter adapter = builder.build();
        return adapter.create(IRetrofitWeatherBusService.class);
    }

    @Provides
    @Singleton
    LocationRepository getLocationRepository() {
        return new LocationRepository();
    }

    @Provides
    @Singleton
    WeatherBusMapRepository getMapRepository(LocationRepository locationRepository) {
        return new WeatherBusMapRepository(locationRepository);
    }

    @Provides
    @Singleton
    SavedStops getSavedStops(SharedPreferences settings) {
        return new SavedStops(settings);
    }
}
