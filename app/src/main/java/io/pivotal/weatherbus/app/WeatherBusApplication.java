package io.pivotal.weatherbus.app;

import android.app.Application;
import dagger.ObjectGraph;

public class WeatherBusApplication extends Application {

    protected static ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(getModule());
    }

    protected Object getModule() {
        return new ApplicationModule(this);
    }

    public static void inject(Object object) {
        objectGraph.inject(object);
    }
}
