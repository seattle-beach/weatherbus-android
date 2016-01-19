package io.pivotal.weatherbus.app;

import android.app.Application;
import com.google.inject.AbstractModule;

import static roboguice.RoboGuice.*;

public class WeatherBusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this), new ApplicationModule());
    }

    public static class ApplicationModule extends AbstractModule {

        @Override
        protected void configure() {

        }
    }
}
