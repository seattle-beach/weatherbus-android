package io.pivotal.weatherbus.app;

import android.app.Application;

import static roboguice.RoboGuice.*;

public class WeatherBusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationModule module = new ApplicationModule();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this), module);
    }
}
