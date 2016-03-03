package io.pivotal.weatherbus.app;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.Module;
import dagger.Provides;
import io.pivotal.weatherbus.app.repositories.LocationRepository;
import io.pivotal.weatherbus.app.repositories.WeatherBusMapRepository;
import io.pivotal.weatherbus.app.services.IRetrofitWeatherBusService;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.view.BusRoutesFragment;
import io.pivotal.weatherbus.app.view.MapActivity;
import io.pivotal.weatherbus.app.view.MapStopsFragment;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import javax.inject.Singleton;

@Module(injects = {
        MapActivity.class,
        MapStopsFragment.class,
        BusRoutesFragment.class
})

public class ApplicationModule {

    private Context context;

    ApplicationModule() {

    }

    ApplicationModule(Context context) {
        this.context = context;
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
    WeatherBusMapRepository getMapRepository() {
        return new WeatherBusMapRepository();
    }

    @Provides
    @Singleton
    SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("FavoriteStops", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    SavedStops getSavedStops(SharedPreferences settings) {
        return new SavedStops(settings);
    }
}
