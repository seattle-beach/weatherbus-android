package io.pivotal.weatherbus.app;

import android.app.Application;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.pivotal.weatherbus.app.services.IRetrofitWeatherBusService;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import static roboguice.RoboGuice.*;

public class WeatherBusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this), new ApplicationModule());
    }
}
