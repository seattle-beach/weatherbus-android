package io.pivotal.weatherbus.app;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.Module;
import dagger.Provides;
import io.pivotal.weatherbus.app.adapter.InfoContentsAdapter;
import io.pivotal.weatherbus.app.repositories.*;
import io.pivotal.weatherbus.app.services.IRetrofitWeatherBusService;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.view.BusRoutesFragment;
import io.pivotal.weatherbus.app.view.MapStopsFragment;
import io.pivotal.weatherbus.app.view.WeatherBusActivity;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import javax.inject.Singleton;

@Module(injects = {
        WeatherBusActivity.class,
        MapStopsFragment.class,
        BusRoutesFragment.class,
        MarkerIconRepository.class
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
    FavoriteStopsRepository getSavedStops(SharedPreferences settings) {
        return new FavoriteStopsRepository(settings);
    }

    @Provides
    InfoContentsAdapter getInfoContentsAdapter() {
        return new InfoContentsAdapter();
    }

    @Provides
    @Singleton
    MarkerIconRepository getMarkerIconRepository() {
        return new MarkerIconRepository();
    }

    @Provides
    @Singleton
    MarkerIconFactory getMarkerIconFactory() {
        return new MarkerIconFactory(context);
    }
}
